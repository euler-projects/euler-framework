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
 * Indicates that the per-type unique key being bound is already taken
 * &mdash; either by another user, or by the same user under a different
 * identity record.
 *
 * <p>To avoid enumeration of existing bindings, the message
 * <strong>must not</strong> include the conflicting value or the
 * owning user id.
 */
public class IdentityOccupiedException extends RuntimeException {

    private final String identityType;

    public IdentityOccupiedException(String identityType) {
        super("The submitted value is already bound to an existing "
                + identityType + " identity");
        this.identityType = identityType;
    }

    public IdentityOccupiedException(String identityType, String message) {
        super(message);
        this.identityType = identityType;
    }

    /**
     * The identity type that detected the conflict.
     */
    public String getIdentityType() {
        return this.identityType;
    }
}
