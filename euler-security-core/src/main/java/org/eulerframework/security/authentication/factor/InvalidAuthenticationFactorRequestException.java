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
package org.eulerframework.security.authentication.factor;

/**
 * Thrown by {@link UserAuthenticationService#bind} when the supplied form
 * parameters cannot be processed: a required parameter is missing, malformed
 * or the underlying verification artefact (e.g. an OTP ticket, an OAuth2
 * authorization code) is invalid or expired.
 * <p>
 * The {@code /user/identities} endpoint filter maps this exception to HTTP
 * {@code 400 Bad Request} with the {@code invalid_request} error code; the
 * exception message is surfaced as {@code error_description}, so callers
 * <strong>must not</strong> include sensitive content (raw OTP, server-side
 * stack details, ...).
 */
public class InvalidAuthenticationFactorRequestException extends RuntimeException {

    public InvalidAuthenticationFactorRequestException(String message) {
        super(message);
    }

    public InvalidAuthenticationFactorRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
