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
 * Thrown when a {@link UserAuthenticationFactor} cannot be located by id for
 * the authenticated user.
 * <p>
 * The {@code /user/identities} endpoint filter maps this exception to HTTP
 * {@code 404 Not Found}. As an information-disclosure precaution it is also
 * raised when the factor exists but belongs to a different user — the API
 * never distinguishes "not yours" from "not found".
 */
public class UserAuthenticationFactorNotFoundException extends RuntimeException {

    private final String factorId;

    public UserAuthenticationFactorNotFoundException(String factorId) {
        super("No user authentication factor found with id '" + factorId + "'");
        this.factorId = factorId;
    }

    /**
     * The factor id that was not found.
     */
    public String getFactorId() {
        return this.factorId;
    }
}
