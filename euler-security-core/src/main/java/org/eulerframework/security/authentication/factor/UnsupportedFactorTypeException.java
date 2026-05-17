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
 * Thrown by a business-layer composite {@link UserAuthenticationFactorService}
 * router when the requested factor type value (carried over the wire as
 * {@code identity_type}) does not match any configured backend.
 * <p>
 * The {@code /user/identities} endpoint filter maps this exception to HTTP
 * {@code 400 Bad Request} with the {@code unsupported_identity_type} error
 * code.
 */
public class UnsupportedFactorTypeException extends RuntimeException {

    private final String factorType;

    public UnsupportedFactorTypeException(String factorType) {
        super("Unsupported factor_type '" + factorType + "': "
                + "no backend is configured for it");
        this.factorType = factorType;
    }

    /**
     * The factor_type value supplied by the client that no service matched.
     */
    public String getFactorType() {
        return this.factorType;
    }
}
