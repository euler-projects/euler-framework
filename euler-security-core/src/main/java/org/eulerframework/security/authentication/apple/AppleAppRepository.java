/*
 * Copyright 2013-2026 the original author or authors.
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

package org.eulerframework.security.authentication.apple;

/**
 * Repository for managing registered Apple Apps used in Apple App Attest validation.
 * <p>
 * Implementations must support lookup by the SHA-256 hash of the App ID
 * ({@code teamId.bundleId}), since the attestation and assertion authenticator data
 * only contains the RP ID hash, not the original App ID.
 *
 * @see RegisteredAppleApp
 * @see DefaultAppleAppAttestValidationService
 */
public interface AppleAppRepository {

    /**
     * Find a registered Apple App by the SHA-256 hash of its App ID.
     * <p>
     * The App ID is computed as {@code teamId.bundleId}. The attestation and assertion
     * authenticator data contains only the RP ID hash (SHA-256 of App ID), so this method
     * enables lookup without knowing the original App ID.
     *
     * @param appIdHash the SHA-256 hash of the App ID (32 bytes)
     * @return the registered Apple App, or {@code null} if no matching app is found
     */
    RegisteredAppleApp findByAppIdHash(byte[] appIdHash);
}
