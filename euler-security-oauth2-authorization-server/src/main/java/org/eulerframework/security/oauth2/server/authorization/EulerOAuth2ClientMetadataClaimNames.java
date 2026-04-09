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

package org.eulerframework.security.oauth2.server.authorization;

public class EulerOAuth2ClientMetadataClaimNames {

    /**
     * {@code registration_id} - the Euler-specific internal registration identifier for the client.
     */
    public static final String REGISTRATION_ID = "registration_id";

    /**
     * {@code jwks} - the client's JSON Web Key Set document containing the client's public keys.
     * Mutually exclusive with {@code jwks_uri}.
     *
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc7591#section-2">RFC 7591 §2</a>
     */
    public static final String JWKS = "jwks";

    /**
     * {@code tls_client_auth_subject_dn} - the expected subject distinguished name of the
     * client certificate used for {@code tls_client_auth} authentication.
     *
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc8705#section-2.1.2">RFC 8705 §2.1.2</a>
     */
    public static final String TLS_CLIENT_AUTH_SUBJECT_DN = "tls_client_auth_subject_dn";

    /**
     * {@code tls_client_certificate_bound_access_tokens} - indicates whether the client
     * supports mutual-TLS certificate-bound access tokens.
     *
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc8705#section-3.1">RFC 8705 §3.1</a>
     */
    public static final String TLS_CLIENT_CERTIFICATE_BOUND_ACCESS_TOKENS = "tls_client_certificate_bound_access_tokens";

    protected EulerOAuth2ClientMetadataClaimNames() {
    }
}
