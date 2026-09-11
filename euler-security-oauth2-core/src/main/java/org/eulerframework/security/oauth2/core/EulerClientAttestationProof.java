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

/**
 * The kind of proof a client presented in a single Attestation-Based Client
 * Authentication request.
 * <p>
 * A request may carry attestation data, a proof of possession, or both; this type records
 * which of them the server actually acted on, so that downstream components can tell
 * <em>device registration</em> apart from <em>device verification</em>. Both artifacts
 * resolve to the same verified device registration, so without this discriminator the two
 * cases are indistinguishable once the request reaches a grant provider.
 * <p>
 * The distinction matters wherever a request may establish persistent state. Notably, the
 * device-to-user association backing the {@code app_assertion} compatibility grant is only
 * ever written for {@link #ATTESTATION} requests: an {@link #ASSERTION} request proves
 * possession of an already-registered key but does not, on its own, fix a relationship
 * between that key and a user.
 *
 * @see EulerOAuth2ClientAttestationType
 */
public enum EulerClientAttestationProof {

    /**
     * The request submitted attestation data ({@code OAuth-Client-Attestation} for the JWT
     * variant, or the {@code attestation} parameter for Apple App Attest), registering or
     * re-registering the device key.
     */
    ATTESTATION,

    /**
     * The request submitted only a proof of possession ({@code OAuth-Client-Attestation-PoP}
     * for the JWT variant, or the {@code assertion} parameter for Apple App Attest) against
     * an already-registered device key.
     */
    ASSERTION
}
