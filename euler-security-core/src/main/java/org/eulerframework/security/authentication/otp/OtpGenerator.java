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
 * Strategy that produces the OTP value to be sent to the recipient.
 * <p>
 * The default implementation, {@link SecureRandomOtpGenerator}, generates a
 * numeric code of the requested length using {@link java.security.SecureRandom}.
 */
@FunctionalInterface
public interface OtpGenerator {

    /**
     * Generate an OTP value of the specified length.
     *
     * @param length the length of the OTP (e.g. {@code 6})
     * @return the generated OTP value
     */
    String generate(int length);
}
