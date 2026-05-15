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

/**
 * Strategy that resolves the {@link OtpPolicy} to apply to a given
 * {@link OtpIssueRequest}.
 * <p>
 * Implementations may differentiate by any combination of {@code channel},
 * {@code purpose} or identity. The framework deliberately does not interpret
 * {@code purpose} - business policies (such as "purpose X may only use channel
 * Y") are expressed inside this strategy by application code.
 *
 * @see StaticOtpPolicyResolver
 */
@FunctionalInterface
public interface OtpPolicyResolver {

    /**
     * Resolve the policy to apply for the given issue request.
     *
     * @param request the incoming issue request
     * @return the policy to apply, never {@code null}
     */
    OtpPolicy resolve(OtpIssueRequest request);
}
