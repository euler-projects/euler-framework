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
 * Thrown by {@link DelegatingOtpChannel} when the requested channel is not
 * registered in its routing table and no fallback channel is configured.
 * <p>
 * Maps to the {@code unsupported_channel} HTTP error in
 * {@code OtpTicketIssueEndpointFilter}.
 */
public class OtpChannelNotFoundException extends OtpDeliveryException {

    public OtpChannelNotFoundException(String channel) {
        super("No OtpChannel registered for name '" + channel + "' and no fallback configured");
    }
}
