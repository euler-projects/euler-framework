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

import java.security.SecureRandom;

/**
 * {@link OtpGenerator} that draws each digit independently using
 * {@link SecureRandom}, producing a numeric OTP of the requested length.
 * <p>
 * For a {@code length} of {@code 6} the output range is
 * {@code "000000"} - {@code "999999"} (with leading zeros preserved as part
 * of the string).
 */
public class SecureRandomOtpGenerator implements OtpGenerator {

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generate(int length) {
        Assert.isTrue(length > 0, "length must be positive");
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(this.secureRandom.nextInt(10));
        }
        return sb.toString();
    }
}
