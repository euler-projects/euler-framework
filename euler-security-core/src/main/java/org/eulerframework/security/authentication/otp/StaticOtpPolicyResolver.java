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
 * {@link OtpPolicyResolver} that always returns the same configured
 * {@link OtpPolicy} regardless of request shape. Suitable for the common case
 * where one global policy applies to all OTP traffic.
 * <p>
 * Applications that need to differentiate policy by channel / purpose / user
 * should provide their own {@link OtpPolicyResolver} bean to override this
 * default.
 */
public class StaticOtpPolicyResolver implements OtpPolicyResolver {

    private final OtpPolicy policy;

    public StaticOtpPolicyResolver(OtpPolicy policy) {
        Assert.notNull(policy, "policy must not be null");
        this.policy = policy;
    }

    @Override
    public OtpPolicy resolve(OtpIssueRequest request) {
        return this.policy;
    }
}
