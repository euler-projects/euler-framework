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

public class ManagedJwkRepository implements JwkRepository {

    private final JwkManageService manageService;

    public ManagedJwkRepository(JwkManageService manageService) {
        this(manageService, null);
    }

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
