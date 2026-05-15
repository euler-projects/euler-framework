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
 * Strategy that delivers an OTP value to a recipient via a specific channel
 * (SMS, email, push notification, ...).
 * <p>
 * The framework treats the <em>channel name</em> as a side-effect of bean
 * registration rather than a property of the channel itself: implementations
 * are expected to be registered as Spring beans whose <em>bean name</em> serves
 * as the channel identifier (e.g. {@code @Bean("sms") OtpChannel sms()}).
 * Routing from a logical channel name to the right implementation is the job
 * of {@link DelegatingOtpChannel}.
 *
 * @see DelegatingOtpChannel
 * @see StdoutOtpChannel
 */
@FunctionalInterface
public interface OtpChannel {

    /**
     * Deliver the OTP described by {@code delivering} to its recipient.
     *
     * @param delivering the delivery instruction
     * @throws OtpDeliveryException if delivery cannot be performed
     */
    void send(OtpDelivering delivering) throws OtpDeliveryException;
}
