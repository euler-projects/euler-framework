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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link OtpChannel} that prints the OTP to the application log instead of
 * delivering it to a real recipient.
 * <p>
 * Intended as a {@code DelegatingOtpChannel#fallback} during development /
 * integration testing - never ship an unconditional registration of this bean
 * as a routed channel in production.
 */
public class StdoutOtpChannel implements OtpChannel {

    private static final Logger logger = LoggerFactory.getLogger(StdoutOtpChannel.class);

    @Override
    public void send(OtpDelivering delivering) {
        String message = String.format(
                "[OTP] channel=%s recipient=%s purpose=%s otp=%s expires-in=%s",
                delivering.channel(),
                delivering.recipient(),
                delivering.purpose(),
                delivering.otp(),
                delivering.expiresIn());
        logger.info(message);
        // Also write to stdout so demos without a configured logger still see it.
        System.out.println(message);
    }
}
