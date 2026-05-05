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
package org.eulerframework.security.oauth2.server.authorization.jwk.source;

import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.proc.SecurityContext;
import org.eulerframework.security.oauth2.server.authorization.jwk.JwkEntry;
import org.eulerframework.security.oauth2.server.authorization.jwk.JwkManageService;
import org.eulerframework.security.oauth2.server.authorization.jwk.JwkRepository;
import org.eulerframework.security.oauth2.server.authorization.jwk.JwkRepositoryChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Single-node {@link ReloadableJwkSource} intended for non-clustered deployments
 * and tests. {@link #reload()} is driven entirely by the aggregate content
 * fingerprint exposed by {@link LiveState}: when the freshly-projected
 * fingerprint matches the previously committed one the reload is a pure no-op.
 * <p>
 * This class holds no signing-selection shadow state: {@code signingKid},
 * published kid order, and retired kids are all recomputed from the repository
 * during each reload.
 */
public class StandaloneReloadableJwkSource implements ReloadableJwkSource {

    private static final Logger log = LoggerFactory.getLogger(StandaloneReloadableJwkSource.class);

    private final JwkRepository repository;
    private final ApplicationEventPublisher publisher;
    private final ReentrantLock reloadLock = new ReentrantLock();

    private volatile LiveState state;

    public StandaloneReloadableJwkSource(JwkRepository repository, ApplicationEventPublisher publisher) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.publisher = Objects.requireNonNull(publisher, "publisher");
    }

    /** Snapshot of the current live state for tests and management services. */
    public LiveState currentState() {
        return this.state;
    }

    /**
     * React to a {@link JwkRepositoryChangedEvent} on a best-effort basis.
     * Exceptions are logged and swallowed: the publishing
     * {@link JwkManageService} has already committed its write and does not
     * depend on the reload outcome.
     */
    @EventListener
    public void onJwkRepositoryChanged(JwkRepositoryChangedEvent event) {
        try {
            reload();
        }
        catch (Exception ex) {
            log.warn("Reload triggered by JwkRepositoryChangedEvent failed (kid={}, cause={}): {}",
                    event.getKid(), event.getCause(), ex.getMessage());
        }
    }

    @Override
    public void reload() {
        this.reloadLock.lock();
        try {
            List<JwkEntry> entries = this.repository.load();
            LiveState next = LiveState.build(entries);
            LiveState current = this.state;
            if (current != null && current.fingerprint().equals(next.fingerprint())) {
                log.debug("StandaloneReloadableJwkSource reload: fingerprint unchanged ({}), no-op",
                        next.fingerprint());
                return;
            }
            this.state = next;
            log.info("StandaloneReloadableJwkSource reload: fingerprint={} signingKid={} publishedKids={}",
                    next.fingerprint(), next.signingKid(), next.publishedKids());
        }
        finally {
            this.reloadLock.unlock();
        }
    }

    @Override
    public List<JWK> get(JWKSelector jwkSelector, SecurityContext context) throws KeySourceException {
        LiveState snapshot = this.state;
        if (snapshot == null) {
            throw new KeySourceException("StandaloneReloadableJwkSource has not been initialised; "
                    + "call reload() before serving the first request");
        }
        return jwkSelector.select(snapshot.publishedSet());
    }
}
