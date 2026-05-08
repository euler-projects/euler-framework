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

import com.nimbusds.jose.jwk.JWK;

/**
 * Mutable view of a JWK managed by a {@link JwkManageService} implementation.
 * Concrete types typically double as the persistence entity (JPA, Mongo,
 * etc.), which is why the interface is kept intentionally thin: it surfaces
 * only the three framework-level fields that are needed to project the
 * entry back into a {@link JwkEntry} snapshot.
 */
public interface ManagedJwk {

    /** Stable key identifier; unique within the owning store. */
    String getKid();

    /** Underlying Nimbus {@link JWK} (public or private depending on status). */
    JWK getJwk();

    /** Lifecycle status tracked outside the key material itself. */
    JwkStatus getStatus();

    /**
     * Hydrate this instance from an immutable {@link JwkEntry} snapshot.
     * Used by {@link JwkManageService#createJwk(JwkEntry)} and
     * {@link JwkManageService#updateJwk(JwkEntry)} adapters so the same
     * persistence entity can be reused for create/update flows.
     */
    void reloadJwkEntry(JwkEntry jwkEntry);
}
