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
 * Persistence SPI for {@link OtpTicket}s.
 * <p>
 * An implementation is expected to:
 * <ol>
 *     <li>{@link #save(OtpTicket) Persist} a freshly-issued ticket so that it
 *         can be looked up later during verification.</li>
 *     <li>{@link #consume(String, String, String, String) Consume} a ticket
 *         when its holder presents the OTP value back. A correctly verified
 *         ticket must be removed (or marked as consumed) atomically so it
 *         cannot be reused; failed attempts must increment {@code failureCount}
 *         and discard the ticket once {@code maxFailures} is exceeded.</li>
 * </ol>
 *
 * @see InMemoryOtpTicketService
 * @see JdbcOtpTicketService
 * @see RedisOtpTicketService
 */
public interface OtpTicketService {

    /**
     * Persist a newly issued ticket. Implementations may choose any storage
     * with a TTL aligned to {@link OtpTicket#expiresAt()}.
     *
     * @param ticket the ticket to persist
     */
    void save(OtpTicket ticket);

    /**
     * Atomically verify and consume a ticket.
     * <p>
     * The verification must check, in this order:
     * <ol>
     *     <li>The ticket exists and has not expired or been previously
     *         consumed.</li>
     *     <li>The supplied {@code otp} matches the stored OTP value.</li>
     *     <li>The supplied {@code codeVerifier}, when transformed using the
     *         ticket's {@code codeChallengeMethod} (currently {@code S256}),
     *         matches the stored {@code codeChallenge} (PKCE, RFC 7636).</li>
     *     <li>If {@code expectedPurpose} is non-{@code null} it must equal the
     *         ticket's stored purpose. A {@code null} value means "do not
     *         enforce".</li>
     * </ol>
     * On any failure the implementation must increment the failure counter
     * and, once {@link OtpPolicy#maxFailures()} is reached, invalidate the
     * ticket. On success the ticket must be marked consumed atomically.
     *
     * @param ticketId        the ticket id presented by the caller
     * @param codeVerifier    PKCE {@code code_verifier} value
     * @param otp             the one-time password value submitted by the user
     * @param expectedPurpose optional purpose that must match the ticket's
     *                        stored purpose; pass {@code null} to skip the check
     * @return an {@link OtpVerification} on success, or {@code null} if the
     *         ticket is unknown / expired / mismatched
     */
    OtpVerification consume(String ticketId, String codeVerifier, String otp, String expectedPurpose);
}
