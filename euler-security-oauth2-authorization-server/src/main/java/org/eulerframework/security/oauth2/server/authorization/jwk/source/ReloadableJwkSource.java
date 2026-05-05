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

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.eulerframework.security.oauth2.server.authorization.jwk.JwkRepository;

/**
 * Runtime JWK source that exposes a single management hook on top of Nimbus
 * {@link JWKSource}. Spring Authorization Server binds to a single
 * {@code JWKSource<SecurityContext>} bean; implementations of this interface
 * satisfy that contract while also offering {@link #reload()} so the admin
 * layer can re-project the {@link JwkRepository} into the in-memory
 * {@code JWKSet}.
 * <p>
 * Rotation is driven entirely by mutations to the authoritative repository
 * (status transitions, inserts, deletes). There is no direct "sign with" /
 * "retire" API on this interface &mdash; the {@link LiveState} content
 * fingerprint, recomputed during {@link #reload()}, is the single source of
 * truth for signing selection. This keeps the contract minimal and makes the
 * clustered implementation converge on repository state rather than on
 * a separately-maintained coordinator payload.
 */
public interface ReloadableJwkSource extends JWKSource<SecurityContext> {

    /**
     * Re-read the authoritative {@link JwkRepository} and rebuild the in-memory
     * {@code JWKSet}. Implementations MUST be idempotent: a reload that
     * produces the same content fingerprint as the previous one is a no-op
     * from the perspective of event publication and (in the clustered case)
     * cross-node coordination.
     */
    void reload();
}
