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
package org.eulerframework.security.jwk;

import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;

/**
 * {@link JwkRepository} adapter that bridges the stateless repository
 * contract onto a lifecycle-aware {@link JwkManageService} implementation.
 *
 * <p>The adapter keeps the two abstractions separated: the management
 * service owns the persistence store and enforces strict
 * {@code create}/{@code update} semantics, while the repository continues to
 * expose the idempotent upsert that bootstrap code and Nimbus sources
 * expect. {@link #save(JwkEntry)} therefore probes the service for an
 * existing {@code kid} and dispatches to the right strict method.
 *
 * <p>Seed entries supplied through the two-argument constructor are
 * upserted eagerly in iteration order so every restart converges the store
 * to a deterministic baseline.
 */
public class ManagedJwkRepository implements JwkRepository {

    private final JwkManageService manageService;

    public ManagedJwkRepository(JwkManageService manageService) {
        this(manageService, null);
    }

    /**
     * @param manageService   lifecycle-aware backend; never {@code null}
     * @param initialEntries  seed entries applied via {@link #save(JwkEntry)}
     *                        in iteration order; may be {@code null} or empty
     */
    public ManagedJwkRepository(JwkManageService manageService, Collection<JwkEntry> initialEntries) {
        Assert.notNull(manageService, "manageService is required");
        this.manageService = manageService;
        if (!CollectionUtils.isEmpty(initialEntries)) {
            for (JwkEntry entry : initialEntries) {
                this.save(entry);
            }
        }
    }

    @Override
    public List<JwkEntry> load() {
        return this.manageService.listJwks()
                .stream()
                .map(ManagedJwkRepository::toJwkEntry)
                .toList();
    }

    @Override
    public void save(JwkEntry jwkEntry) {
        Assert.notNull(jwkEntry, "JWK entry is required");
        ManagedJwk existing = this.manageService.getJwk(jwkEntry.kid());
        if (existing == null) {
            this.manageService.createJwk(jwkEntry);
        } else {
            this.manageService.updateJwk(jwkEntry);
        }
    }

    @Override
    public String fingerprint() {
        return this.manageService.getFingerprint();
    }

    private static JwkEntry toJwkEntry(ManagedJwk managedJwk) {
        return new JwkEntry(managedJwk.getJwk(), managedJwk.getStatus());
    }
}
