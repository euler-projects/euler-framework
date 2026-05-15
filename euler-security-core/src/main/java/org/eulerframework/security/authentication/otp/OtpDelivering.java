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

import java.time.Duration;

/**
 * Carrier passed from {@link DelegatingOtpChannel} to a concrete
 * {@link OtpChannel} implementation that actually delivers the OTP value.
 * <p>
 * Intentionally trimmed: the channel only needs to know <em>what</em> to send
 * ({@code otp}), <em>where</em> to send it ({@code recipient}), and enough
 * context to render a sensible message ({@code purpose}, {@code expiresIn}).
 * The server-side ticket id is intentionally <strong>not</strong> exposed to
 * channel implementations - it is a verification artefact, not a delivery
 * concern.
 *
 * @param channel   logical channel name (e.g. {@code sms}, {@code email})
 * @param recipient channel-addressable target string (phone number, email,
 *                  push token, ...)
 * @param purpose   optional opaque business label - channels may use it to
 *                  pick a template or wording
 * @param otp       the one-time password value to deliver
 * @param expiresIn how long the OTP remains valid - useful for rendering
 *                  messages such as {@code "valid for 5 minutes"}
 */
public record OtpDelivering(
        String channel,
        String recipient,
        String purpose,
        String otp,
        Duration expiresIn) {

    public OtpDelivering {
        Assert.hasText(channel, "channel must not be empty");
        Assert.hasText(recipient, "recipient must not be empty");
        Assert.hasText(otp, "otp must not be empty");
        Assert.notNull(expiresIn, "expiresIn must not be null");
    }
}
