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
 * <em>App instance registration</em> apart from <em>assertion verification</em>. Both artifacts
 * resolve to the same verified App instance registration, so without this discriminator the two
 * cases are indistinguishable once the request reaches a grant provider.
 * <p>
 * The distinction matters wherever a request may establish persistent state: only an
 * {@link #ATTESTATION} request may fix a relationship between a key and a user, since an
 * {@link #ASSERTION} request merely proves possession of an already-registered key.
 *
 * @see EulerOAuth2ClientAttestationType
 * @deprecated compatibility logic; see
 * {@link org.eulerframework.security.core.userdetails.EulerDeviceUserDetailsService}. Removed once
 * the device-to-user mapping is retired.
 */
@Deprecated
public enum EulerClientAttestationProof {

    /**
     * The request submitted attestation data ({@code OAuth-Client-Attestation} for the JWT
     * variant, or the {@code attestation} parameter for Apple App Attest), registering or
     * re-registering the App Attest key.
     */
    ATTESTATION,

    /**
     * The request submitted only a proof of possession ({@code OAuth-Client-Attestation-PoP}
     * for the JWT variant, or the {@code assertion} parameter for Apple App Attest) against
     * an already-registered App Attest key.
     */
    ASSERTION
}
