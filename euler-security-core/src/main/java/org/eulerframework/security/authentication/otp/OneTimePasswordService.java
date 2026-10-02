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
package org.eulerframework.security.authentication.otp;

/**
 * Persistence SPI for {@link OneTimePassword}s.
 * <p>
 * Mirrors the shape of Spring Security's {@code OneTimeTokenService}: a
 * request object goes in and the persisted ticket comes out of
 * {@link #generate(GenerateOneTimePasswordRequest)}, while the authentication token
 * itself goes into {@link #consume(OneTimePasswordAuthenticationToken)} and
 * the consumed ticket comes back out. Implementations persist issued tickets
 * and later verify and consume them: a successfully verified ticket must never
 * be reusable, and failed attempts must count towards the failure ceiling.
 *
 * @see InMemoryOneTimePasswordService
 * @see JdbcOneTimePasswordService
 * @see RedisOneTimePasswordService
 */
public interface OneTimePasswordService {

    /**
     * Mint, persist and return a fresh ticket for the given request. The OTP
     * value is generated internally unless the request carries a fixed one.
     *
     * @param request the generation input
     * @return the persisted ticket, including its id and OTP value
     */
    OneTimePassword generate(GenerateOneTimePasswordRequest request);

    /**
     * Atomically verify and consume the ticket presented by an authentication
     * token.
     * <p>
     * Verification succeeds only when the ticket exists, has neither expired
     * nor been consumed, and the submitted OTP matches the stored value. On
     * success the ticket must be invalidated atomically; on failure the
     * failure counter must be incremented and the ticket discarded once the
     * failure ceiling is reached.
     *
     * @param authentication the unauthenticated request token carrying the
     *                       ticket id (principal) and the submitted OTP
     * @return the consumed {@link OneTimePassword} on success, or {@code null}
     *         when the ticket is unknown, expired or mismatched
     */
    OneTimePassword consume(OneTimePasswordAuthenticationToken authentication);
}
