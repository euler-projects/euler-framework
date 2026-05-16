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
package org.eulerframework.security.authentication.factor;

import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Optional;

/**
 * SPI exposing the lifecycle of a single <em>kind</em> of authentication
 * factor (phone, email, WeChat, passkey, ...).
 * <p>
 * One bean per factor type. Each implementation is fully responsible for the
 * factor it owns — input validation, downstream verification (OTP, OAuth2
 * exchange, attestation, ...), persistence and read access. The framework
 * does not impose a repository abstraction on top of this SPI; persistence
 * is an implementation detail of each factor.
 * <p>
 * Naming and design follow Spring's
 * {@code DelegatingPasswordEncoder} pattern: the framework operates against
 * a single {@code UserAuthenticationService} entry-point, while business
 * code is free to register as many backing services as needed (one per
 * factor type). Routing from a logical factor name to the right
 * implementation is the job of {@link DelegatingUserAuthenticationService},
 * which is itself a {@code UserAuthenticationService}.
 *
 * @see DelegatingUserAuthenticationService
 * @see UserAuthenticationFactor
 */
public interface UserAuthenticationService {

    /**
     * Logical name of the factor type this service handles, matching the
     * {@code factor_type} value submitted by the client when binding.
     * <p>
     * Implementations registered with
     * {@link DelegatingUserAuthenticationService} must return a stable,
     * non-empty value that is unique across all registered implementations.
     *
     * @return the factor type, never {@code null} or empty
     */
    String factorType();

    /**
     * Bind a new authentication factor to the given user.
     * <p>
     * {@code params} carries the raw form parameters submitted to
     * {@code POST /user/identities}; each implementation defines which keys
     * it requires (e.g. the phone factor expects {@code otp_ticket} and
     * {@code otp}). Missing or invalid parameters should be reported via
     * {@link InvalidAuthenticationFactorRequestException}; an attempt to
     * bind a credential that is already taken should be reported via
     * {@link IdentifierConflictException}.
     *
     * @param userId the id of the authenticated user the factor is being
     *               bound to; never {@code null}
     * @param params the raw bind parameters; never {@code null}
     * @return the freshly bound factor, never {@code null}
     */
    UserAuthenticationFactor bind(String userId, MultiValueMap<String, String> params);

    /**
     * Look up a factor owned by {@code userId}.
     * <p>
     * Implementations <strong>must</strong> scope the lookup to {@code userId}
     * — never return a factor belonging to a different user. When this
     * service does not own the factor (typical when called via
     * {@link DelegatingUserAuthenticationService}'s short-circuit fan-out),
     * return {@link Optional#empty()}; do not throw.
     *
     * @param userId the id of the authenticated user; never {@code null}
     * @param id     the factor id; never {@code null}
     * @return the factor if owned by this service and the user, otherwise
     * {@link Optional#empty()}
     */
    Optional<UserAuthenticationFactor> findById(String userId, String id);

    /**
     * List all factors of this service's type that are bound to
     * {@code userId}.
     * <p>
     * Order is implementation-defined; callers that need a stable ordering
     * (e.g. the {@code /user/identities} endpoint) are expected to sort the
     * aggregated result themselves.
     *
     * @param userId the id of the authenticated user; never {@code null}
     * @return a possibly empty list of factors, never {@code null}
     */
    List<UserAuthenticationFactor> findAllByUserId(String userId);

    /**
     * Delete the factor with the given id, if it is owned by {@code userId}
     * and this service.
     * <p>
     * When the factor is not owned by this service, implementations
     * <strong>must</strong> return silently rather than throw; this is the
     * contract that lets {@link DelegatingUserAuthenticationService} fan
     * delete requests out across all registered services and only raise
     * {@link UserAuthenticationFactorNotFoundException} when none of them
     * matched. When the factor is owned by this service but not by the
     * given user, implementations <strong>must</strong> also return
     * silently — exposing a 404 vs 403 distinction here would leak factor
     * ownership.
     *
     * @param userId the id of the authenticated user; never {@code null}
     * @param id     the factor id to delete; never {@code null}
     */
    void deleteById(String userId, String id);
}
