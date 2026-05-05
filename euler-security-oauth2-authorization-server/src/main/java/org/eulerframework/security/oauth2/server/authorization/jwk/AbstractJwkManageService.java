/*
 * Copyright 2013-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eulerframework.security.oauth2.server.authorization.jwk;

import com.nimbusds.jose.jwk.JWK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eulerframework.security.oauth2.server.authorization.jwk.source.ReloadableJwkSource;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Backend-agnostic base class implementing the {@link JwkManageService}
 * template. Subclasses plug in backend specifics through the
 * {@code doCreate / doFindByKid / doList / doUpdate / doPatch / doDelete}
 * hooks; the base class owns the common validation, audit logging, and
 * post-write reload notification pipeline so each backend does not re-invent
 * the flow.
 *
 * <h2>Validation pipeline</h2>
 * On every write the template runs:
 * <ol>
 *   <li>Pre-existence lookup via {@link #doFindByKid(String)};</li>
 *   <li>Cross-entry {@link #validateAggregate(List) validateAggregate} against
 *       the projected post-write entry set;</li>
 *   <li>Backend hook ({@code doCreate / doUpdate / doPatch / doDelete});</li>
 *   <li>A best-effort {@link JwkRepositoryChangedEvent} broadcast.</li>
 * </ol>
 *
 * <h2>Reload notification</h2>
 * The service never calls {@link ReloadableJwkSource#reload()} directly and
 * does not wait for any form of cluster convergence. Instead it publishes a
 * {@link JwkRepositoryChangedEvent} via the injected
 * {@link ApplicationEventPublisher}; listeners &mdash; typically the live
 * {@link ReloadableJwkSource} bean &mdash; are responsible for reacting to
 * that event on a best-effort basis. The publisher MUST NOT block on, inspect,
 * or recover from listener failures: the repository has already committed the
 * write by the time the event is dispatched, so downstream reload failure
 * never rolls back a mutation.
 *
 * <h2>Single-entry validation</h2>
 * Per-entry JWK self-description (supported {@code alg}, {@code use=sig},
 * non-null {@code iat}) is not enforced here; subclasses MUST apply their
 * own self-description gate inside {@code doCreate / doUpdate / doPatch}
 * before persisting.
 */
public abstract class AbstractJwkManageService implements JwkManageService {

    private static final Logger log = LoggerFactory.getLogger(AbstractJwkManageService.class);
    private static final Logger audit = LoggerFactory.getLogger("org.eulerframework.audit.jwk");

    private final ApplicationEventPublisher publisher;

    /**
     * @param publisher the Spring event publisher used to broadcast
     *                  {@link JwkRepositoryChangedEvent} after every successful
     *                  write; MUST be non-null
     */
    protected AbstractJwkManageService(ApplicationEventPublisher publisher) {
        this.publisher = Objects.requireNonNull(publisher, "publisher");
    }

    // ---- backend hooks: deferred to subclasses ----

    /**
     * Persist a brand-new entry. The template has already verified that no
     * entry with the same {@link JwkEntry#kid() kid} exists and that the
     * projected post-write entry set satisfies the cross-entry contract.
     */
    protected abstract JwkEntry doCreate(JwkEntry entry);

    /**
     * Look up a single entry by its {@link JwkEntry#kid() kid}.
     *
     * @return the entry, or {@code null} if not found
     */
    protected abstract JwkEntry doFindByKid(String kid);

    /**
     * Return every stored entry. Ordering is unspecified.
     */
    protected abstract List<JwkEntry> doList();

    /**
     * Persist a full-overwrite update. The template has already verified that
     * the target entry exists and that the projected post-write entry set
     * satisfies the cross-entry contract.
     */
    protected abstract void doUpdate(JwkEntry entry);

    /**
     * Persist a partial update. The incoming {@code entry} has already been
     * merged with the current persisted state (non-null fields applied,
     * null fields carried over from the existing row) and validated.
     */
    protected abstract void doPatch(JwkEntry entry);

    /**
     * Physically remove the entry identified by {@code kid}. The template
     * has already verified that the entry exists and is in
     * {@link JwkStatus#RETIRED RETIRED}.
     */
    protected abstract void doDelete(String kid);

    // ---- template methods ----

    @Override
    public JwkEntry createKey(JwkEntry entry) {
        Objects.requireNonNull(entry, "entry");
        if (doFindByKid(entry.kid()) != null) {
            throw new IllegalStateException("duplicate kid: " + entry.kid());
        }
        List<JwkEntry> projected = new ArrayList<>(doList());
        projected.add(entry);
        validateAggregate(projected);

        String operator = currentOperator();
        audit.info("jwk.manage createKey attempt (operator={}, kid={}, alg={}, status={})",
                operator, entry.kid(), algName(entry), entry.status());
        JwkEntry saved = doCreate(entry);
        publishChanged(saved.kid(), "create");
        audit.info("jwk.manage createKey done (operator={}, kid={})", operator, saved.kid());
        return saved;
    }

    @Override
    public JwkEntry findByKid(String kid) {
        Objects.requireNonNull(kid, "kid");
        return doFindByKid(kid);
    }

    @Override
    public List<JwkEntry> listKeys() {
        return doList();
    }

    @Override
    public void updateKey(JwkEntry entry) {
        Objects.requireNonNull(entry, "entry");
        JwkEntry existing = doFindByKid(entry.kid());
        if (existing == null) {
            throw new IllegalStateException("JWK kid=" + entry.kid() + " not found");
        }
        List<JwkEntry> projected = projectReplace(entry);
        validateAggregate(projected);

        String operator = currentOperator();
        audit.info("jwk.manage updateKey attempt (operator={}, kid={}, alg={}, status={})",
                operator, entry.kid(), algName(entry), entry.status());
        doUpdate(entry);
        publishChanged(entry.kid(), "update");
        audit.info("jwk.manage updateKey done (operator={}, kid={})", operator, entry.kid());
    }

