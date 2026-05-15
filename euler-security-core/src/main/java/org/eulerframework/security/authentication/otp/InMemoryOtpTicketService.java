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

import org.springframework.util.Assert;

import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory {@link OtpTicketService} backed by a {@link ConcurrentHashMap}.
 * Expired entries are cleaned up lazily on each {@link #save(OtpTicket)} call.
 * <p>
 * Suitable for single-instance deployments and development / testing. For
 * clustered deployments use {@link RedisOtpTicketService} or
 * {@link JdbcOtpTicketService} instead.
 */
public class InMemoryOtpTicketService implements OtpTicketService {

    public static final int DEFAULT_MAX_TICKETS = 10000;
    public static final int DEFAULT_MAX_FAILURES = 5;

    private final Map<String, OtpTicket> tickets = new ConcurrentHashMap<>();
    private final int maxTickets;
    private final int maxFailures;

    public InMemoryOtpTicketService() {
        this(DEFAULT_MAX_TICKETS, DEFAULT_MAX_FAILURES);
    }

    public InMemoryOtpTicketService(int maxTickets, int maxFailures) {
        Assert.isTrue(maxTickets > 0, "maxTickets must be positive");
        Assert.isTrue(maxFailures > 0, "maxFailures must be positive");
        this.maxTickets = maxTickets;
        this.maxFailures = maxFailures;
    }

    @Override
    public void save(OtpTicket ticket) {
        Assert.notNull(ticket, "ticket must not be null");
        cleanupExpired();
        if (this.tickets.size() >= this.maxTickets) {
            throw new IllegalStateException(
                    "Maximum number of active OTP tickets (" + this.maxTickets + ") reached");
        }
        this.tickets.put(ticket.ticketId(), ticket);
    }

    @Override
    public OtpVerification consume(String ticketId, String codeVerifier, String otp, String expectedPurpose) {
        if (ticketId == null) {
            return null;
        }
        OtpTicket ticket = this.tickets.get(ticketId);
        if (ticket == null || ticket.consumed() || Instant.now().isAfter(ticket.expiresAt())) {
            this.tickets.remove(ticketId);
            return null;
        }

        boolean otpMatches = Objects.equals(ticket.otp(), otp);
        boolean pkceMatches = OtpPkceVerifier.verify(codeVerifier, ticket.codeChallenge(), ticket.codeChallengeMethod());
        boolean purposeMatches = expectedPurpose == null
                || Objects.equals(expectedPurpose, ticket.purpose());

        if (otpMatches && pkceMatches && purposeMatches) {
            // Atomic remove on success: a one-time ticket is gone after consumption.
            if (this.tickets.remove(ticketId, ticket)) {
                return new OtpVerification(ticket.ticketId(), ticket.channel(), ticket.recipient(),
                        ticket.purpose(), Instant.now());
            }
            // Lost the race - another caller already consumed/updated this ticket.
            return null;
        }

        // Failure path: bump the failure count, discard the ticket once it
        // reaches the configured ceiling so brute-force attempts cannot hold
        // the slot indefinitely.
        OtpTicket updated = ticket.withFailureIncremented();
        if (updated.failureCount() >= this.maxFailures) {
            this.tickets.remove(ticketId, ticket);
        } else {
            this.tickets.replace(ticketId, ticket, updated);
        }
        return null;
    }

    private void cleanupExpired() {
        Instant now = Instant.now();
        Iterator<Map.Entry<String, OtpTicket>> it = this.tickets.entrySet().iterator();
        while (it.hasNext()) {
            OtpTicket t = it.next().getValue();
            if (t.consumed() || now.isAfter(t.expiresAt())) {
                it.remove();
            }
        }
    }
}
