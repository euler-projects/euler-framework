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

import java.util.List;
import java.util.Objects;

/**
 * {@link JwkRepository} bridge that delegates every call to an application-
 * supplied {@link JwkManageService}. This class itself is deliberately free
 * of persistence concerns: encryption envelopes, SQL access, transactions and
 * cross-entry validation all belong to the {@link JwkManageService}
 * implementation (typically one that extends
 * {@link AbstractJwkManageService}).
 *
 * <p>The autoconfigure layer wires this repository whenever a
 * {@link JwkManageService} bean is present. Bootstrap seeding of
 * pre-configured keys (from
 * {@code euler.security.oauth2.authorizationserver.jwk.keys}) is performed by
 * autoconfig by calling {@link #save(JwkEntry)} per entry; the bridge
 * internally dispatches to the service's strict CRUDL
 * {@link JwkManageService#createKey(JwkEntry) createKey} /
 * {@link JwkManageService#updateKey(JwkEntry) updateKey} based on an
 * existence check. The exists-then-write sequence is not transactional, so
 * the race window between the two service calls is accepted &mdash; matching
 * the conventional {@code RegisteredClientRepository.save} implementation.
 * Callers should rely on the underlying service's primary-key uniqueness
 * constraint as the authoritative guarantee.
 */
public class PersistentJwkRepository implements JwkRepository {

    private final JwkManageService manageService;

    public PersistentJwkRepository(JwkManageService manageService) {
        this.manageService = Objects.requireNonNull(manageService, "manageService");
    }

    @Override
    public List<JwkEntry> load() {
        return this.manageService.listKeys();
    }

    @Override
    public JwkEntry save(JwkEntry entry) {
        Objects.requireNonNull(entry, "entry");
        JwkEntry existing = this.manageService.findByKid(entry.kid());
        if (existing == null) {
            return this.manageService.createKey(entry);
        }
        this.manageService.updateKey(entry);
        return entry;
    }
}
