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
 * Result of a successful
 * {@link OtpTicketService#consume(String, String, String, String)} call.
 *
 * @param ticketId   the ticket that was consumed
 * @param channel    channel the OTP was originally delivered through
 * @param recipient  the recipient the OTP was originally delivered to
 * @param purpose    business purpose label associated with the ticket
 * @param verifiedAt server-side timestamp at which the ticket was successfully
 *                   consumed
 */
public record OtpVerification(
        String ticketId,
        String channel,
        String recipient,
        String purpose,
        Instant verifiedAt) {

    public OtpVerification {
        Assert.hasText(ticketId, "ticketId must not be empty");
        Assert.hasText(channel, "channel must not be empty");
        Assert.hasText(recipient, "recipient must not be empty");
        Assert.notNull(verifiedAt, "verifiedAt must not be null");
    }
}
