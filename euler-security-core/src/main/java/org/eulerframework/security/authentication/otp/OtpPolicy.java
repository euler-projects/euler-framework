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
 * Static configuration applied to a single OTP issuance.
 * <p>
 * Resolution of an {@code OtpPolicy} for a given request is delegated to
 * {@link OtpPolicyResolver}. The framework intentionally does not interpret
 * {@link OtpIssueRequest#purpose()} or enforce channel/purpose whitelists -
 * such policies, if needed, are implemented by application-supplied resolvers.
 *
 * @param otpLength   length of the OTP value to generate (e.g. {@code 6})
 * @param expiresIn   how long the OTP / ticket remains valid
 * @param retryAfter  advisory minimum interval before requesting a new OTP -
 *                    currently surfaced to clients as a hint only, not enforced
 *                    by the server
 * @param maxFailures maximum number of failed consume attempts before the
 *                    ticket is invalidated
 */
public record OtpPolicy(
        int otpLength,
        Duration expiresIn,
        Duration retryAfter,
        int maxFailures) {

    public OtpPolicy {
        Assert.isTrue(otpLength > 0, "otpLength must be positive");
        Assert.notNull(expiresIn, "expiresIn must not be null");
        Assert.notNull(retryAfter, "retryAfter must not be null");
        Assert.isTrue(maxFailures > 0, "maxFailures must be positive");
    }
}
