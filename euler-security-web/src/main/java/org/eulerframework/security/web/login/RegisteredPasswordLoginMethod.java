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
package org.eulerframework.security.web.login;

/**
 * A registered {@code password} login method: the traditional
 * username/password form login, served by
 * {@link PasswordLoginMethodHandler}.
 *
 * <p>The type has no settings of its own. It establishes no identity
 * either: credentials are verified against the local user store.
 */
public final class RegisteredPasswordLoginMethod extends RegisteredLoginMethod {

    /** The {@code method-type} this registration is declared under. */
    public static final String TYPE = "password";

    /**
     * @param id      the identifier this registration is stored under
     * @param name    the name clients address this method by
     * @param primary whether the method belongs to the primary group
     */
    public RegisteredPasswordLoginMethod(String id, String name, boolean primary) {
        super(TYPE, id, name, null, primary);
    }
}
