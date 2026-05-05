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

import org.eulerframework.security.oauth2.server.authorization.jwk.source.ReloadableJwkSource;
import org.springframework.context.ApplicationEvent;

import java.util.Objects;

/**
 * Advisory event emitted by a {@link JwkManageService} whenever the underlying
 * JWK storage has been mutated by a CRUDL write (create / update / patch /
 * delete). Listeners are expected to trigger a
 * {@link ReloadableJwkSource#reload() reload} (or an equivalent refresh) on a
 * best-effort basis.
 *
 * <h2>Delivery semantics</h2>
 * The contract between publisher and listeners is deliberately one-way:
 * <ul>
 *   <li>The publisher MUST NOT block on, inspect, or depend on any listener
 *       outcome. In particular, no cluster-convergence waiting is implied.</li>
 *   <li>Listeners MUST isolate their own failures &mdash; a reload exception
 *       on one node is not an error condition for the write itself. The write
 *       has already committed to the repository before this event is
 *       published, so downstream reload failure never rolls back a mutation.</li>
 * </ul>
 */
public class JwkRepositoryChangedEvent extends ApplicationEvent {

    private final String kid;
    private final String cause;

    /**
     * @param source the publishing {@link JwkManageService}
     * @param kid    the primary {@code kid} affected by the write, or
     *               {@code null} if the mutation does not target a single key
     * @param cause  short free-form label for the originating operation (e.g.
     *               {@code "create"}, {@code "update"}, {@code "patch"},
     *               {@code "delete"})
     */
    public JwkRepositoryChangedEvent(Object source, String kid, String cause) {
        super(Objects.requireNonNull(source, "source"));
        this.kid = kid;
        this.cause = Objects.requireNonNull(cause, "cause");
    }

    /** Affected {@code kid}, or {@code null} when the write is not scoped to one key. */
    public String getKid() {
        return this.kid;
    }

    /** Short label describing the originating operation. */
    public String getCause() {
        return this.cause;
    }
}
