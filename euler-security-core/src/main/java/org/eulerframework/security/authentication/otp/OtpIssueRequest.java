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

/**
 * Input parameters of an OTP ticket issue request, passed to
 * {@link OtpPolicyResolver} so that policy lookup may differentiate by
 * channel, purpose or identity when business rules require so.
 * <p>
 * Exactly one of {@code recipient} and {@code identityId} is non-null.
 *
 * @param channel    logical channel name (e.g. {@code sms}, {@code email})
 * @param recipient  explicit recipient (phone number, email, ...);
 *                   {@code null} when {@code identityId} is supplied
 * @param identityId identity factor id resolved to a recipient server-side;
 *                   {@code null} when {@code recipient} is supplied
 * @param purpose    optional business label, opaque to the framework
 */
public record OtpIssueRequest(
        String channel,
        String recipient,
        String identityId,
        String purpose) {

    public OtpIssueRequest {
        Assert.hasText(channel, "channel must not be empty");
    }
}
