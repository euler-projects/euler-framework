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

import org.springframework.security.core.AuthenticationException;

/**
 * Thrown by {@link OtpTicketIssueAuthenticationProvider} when an
 * {@code identity_id} was supplied but no {@link OtpRecipientResolver} bean is
 * registered, or the resolver returned no recipient / threw an exception.
 * Surfaced to clients as the {@code invalid_identity_id} error.
 */
public class OtpInvalidIdentityIdException extends AuthenticationException {

    public OtpInvalidIdentityIdException(String message) {
        super(message);
    }

    public OtpInvalidIdentityIdException(String message, Throwable cause) {
        super(message, cause);
    }
}
