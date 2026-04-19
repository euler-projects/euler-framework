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

package org.eulerframework.security.authentication.appattest;

/**
 * Service interface for storing and retrieving device attestation registrations.
 * <p>
 * Registrations are created during the attestation (device registration) process
 * and used during assertion (device re-authentication) to verify the device's identity.
 *
 * @see DeviceAppAttestationRegistration
 */
public interface DeviceAppAttestationRegistrationService {

    /**
     * Save a registration after successful attestation.
     *
     * @param registration the registration to save
     */
    void saveRegistration(DeviceAppAttestationRegistration registration);

    /**
     * Retrieve a registration by its key ID.
     *
     * @param keyId the key identifier
     * @return the registration, or {@code null} if not found
     */
    DeviceAppAttestationRegistration findByKeyId(String keyId);

    /**
     * Update the sign count for a registration.
     * <p>
     * Implementations should ensure atomicity and verify that the new sign count
     * is greater than the stored value to prevent replay attacks.
     *
     * @param keyId        the key identifier
     * @param newSignCount the new sign count value
     */
    void updateSignCount(String keyId, long newSignCount);
}
