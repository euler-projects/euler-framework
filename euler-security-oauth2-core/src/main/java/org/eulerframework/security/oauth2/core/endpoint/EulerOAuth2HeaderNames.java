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
 * draft-ietf-oauth-attestation-based-client-auth</a>;
 * {@link #OAUTH_CLIENT_ATTESTATION_TYPE} is a deprecated extension of that family, retained only to
 * recognize released clients.
 * <p>
 * The Apple App Attest variant deliberately has <b>no</b> constants here: it carries its credential
 * in the App Attest business domain's own names &mdash; the {@code App-Attest-*} headers or the
 * {@code app_attest_*} form parameters &mdash; rather than in OAuth-specific headers, so the two
 * business domains share one vocabulary for the same Apple payload.
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
     *
     * @deprecated the variant is no longer declared but resolved from the credential carriage: an
     * Apple App Attest credential (the {@code App-Attest-*} headers or the {@code app_attest_*} form
     * parameters) selects the Apple variant, anything else selects the draft's standard JWT variant.
     * Retained solely as the discriminator for released clients, which carry the Apple credential in
     * the deprecated {@code attestation} / {@code assertion} / {@code challenge} / {@code kid} form
     * parameters &mdash; names too generic to be told apart from an unrelated form parameter, so
     * carriage cannot identify them. Those clients only call the token endpoint. Removed together
     * with {@link EulerOAuth2ParameterNames#ATTESTATION}.
     */
    @Deprecated
    public static final String OAUTH_CLIENT_ATTESTATION_TYPE = "OAuth-Client-Attestation-Type";

}
