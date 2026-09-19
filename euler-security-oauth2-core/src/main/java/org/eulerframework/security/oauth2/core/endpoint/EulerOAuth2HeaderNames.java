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

package org.eulerframework.security.oauth2.core.endpoint;

/**
 * Names of the HTTP header fields used by OAuth 2.0 Attestation-Based Client Authentication.
 * <p>
 * Kept apart from {@link EulerOAuth2ParameterNames} because in OAuth 2.0 vocabulary a
 * <i>parameter</i> is carried in the request URI query component or in the
 * {@code application/x-www-form-urlencoded} body, whereas these values are carried in header
 * fields and must be read with {@code getHeader}, never {@code getParameter}. Spring Security's
 * own {@code OAuth2ParameterNames} likewise contains no header names. The first two follow
 * <a href="https://datatracker.ietf.org/doc/html/draft-ietf-oauth-attestation-based-client-auth">
 * draft-ietf-oauth-attestation-based-client-auth</a>; the rest extend that family for the Apple
 * App Attest variant.
 * <p>
 * Each name serves a second purpose on the server side: it is also the key under which the value
 * is stored in {@code additionalParameters}, onto which the converters normalize both the header
 * carriage and the deprecated form carriage. It is therefore also the label that reaches the
 * client inside an {@code invalid_client_attestation} error description, which names the header a
 * request got wrong. For both reasons these strings must stay identical to the wire header names.
 *
 * @see EulerOAuth2ParameterNames
 */
public final class EulerOAuth2HeaderNames {

    /**
     * Client Attestation JWT header.
     */
    public static final String OAUTH_CLIENT_ATTESTATION = "OAuth-Client-Attestation";

    /**
     * Client Attestation Proof-of-Possession data header.
     */
    public static final String OAUTH_CLIENT_ATTESTATION_POP = "OAuth-Client-Attestation-PoP";

    /**
     * Custom extension: Client Attestation type identifier.
     * Defaults to {@link org.eulerframework.security.oauth2.core.EulerOAuth2ClientAttestationType#JWT} when absent.
     */
    public static final String OAUTH_CLIENT_ATTESTATION_TYPE = "OAuth-Client-Attestation-Type";

    /**
     * Apple App Attest variant: the key identifier. Required, since an assertion carries no key
     * identifier of its own.
     * <p>
     * Used by the token endpoint and by the dynamic client registration endpoint, whose JSON body
     * leaves no room for these values.
     */
    public static final String OAUTH_CLIENT_ATTESTATION_KID = "OAuth-Client-Attestation-Kid";

    /**
     * Apple App Attest variant: the one-time challenge, in its raw form. It is the nonce input to
     * {@code attestKey} and {@code generateAssertion}, which take its SHA-256 digest.
     */
    public static final String OAUTH_CLIENT_ATTESTATION_CHALLENGE = "OAuth-Client-Attestation-Challenge";

    /**
     * Apple App Attest variant: the Base64-encoded assertion, proving possession of an
     * already-registered App Attest key.
     * <p>
     * Its presence also selects the header carriage at the token endpoint: a request carrying it
     * is read entirely from headers and never from the deprecated form parameters.
     */
    public static final String OAUTH_CLIENT_ATTESTATION_ASSERTION = "OAuth-Client-Attestation-Assertion";

}
