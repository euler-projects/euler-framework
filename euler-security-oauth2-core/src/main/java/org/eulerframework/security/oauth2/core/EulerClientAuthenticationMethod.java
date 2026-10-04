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

package org.eulerframework.security.oauth2.core;

import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

public class EulerClientAuthenticationMethod {

    /**
     * Attestation-based JWT client authentication method as registered in
     * <a href="https://www.ietf.org/archive/id/draft-ietf-oauth-attestation-based-client-auth-11.html#section-15.6">
     * Section 15.6</a> of the draft.
     */
    public static final ClientAuthenticationMethod ATTEST_JWT_CLIENT_AUTH = new ClientAuthenticationMethod(
            "attest_jwt_client_auth");

    /**
     * Attestation-based client authentication using an Apple App Attest assertion as the proof of
     * possession, instead of the draft's Client Attestation PoP JWT.
     * <p>
     * Section 5 of the draft expects a specification defining an additional proof of possession
     * mechanism to register its own token endpoint authentication method value, analogous to
     * {@code attest_jwt_client_auth} and {@code attest_jwt_client_auth_dpop}. This value follows
     * that shape but is <b>not IANA registered</b>: registration is Specification Required and this
     * variant has no public specification, so the value is private to this deployment.
     */
    public static final ClientAuthenticationMethod ATTEST_APPATTEST_CLIENT_AUTH = new ClientAuthenticationMethod(
            "attest_appattest_client_auth");

    /**
     * Whether the given method is one of the attestation-based client authentication methods, i.e.
     * {@link #ATTEST_JWT_CLIENT_AUTH} or {@link #ATTEST_APPATTEST_CLIENT_AUTH}. Useful where the
     * variant is not yet known, such as before the credential has been verified.
     *
     * @param method the client authentication method to test
     * @return {@code true} if the method is attestation-based
     */
    public static boolean isAttestationBased(ClientAuthenticationMethod method) {
        return ATTEST_JWT_CLIENT_AUTH.equals(method) || ATTEST_APPATTEST_CLIENT_AUTH.equals(method);
    }
}
