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

import java.util.List;

/**
 * Service interface for loading and creating user details based on Apple App Attest identities.
 * <p>
 * Implementations are responsible for mapping an {@link AppAttestUser} (identified by
 * a device key ID) to the application's user model, and persist that mapping (conventionally
 * in an {@code app_attest_attestation_user_mapping} table).
 * <p>
 * <b>This is the single place describing the deprecation of the device-to-user association.</b>
 * Members, implementations and the call sites that keep them alive carry only a pointer here.
 * <p>
 * The entire interface exists to maintain a <em>fixed</em> association between a device key and
 * a user. That association is pure compatibility support for clients released before App Attest
 * was reduced to a device-level proof, and it is retired in two stages:
 * <ul>
 *   <li>{@link #createUser} serves only the deprecated {@code app_assertion} grant, which
 *       JIT-provisions an anonymous user; it goes first, with that grant.</li>
 *   <li>{@link #bindToUser} and {@link #loadUserByDeviceUser} serve the OTP grant, which binds
 *       the device to the OTP-resolved user on an attestation request and rejects a mismatch
 *       afterwards.</li>
 * </ul>
 * Going forward App Attest is a <b>device and client proof only</b>: it authenticates the OAuth
 * client, never the user. A token request must therefore layer a factor that identifies a user
 * (a user grant such as OTP, or {@code refresh_token}); the device key contributes no user
 * identity and is never bound to one.
 *
 * @deprecated compatibility support for released clients that rely on a device key identifying a
 * user. Removed, together with the mapping table, once those clients are retired; from then on a
 * key is never associated with a user.
 */
@Deprecated
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
     * This method is called by the {@code app_assertion} grant when just-in-time provisioning
     * is enabled and no existing user is found for the key. It is only reached for a token
     * request that presented an <b>attestation</b>, which registers the device key; the
     * device registration endpoint itself creates no user.
     *
     * @param appAttestUser the validated App Attest user containing the device key ID
     * @param authorities   authorities to grant, supplied by the caller's just-in-time
     *                      provisioning policy; never empty. Implementations must
     *                      persist these verbatim rather than choosing authorities
     *                      themselves.
     * @return the newly created user details
     */
    EulerUserDetails createUser(AppAttestUser appAttestUser, List<String> authorities);

    /**
     * Bind the device identified by {@code appAttestUser} to an EXISTING user.
     * <p>
     * Unlike {@link #createUser(AppAttestUser, List)}, this method does NOT create a
     * new user; it only persists the {@code device -> user} mapping. It is used by flows
     * (notably the OTP token grant) where the user has already been resolved through
     * a different channel (e.g. by a verified factor binding) and the request also
     * carries a verified App Attest identity that must be associated with that user
     * for future device-based assertions.
     * <p>
     * Implementations <strong>must</strong> reject the call if the {@code keyId} is
     * already mapped to a different user.
     * <p>
     * Callers <strong>must</strong> only invoke this for a request that presented an
     * <b>attestation</b>. A request carrying only an <b>assertion</b> proves possession of an
     * already-registered key but does not fix that key to a user, so it neither binds nor
     * creates; it may only read an association established earlier.
     *
     * @param appAttestUser the validated App Attest user containing the device key ID
     * @param userId        the existing user id to associate the device with; never
     *                      {@code null} or empty
     */
    void bindToUser(AppAttestUser appAttestUser, String userId);
}
