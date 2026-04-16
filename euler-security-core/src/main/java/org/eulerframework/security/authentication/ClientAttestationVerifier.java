/*
 * Copyright 2013-2024 the original author or authors.
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
package org.eulerframework.security.authentication;

import java.security.PublicKey;

import org.springframework.security.core.AuthenticationException;

/**
 * Verifies a Client Attestation JWT (Section 5.1 of
 * draft-ietf-oauth-attestation-based-client-auth-08) and returns the
 * confirmation key ({@code cnf} claim).
 * <p>
 * Implement this interface when a reliable Client Attester (backend issuer)
 * is available to sign Client Attestation JWTs. The Client Attestation JWT
 * contains the {@code sub} claim (client_id) and the {@code cnf} claim
 * (client instance public key).
 * <p>
 * This is an <b>optional</b> dependency for {@code ClientAttestationFilter}.
 * If no bean of this type is found in the {@code ApplicationContext}, the
 * filter will skip Client Attestation JWT verification and rely solely on
 * PoP verification (kid-based lookup from {@code AppAttestRegistrationService}).
 *
 * @see <a href="https://www.ietf.org/archive/id/draft-ietf-oauth-attestation-based-client-auth-08.html#section-5.1">
 *     Section 5.1 – Client Attestation JWT</a>
 */
public interface ClientAttestationVerifier {

    /**
     * Verify the given Client Attestation JWT and extract the client instance
     * public key from its {@code cnf} claim.
     *
     * @param attestationJwt the Client Attestation JWT
     * @return the public key from the {@code cnf} claim
     * @throws AuthenticationException if verification fails (e.g., invalid
     *         signature, expired, malformed)
     */
    PublicKey verifyClientAttestation(String attestationJwt) throws AuthenticationException;
}
