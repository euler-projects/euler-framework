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
 * Thrown by {@link UserAuthenticationService#bind} when the credential the
 * caller is trying to bind is already taken — either by another user, or by
 * the same user under a different factor record.
 * <p>
 * The {@code /user/identities} endpoint filter maps this exception to HTTP
 * {@code 409 Conflict}. To avoid leaking which account owns the conflicting
 * credential the response intentionally omits both the {@code identifier}
 * and the owning user id.
 */
public class IdentifierConflictException extends RuntimeException {

    private final String factorType;

    public IdentifierConflictException(String factorType) {
        super("The submitted identifier is already bound to an existing "
                + factorType + " factor");
        this.factorType = factorType;
    }

    public IdentifierConflictException(String factorType, String message) {
        super(message);
        this.factorType = factorType;
    }

    /**
     * The factor type that detected the conflict.
     */
    public String getFactorType() {
        return this.factorType;
    }
}
