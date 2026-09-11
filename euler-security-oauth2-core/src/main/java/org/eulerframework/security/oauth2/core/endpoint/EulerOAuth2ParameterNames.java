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

public final class EulerOAuth2ParameterNames {
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

    /**
     * Apple App Attest key identifier, submitted as a token endpoint form parameter.
     *
     * @deprecated use the {@link #OAUTH_CLIENT_ATTESTATION_KID} header; see {@link #ATTESTATION}
     * for the retirement of the whole form parameter vocabulary.
     */
    @Deprecated
    public static final String KEY_ID = "kid";

    /**
     * Base64-encoded Apple App Attest attestation object, submitted as a token endpoint form
     * parameter to register the device key and authenticate the client in a single request.
     * <p>
     * <b>This is the single place describing the retirement of the Apple App Attest form
     * parameter vocabulary at the token endpoint</b>, which covers this constant together with
     * {@link #KEY_ID}, {@link #ASSERTION} and {@link #CHALLENGE}. The concepts do not go away:
     * an assertion remains the only accepted proof and a challenge remains its required nonce.
     * What is retired is carrying them as form parameters, in favour of the
     * {@code OAuth-Client-Attestation-*} headers.
     *
     * @deprecated device registration belongs to the dedicated registration endpoint
     * ({@code POST /app_attest/register}) and the OAuth2 flow is assertion-only, so no header
     * analog exists for an attestation. Retained because released STATIC clients still register
     * here, and removed together with the {@code app_assertion} grant and the device-to-user
     * mapping — the only flows that need an attestation at the token endpoint.
     */
    @Deprecated
    public static final String ATTESTATION = "attestation";

    /**
     * Base64-encoded Apple App Attest assertion object, submitted as a token endpoint form
     * parameter to prove possession of an already-registered device key.
     *
     * @deprecated use the {@link #OAUTH_CLIENT_ATTESTATION_ASSERTION} header; see
     * {@link #ATTESTATION}.
     */
    @Deprecated
    public static final String ASSERTION = "assertion";

    /**
     * One-time challenge issued by {@code POST /oauth2/challenge}, submitted as a token endpoint
     * form parameter in its raw form. It is the nonce input to {@code attestKey} and
     * {@code generateAssertion}, which take its SHA-256 digest.
     *
     * @deprecated use the {@link #OAUTH_CLIENT_ATTESTATION_CHALLENGE} header; see
     * {@link #ATTESTATION}.
     */
    @Deprecated
    public static final String CHALLENGE = "challenge";

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
     * Apple App Attest variant: the key identifier carried in an HTTP header. Required, since an
     * assertion carries no key identifier of its own.
     * <p>
     * Used by the token endpoint and by the dynamic client registration endpoint, whose JSON body
     * leaves no room for these values.
     */
    public static final String OAUTH_CLIENT_ATTESTATION_KID = "OAuth-Client-Attestation-Kid";

    /**
     * Apple App Attest variant: the one-time challenge carried in an HTTP header, in its raw form.
     * It is the nonce input to {@code attestKey} and {@code generateAssertion}, which take its
     * SHA-256 digest.
     */
    public static final String OAUTH_CLIENT_ATTESTATION_CHALLENGE = "OAuth-Client-Attestation-Challenge";

    /**
     * Apple App Attest variant: the Base64-encoded assertion, proving possession of an
     * already-registered device key, carried in an HTTP header.
     * <p>
     * Its presence also selects the header carriage at the token endpoint: a request carrying it
     * is read entirely from headers and never from the deprecated form parameters.
     */
    public static final String OAUTH_CLIENT_ATTESTATION_ASSERTION = "OAuth-Client-Attestation-Assertion";

    public static final String ADDITIONAL_SECURITY_SIGNAL = "additional-security-signal";

    /**
     * Ticket id returned by {@code POST /otp/tickets}; used as the credential
     * pointer in the {@code grant_type=otp} token request.
     */
    public static final String OTP_TICKET = "otp_ticket";

    /**
     * The one-time password value the end-user typed back from the chosen
     * {@code OtpChannel} delivery (e.g. SMS).
     */
    public static final String OTP = "otp";
}
