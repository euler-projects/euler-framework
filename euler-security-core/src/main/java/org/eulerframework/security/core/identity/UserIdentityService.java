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
package org.eulerframework.security.core.identity;

import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Optional;

/**
 * SPI exposing the lifecycle of a single user-identity entry point.
 *
 * <p>An implementation handles exactly one {@link #identityType()} and
 * is responsible for:
 * <ul>
 *   <li>verifying ownership of the underlying business value
 *       (consuming an OTP ticket, exchanging an OAuth2 authorization
 *       code, verifying a passkey assertion, ...);</li>
 *   <li>deriving the persisted per-type unique key (SHA-256 hash,
 *       identity transform, ...) from that verified value;</li>
 *   <li>persisting the identity row and any per-type attributes
 *       (encrypted phone original, federated profile cache, ...);</li>
 *   <li>reverse-resolving an identity from the un-transformed business
 *       value supplied by a caller that already knows it.</li>
 * </ul>
 *
 * <p>The framework consumes a single {@code UserIdentityService} bean
 * regardless of how many identity types a deployment supports;
 * applications that need more than one type are expected to provide a
 * composite implementation that fans out per {@code identity_type}.
 *
 * <p>Lifecycle methods follow the project-wide CRUDL convention
 * ({@code create / get / update / patch / delete / list}<em>UserIdentity</em>);
 * {@link #findUserIdentityByRawSubject(String, String)} is an
 * additional reverse-lookup entry.
 *
 * @see UserIdentity
 */
public interface UserIdentityService {

    /**
     * Conventional key under which the target identity type is carried
     * inside the raw-parameter map. Composite implementations read this
     * key to route to the matching per-type backend; single-type
     * backends typically ignore it.
     */
    String IDENTITY_TYPE_PARAMETER = "identity_type";

    /**
     * Logical identity type handled by this backend.
     *
     * @return a stable, non-empty value such as {@code "phone"},
     *         {@code "email"}, {@code "wechat"}
     */
    String identityType();

    /**
     * Create an identity from raw, <em>unverified</em> parameters.
     *
     * <p>The backend itself performs the per-type verification (OTP
     * consumption, OAuth2 code exchange, passkey assertion check, ...)
     * before persistence. The key set of {@code params} is
     * implementation defined; for example the phone backend expects
     * {@code otp_ticket} and {@code otp}.
     *
     * @param userId id of the authenticated user; never {@code null}
     * @param params raw caller-supplied parameters; never {@code null}
     * @return the freshly persisted identity, never {@code null}
     * @throws InvalidUserIdentityException when parameters are missing
     *         or malformed
     * @throws IdentityOccupiedException    when the value is already
     *         bound to another account
     */
    UserIdentity createUserIdentity(String userId, MultiValueMap<String, String> params);

    /**
     * Create an identity from a <em>pre-verified prototype</em>.
     *
     * <p>Intended for callers that have already verified ownership of
     * the underlying value through another channel. Backends must not
     * perform additional verification.
     *
     * <p>Prototype contract:
     * <ul>
     *   <li>{@link UserIdentity#getIdentityType()} must equal
     *       {@link #identityType()}; otherwise the backend throws
     *       {@link InvalidUserIdentityException}.</li>
     *   <li>{@code identityId}, {@code subject}, {@code userId} and
     *       {@code boundAt} must be unset; the backend mints, derives,
     *       stamps and pairs them.</li>
     *   <li>The raw business value is carried as an extension attribute
     *       under the key the backend uses for its persisted projection
     *       (e.g. {@code "phone"} for the phone backend), attached via
     *       {@link UserIdentity.Builder#property(String, Object)}.</li>
     * </ul>
     *
     * @param userId    id of the user the identity is being bound to;
     *                  never {@code null} or empty
     * @param prototype pre-verified identity prototype; never
     *                  {@code null}
     * @return the freshly persisted identity, never {@code null}
     * @throws InvalidUserIdentityException when the prototype violates
     *         the contract
     * @throws IdentityOccupiedException    when the derived
     *         {@code subject} is already bound to another account
     */
    UserIdentity createUserIdentity(String userId, UserIdentity prototype);

    /**
     * Read an identity owned by {@code userId}.
     *
     * <p>Implementations must scope the lookup to {@code userId} and
     * return {@link Optional#empty()} when the identity is not owned by
     * this backend; a composite caller can then fan a lookup out
     * without prior knowledge of the target type.
     *
     * @param userId     id of the authenticated user; never {@code null}
     * @param identityId identity id; never {@code null}
     * @return the persisted identity, or {@link Optional#empty()}
     */
    Optional<UserIdentity> getUserIdentity(String userId, String identityId);

    /**
     * List all identities of this backend's type owned by {@code userId}.
     *
     * <p>Order is implementation defined; composite callers that
     * aggregate across backends are expected to apply their own stable
     * ordering on the merged result.
     *
     * @param userId id of the authenticated user; never {@code null}
     * @return a possibly empty list of persisted identities, never
     *         {@code null}
     */
    List<UserIdentity> listUserIdentities(String userId);

    /**
     * Replace an existing identity in place.
     *
     * <p>The identity type is preserved; only the per-type business
     * value is replaced (for example, swap one phone number for
     * another). As with
     * {@link #createUserIdentity(String, MultiValueMap)} the backend
     * verifies ownership of the new value before persisting.
     *
     * @param userId     id of the authenticated user; never {@code null}
     * @param identityId identity to replace; never {@code null}
     * @param params     raw caller-supplied parameters; never
     *                   {@code null}
     * @return the updated persisted identity, never {@code null}
     * @throws UserIdentityNotFoundException when the identity does not
     *         exist or is not owned by this user / backend
     * @throws InvalidUserIdentityException  when parameters are missing
     *         or malformed
     * @throws IdentityOccupiedException     when the new value is
     *         already bound to another account
     */
    UserIdentity updateUserIdentity(String userId, String identityId, MultiValueMap<String, String> params);

    /**
     * Delete the identity with the given id, if it is owned by
     * {@code userId} and this backend.
     *
     * <p>When the identity is not owned by this backend, or owned by
     * this backend but not by the user, implementations return
     * silently. A composite caller can then fan a delete out without
     * leaking ownership, and the SPI never distinguishes "not yours"
     * from "not found".
     *
     * @param userId     id of the authenticated user; never {@code null}
     * @param identityId identity to delete; never {@code null}
     */
    void deleteUserIdentity(String userId, String identityId);

    /**
     * Reverse-resolve the identity bound to the given raw subject.
     *
     * <p>The {@code subject = f(rawSubject)} transform is implementation
     * defined and opaque to the caller. For {@code phone} and
     * {@code email} the transform is SHA-256 (after normalisation); for
     * {@code wechat}, {@code apple}, {@code google} it is the identity
     * function on the IdP-issued opaque value.
     *
     * <p>Backends whose {@link #identityType()} does not match the
     * first argument return {@link Optional#empty()}; composite callers
     * use the argument to route to the right per-type backend.
     *
     * @param identityType target identity type; never {@code null} or
     *                     empty
     * @param rawSubject   un-transformed business value (pre-image of
     *                     the persisted {@code subject}); never
     *                     {@code null} or empty
     * @return the persisted identity bound to the value, or
     *         {@link Optional#empty()}
     */
    Optional<UserIdentity> findUserIdentityByRawSubject(String identityType, String rawSubject);
}
