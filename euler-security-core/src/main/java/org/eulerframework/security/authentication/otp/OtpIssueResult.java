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
 * Outcome of a successful OTP ticket issuance, returned by the issue endpoint
 * to the caller.
 * <p>
 * {@code retryAfter} is currently only a hint for clients - the framework does
 * not enforce rate limiting on the server side in this iteration.
 *
 * @param otpTicket  the opaque ticket id presented by the caller during
 *                   verification
 * @param expiresIn  how long the ticket / OTP remains valid
 * @param retryAfter advisory minimum interval before the caller may request a
 *                   new OTP
 */
public record OtpIssueResult(
        String otpTicket,
        Duration expiresIn,
        Duration retryAfter) {

    public OtpIssueResult {
        Assert.hasText(otpTicket, "otpTicket must not be empty");
        Assert.notNull(expiresIn, "expiresIn must not be null");
        Assert.notNull(retryAfter, "retryAfter must not be null");
    }
}
