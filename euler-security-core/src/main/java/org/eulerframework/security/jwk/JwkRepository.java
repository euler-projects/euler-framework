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

import java.util.List;

/**
 * Data source of JWK material. The repository is stateless and caches nothing;
 * every {@link #load()} call MUST reflect the latest underlying state.
 * The calling frequency is driven by the management layer (startup warm-up
 * plus admin-triggered reload paths).
 *
 * <p>{@link #save(JwkEntry)} is offered as an upsert contract to simplify
 * bootstrap seeding (the autoconfigure layer iterates over pre-configured
 * entries and calls {@code save} per entry, remaining race-safe and
 * idempotent across restarts). Higher-level lifecycle semantics (status
 * transitions, physical deletion, key generation) are owned by
 * {@code JwkManageService}; the persistent bridge implementation dispatches
 * {@code save} into the service's strict CRUDL methods
 * ({@code createKey} / {@code updateKey}) rather than exposing a raw upsert
 * on the service itself.
 */
public interface JwkRepository {

    /**
     * Return all currently known JWK entries (any lifecycle {@link JwkStatus}).
     * Ordering is unspecified. Implementations must not cache across calls.
     *
     * @return immutable snapshot of every persisted entry
     */
    List<JwkEntry> load();

    void save(JwkEntry entry);

    String fingerprint();
}