    @Override
    public void patchKey(JwkEntry entry) {
        Objects.requireNonNull(entry, "entry");
        String kid = (entry.jwk() != null) ? entry.kid() : null;
        if (kid == null) {
            throw new IllegalArgumentException("patchKey requires a kid-bearing entry");
        }
        JwkEntry existing = doFindByKid(kid);
        if (existing == null) {
            throw new IllegalStateException("JWK kid=" + kid + " not found");
        }
        JWK mergedJwk = (entry.jwk() != null) ? entry.jwk() : existing.jwk();
        JwkStatus mergedStatus = (entry.status() != null) ? entry.status() : existing.status();
        JwkEntry merged = new JwkEntry(mergedJwk, mergedStatus);

        List<JwkEntry> projected = projectReplace(merged);
        validateAggregate(projected);

        String operator = currentOperator();
        audit.info("jwk.manage patchKey attempt (operator={}, kid={}, alg={}, status={})",
                operator, merged.kid(), algName(merged), mergedStatus);
        doPatch(merged);
        publishChanged(merged.kid(), "patch");
        audit.info("jwk.manage patchKey done (operator={}, kid={})", operator, merged.kid());
    }

    @Override
    public void deleteByKid(String kid) {
        Objects.requireNonNull(kid, "kid");
        JwkEntry existing = doFindByKid(kid);
        if (existing == null) {
            throw new IllegalStateException("JWK kid=" + kid + " not found");
        }
        if (existing.status() != JwkStatus.RETIRED) {
            throw new IllegalStateException("kid=" + kid + " cannot be deleted while status="
                    + existing.status() + "; retire it first");
        }
        String operator = currentOperator();
        audit.info("jwk.manage delete attempt (operator={}, kid={})", operator, kid);
        doDelete(kid);
        publishChanged(kid, "delete");
        audit.info("jwk.manage delete done (operator={}, kid={})", operator, kid);
    }

    // ---- cross-entry validation ----

    /**
     * Enforce the cross-entry invariants of the {@link JwkManageService}
     * contract over {@code entries} (typically the post-write projection):
     * each algorithm carries at most one {@link JwkStatus#ACTIVE ACTIVE}
     * entry, every {@code ACTIVE} or {@link JwkStatus#PENDING PENDING} entry
     * carries a private key, and {@link JwkEntry#kid() kid} values are
     * unique.
     *
     * @param entries the projected entry set
     * @throws IllegalStateException when any invariant is violated
     */
    protected static void validateAggregate(List<JwkEntry> entries) {
        Objects.requireNonNull(entries, "entries");
        Set<String> seenKids = new HashSet<>();
        Map<String, String> activeByAlg = new HashMap<>();
        for (JwkEntry entry : entries) {
            String kid = entry.kid();
            if (!seenKids.add(kid)) {
                throw new IllegalStateException("duplicate kid: " + kid);
            }
            JwkStatus status = entry.status();
            if (status == JwkStatus.ACTIVE || status == JwkStatus.PENDING) {
                if (!entry.hasPrivateKey()) {
                    throw new IllegalStateException("kid=" + kid + " in status " + status
                            + " requires a private key");
                }
            }
            if (status == JwkStatus.ACTIVE) {
                String alg = algName(entry);
                if (alg == null) {
                    throw new IllegalStateException("ACTIVE kid=" + kid + " has no algorithm");
                }
                String prev = activeByAlg.put(alg, kid);
                if (prev != null) {
                    throw new IllegalStateException("at most one ACTIVE key per algorithm ("
                            + alg + ") is allowed; found kid=" + prev + " and kid=" + kid);
                }
            }
        }
    }

    // ---- helpers ----

    /**
     * Fire-and-forget notification that the JWK storage has changed. Listener
     * exceptions are not recovered here &mdash; Spring's event multicaster
     * handles dispatch errors &mdash; but we still wrap the call so a
     * misconfigured multicaster cannot surface as a failed write.
     */
    private void publishChanged(String kid, String cause) {
        try {
            this.publisher.publishEvent(new JwkRepositoryChangedEvent(this, kid, cause));
        }
        catch (Throwable ex) {
            log.warn("Failed to publish JwkRepositoryChangedEvent (kid={}, cause={}): {}",
                    kid, cause, ex.getMessage());
        }
    }

    private List<JwkEntry> projectReplace(JwkEntry replacement) {
        List<JwkEntry> current = doList();
        List<JwkEntry> projected = new ArrayList<>(current.size());
        boolean replaced = false;
        for (JwkEntry e : current) {
            if (e.kid().equals(replacement.kid())) {
                projected.add(replacement);
                replaced = true;
            }
            else {
                projected.add(e);
            }
        }
        if (!replaced) {
            // Should not happen because callers pre-check existence, but be
            // defensive: fall through to the aggregate validator which will
            // surface the mismatch uniformly.
            projected.add(replacement);
        }
        return projected;
    }

    private static String algName(JwkEntry entry) {
        return (entry.jwk() != null && entry.jwk().getAlgorithm() != null)
                ? entry.jwk().getAlgorithm().getName() : null;
    }

    /**
     * Resolve the current operator from
     * {@link org.springframework.security.core.context.SecurityContextHolder}.
     * Shields callers from {@code NoClassDefFoundError} when Spring Security is absent.
     */
    protected static String currentOperator() {
        try {
            org.springframework.security.core.Authentication authentication =
                    org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return "anonymous";
            }
            String name = authentication.getName();
            return (name == null || name.isBlank()) ? "anonymous" : name;
        }
        catch (Throwable ex) {
            return "anonymous";
        }
    }
}
