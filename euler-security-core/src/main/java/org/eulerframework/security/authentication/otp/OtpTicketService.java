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
 * Implementations persist issued tickets and later verify and consume them:
 * a successfully verified ticket must never be reusable, and failed attempts
 * must count towards the failure ceiling.
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
     * Verification succeeds only when the ticket exists, has neither expired
     * nor been consumed, the supplied {@code otp} matches the stored value,
     * and - when {@code expectedPurpose} is non-{@code null} - the stored
     * purpose equals it. On success the ticket must be invalidated atomically;
     * on failure the failure counter must be incremented and the ticket
     * discarded once the failure ceiling is reached.
     *
     * @param ticketId        the ticket id presented by the caller
     * @param otp             the one-time password value submitted by the user
     * @param expectedPurpose purpose that must match the stored purpose;
     *                        {@code null} skips the check
     * @return an {@link OtpVerification} on success, or {@code null} when the
     *         ticket is unknown, expired or mismatched
     */
    OtpVerification consume(String ticketId, String otp, String expectedPurpose);
}
