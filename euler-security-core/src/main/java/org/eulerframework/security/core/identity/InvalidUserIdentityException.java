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
package org.eulerframework.security.core.identity;

/**
 * Indicates that the arguments describing a user identity are missing,
 * malformed, or otherwise unprocessable, so no valid {@link UserIdentity}
 * can be produced.
 *
 * <p>The exception message is propagated to callers and
 * <strong>must not</strong> include sensitive content such as raw OTP
 * values or internal implementation details.
 */
public class InvalidUserIdentityException extends RuntimeException {

    public InvalidUserIdentityException(String message) {
        super(message);
    }

    public InvalidUserIdentityException(String message, Throwable cause) {
        super(message, cause);
    }
}
