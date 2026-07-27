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

/**
 * Server-side state of a single OTP issuance, persisted by an
 * {@link OtpTicketService} and consumed at most once when the ticket holder
 * presents the OTP value back to the server.
 *
 * @param ticketId     opaque, URL-safe ticket identifier (e.g. {@code ot_xxxxxxxx})
 * @param channel      logical delivery channel name (e.g. {@code sms}, {@code email})
 * @param recipient    channel-addressable delivery target (phone number, email, ...)
 * @param purpose      optional, opaque business label; not interpreted by the framework
 * @param otp          the one-time password value as plain text
 * @param expiresAt    when this ticket becomes invalid
 * @param failureCount number of failed consume attempts so far
 * @param consumed     whether this ticket has already been consumed
 */
public record OtpTicket(
        String ticketId,
        String channel,
        String recipient,
        String purpose,
        String otp,
        Instant expiresAt,
        int failureCount,
        boolean consumed) {

    public OtpTicket {
        Assert.hasText(ticketId, "ticketId must not be empty");
        Assert.hasText(channel, "channel must not be empty");
        Assert.hasText(recipient, "recipient must not be empty");
        Assert.hasText(otp, "otp must not be empty");
        Assert.notNull(expiresAt, "expiresAt must not be null");
        Assert.isTrue(failureCount >= 0, "failureCount must not be negative");
    }

    /**
     * Returns a copy of this ticket with {@code failureCount} incremented by one.
     */
    public OtpTicket withFailureIncremented() {
        return new OtpTicket(this.ticketId, this.channel, this.recipient, this.purpose,
                this.otp, this.expiresAt, this.failureCount + 1, this.consumed);
    }

    /**
     * Returns a copy of this ticket marked as consumed.
     */
    public OtpTicket markConsumed() {
        return new OtpTicket(this.ticketId, this.channel, this.recipient, this.purpose,
                this.otp, this.expiresAt, this.failureCount, true);
    }
}
