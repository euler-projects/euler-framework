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
 * {@link OtpPolicyResolver#resolve(OtpIssueRequest)} so that policy lookup may
 * differentiate by channel / purpose / identity if business rules require so.
 * <p>
 * Exactly one of {@code recipient} and {@code identityId} is expected to be
 * non-null (this two-of-one rule is enforced earlier by the request converter).
 *
 * @param channel             logical channel name (e.g. {@code sms},
 *                            {@code email})
 * @param recipient           explicit recipient supplied by the caller
 *                            (phone number, email, ...) - may be {@code null}
 *                            when {@code identityId} is supplied
 * @param identityId          identity factor id used to look up the recipient
 *                            via {@link OtpRecipientResolver} - may be
 *                            {@code null} when {@code recipient} is supplied
 * @param purpose             optional business label, opaque to the framework
 * @param codeChallenge       PKCE code challenge (RFC 7636); may be
 *                            {@code null} when PKCE is disabled (see
 *                            {@code euler.security.otp.pkce.enabled})
 * @param codeChallengeMethod PKCE code challenge method, currently must be
 *                            {@code S256} when present; may be {@code null}
 *                            when {@code codeChallenge} is {@code null}
 */
public record OtpIssueRequest(
        String channel,
        String recipient,
        String identityId,
        String purpose,
        String codeChallenge,
        String codeChallengeMethod) {

    public OtpIssueRequest {
        Assert.hasText(channel, "channel must not be empty");
    }
}
