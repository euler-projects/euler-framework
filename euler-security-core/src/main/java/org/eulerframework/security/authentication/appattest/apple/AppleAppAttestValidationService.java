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

package org.eulerframework.security.authentication.appattest.apple;

import org.eulerframework.security.authentication.appattest.AppAttestUser;
import org.eulerframework.security.authentication.appattest.AppAttestAttestationRegistration;
import org.springframework.security.core.AuthenticationException;

/**
 * Service interface for validating Apple App Attest attestation and assertion objects.
 * <p>
 * Implementations are responsible for CBOR decoding, certificate chain verification,
 * nonce validation (attestation), and signature verification (assertion) according to
 * Apple's App Attest documentation.
 * <p>
 * Both methods return {@link AppAttestAttestationRegistration} containing the validated cryptographic
 * data. Callers are responsible for constructing {@link AppAttestUser} or performing
 * user lookup/creation as needed.
 *
 * @see <a href="https://developer.apple.com/documentation/devicecheck/validating-apps-that-connect-to-your-server">
 *     Validating Apps That Connect to Your Server</a>
 */
public interface AppleAppAttestValidationService {

    /**
     * Validate an App Attest attestation object (initial device KEY registration) and
     * return the validated registration data.
     * <p>
     * The key identifier is <b>derived from the attestation itself</b> (Apple sets the
     * {@code credentialId} embedded in the attestation equal to the key ID), so it is not
     * supplied by the caller and cannot be spoofed. On success a new registration is
     * persisted under the derived key ID.
     * <p>
     * The operation is <b>idempotent</b>: the attestation is always fully validated
     * against the (one-time) challenge first, and if the key is already registered the
     * existing registration is returned instead of being re-created. A captured
     * attestation cannot be replayed because its nonce is bound to the already-consumed
     * challenge; this path only succeeds for a genuinely re-attested key, which Apple
     * permits when the first attestation response was lost.
     *
     * @param attestation the Base64-encoded attestation object
     * @param challenge   the challenge used when generating the attestation
     * @return the validated registration, or the existing one if the key was already registered
     * @throws AuthenticationException if validation fails
     */
    AppAttestAttestationRegistration validateAttestation(String attestation, String challenge) throws AuthenticationException;

    /**
     * Validate an App Attest assertion object (re-authentication with a registered device)
     * and return the registration data.
     * <p>
     * Unlike an attestation, an assertion's authenticator data does <b>not</b> embed the
     * credential ID, so the {@code keyId} must be supplied by the caller to locate the
     * registered public key.
     *
     * @param keyId     the key identifier of the previously registered device
     * @param assertion the Base64-encoded assertion object
     * @param challenge the challenge used when generating the assertion
     * @return the registration associated with the validated assertion
     * @throws AuthenticationException if validation fails
     */
    AppAttestAttestationRegistration validateAssertion(String keyId, String assertion, String challenge) throws AuthenticationException;
}
