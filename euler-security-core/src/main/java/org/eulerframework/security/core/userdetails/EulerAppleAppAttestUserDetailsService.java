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

import org.eulerframework.security.authentication.apple.AppleAppAttestUser;

/**
 * Service interface for loading and creating user details based on Apple App Attest identities.
 * <p>
 * Implementations are responsible for mapping an {@link AppleAppAttestUser} (identified by
 * a device key ID) to the application's user model.
 */
public interface EulerAppleAppAttestUserDetailsService {

    /**
     * Load the user associated with the given Apple App Attest identity.
     *
     * @param attestUser the validated App Attest user containing the device key ID
     * @return the user details for the matching user
     * @throws UserDetailsNotFountException if no user is found for the given key ID
     */
    EulerUserDetails loadUserByAppleAppAttestUser(AppleAppAttestUser attestUser) throws UserDetailsNotFountException;

    /**
     * Create a new user account associated with the given Apple App Attest identity.
     * <p>
     * This method is called when auto-user-creation is enabled during the attestation
     * (device registration) flow and no existing user is found.
     *
     * @param attestUser the validated App Attest user containing the device key ID
     * @return the newly created user details
     */
    EulerUserDetails createUser(AppleAppAttestUser attestUser);
}
