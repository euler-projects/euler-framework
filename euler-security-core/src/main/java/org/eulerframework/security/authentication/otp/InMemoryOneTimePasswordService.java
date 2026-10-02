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
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory {@link OneTimePasswordService} for single-instance deployments and
 * development / testing; expired entries are evicted lazily. For clustered
 * deployments use {@link RedisOneTimePasswordService} or {@link JdbcOneTimePasswordService}
 * instead.
 */
public class InMemoryOneTimePasswordService implements OneTimePasswordService {

    public static final int DEFAULT_MAX_TICKETS = 10000;
    public static final int DEFAULT_MAX_FAILURES = 5;

    private final Map<String, OneTimePassword> tickets = new ConcurrentHashMap<>();
    private final OneTimePasswordGenerator oneTimePasswordGenerator;
    private final int maxTickets;
    private final int maxFailures;

    public InMemoryOneTimePasswordService(OneTimePasswordGenerator oneTimePasswordGenerator) {
        this(oneTimePasswordGenerator, DEFAULT_MAX_TICKETS, DEFAULT_MAX_FAILURES);
    }

    public InMemoryOneTimePasswordService(OneTimePasswordGenerator oneTimePasswordGenerator, int maxTickets, int maxFailures) {
        Assert.notNull(oneTimePasswordGenerator, "oneTimePasswordGenerator must not be null");
        Assert.isTrue(maxTickets > 0, "maxTickets must be positive");
        Assert.isTrue(maxFailures > 0, "maxFailures must be positive");
        this.oneTimePasswordGenerator = oneTimePasswordGenerator;
        this.maxTickets = maxTickets;
        this.maxFailures = maxFailures;
    }

    @Override
    public OneTimePassword generate(GenerateOneTimePasswordRequest request) {
        Assert.notNull(request, "request must not be null");
        String otp = StringUtils.hasText(request.fixedOtp())
                ? request.fixedOtp()
                : this.oneTimePasswordGenerator.generate(request.otpLength());
        OneTimePassword ticket = new OneTimePassword(
                UUID.randomUUID().toString(),
                request.channel(),
                request.recipient(),
                request.purpose(),
                otp,
                Instant.now().plus(request.expiresIn()),
                0,
                false);

        cleanupExpired();
        if (this.tickets.size() >= this.maxTickets) {
            throw new IllegalStateException(
                    "Maximum number of active OTP tickets (" + this.maxTickets + ") reached");
        }
        this.tickets.put(ticket.ticketId(), ticket);
        return ticket;
    }

    @Override
    public OneTimePassword consume(OneTimePasswordAuthenticationToken authentication) {
        Assert.notNull(authentication, "authentication must not be null");
        String ticketId = authentication.getTicketId();
        if (ticketId == null) {
            return null;
        }
        String otp = authentication.getOtp();
        OneTimePassword ticket = this.tickets.get(ticketId);
        if (ticket == null || ticket.consumed() || Instant.now().isAfter(ticket.expiresAt())) {
            this.tickets.remove(ticketId);
            return null;
        }

        if (Objects.equals(ticket.otp(), otp)) {
            // Atomic remove on success: a one-time ticket is gone after consumption.
            if (this.tickets.remove(ticketId, ticket)) {
                return ticket;
            }
            // Lost the race - another caller already consumed/updated this ticket.
            return null;
        }

        // Failure path: bump the failure count, discard the ticket once it
        // reaches the configured ceiling so brute-force attempts cannot hold
        // the slot indefinitely.
        OneTimePassword updated = ticket.withFailureIncremented();
        if (updated.failureCount() >= this.maxFailures) {
            this.tickets.remove(ticketId, ticket);
        } else {
            this.tickets.replace(ticketId, ticket, updated);
        }
        return null;
    }

    private void cleanupExpired() {
        Instant now = Instant.now();
        Iterator<Map.Entry<String, OneTimePassword>> it = this.tickets.entrySet().iterator();
        while (it.hasNext()) {
            OneTimePassword t = it.next().getValue();
            if (t.consumed() || now.isAfter(t.expiresAt())) {
                it.remove();
            }
        }
    }
}
