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

package org.eulerframework.security.core.userdetails;


import org.eulerframework.security.authentication.appattest.AppAttestUser;

/**
 * Service interface for loading and creating user details based on Apple App Attest identities.
 * <p>
 * Implementations are responsible for mapping an {@link AppAttestUser} (identified by
 * a device key ID) to the application's user model.
 */
public interface EulerDeviceUserDetailsService {

    /**
     * Load the user associated with the given Apple App Attest identity.
     *
     * @param appAttestUser the validated App Attest user containing the device key ID
     * @return the user details for the matching user
     * @throws UserDetailsNotFoundException if no user is found for the given key ID
     */
    EulerUserDetails loadUserByDeviceUser(AppAttestUser appAttestUser) throws UserDetailsNotFoundException;

    /**
     * Create a new user account associated with the given Apple App Attest identity.
     * <p>
     * This method is called when auto-user-creation is enabled during the attestation
     * (device registration) flow and no existing user is found.
     *
     * @param appAttestUser the validated App Attest user containing the device key ID
     * @return the newly created user details
     */
    EulerUserDetails createUser(AppAttestUser appAttestUser);

    /**
     * Bind the device identified by {@code appAttestUser} to an EXISTING user.
     * <p>
     * Unlike {@link #createUser(AppAttestUser)}, this method does NOT create a new
     * user; it only persists the {@code device -> user} mapping. It is used by flows
     * (notably the OTP token grant) where the user has already been resolved through
     * a different channel (e.g. by a verified factor binding) and the request also
     * carries a verified App Attest identity that must be associated with that user
     * for future device-based assertions.
     * <p>
     * Implementations <strong>must</strong> reject the call if the {@code keyId} is
     * already mapped to a different user.
     *
     * @param appAttestUser the validated App Attest user containing the device key ID
     * @param userId        the existing user id to associate the device with; never
     *                      {@code null} or empty
     */
    void bindToUser(AppAttestUser appAttestUser, String userId);
}
