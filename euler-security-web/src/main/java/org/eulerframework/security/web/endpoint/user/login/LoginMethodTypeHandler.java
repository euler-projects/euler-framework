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
package org.eulerframework.security.web.endpoint.user.login;

import java.util.Map;

/**
 * SPI that translates a login-method declaration under
 * {@code euler.security.web.login-methods.<name>} into a
 * {@link LoginMethodView} for the shared login page.
 *
 * <p>Implementations are registered as Spring beans, one per
 * {@code type} value (e.g. {@code oauth2}, {@code otp},
 * {@code passkey}); a generic dispatcher delegates each declared entry
 * to the handler whose {@link #type()} matches.
 *
 * <p>This SPI covers view production only. Filter-chain wiring,
 * success handlers and other runtime beans are configured by each
 * type's own module.
 */
public interface LoginMethodTypeHandler {

    /**
     * The stable identifier used as the {@code type} value in the YAML
     * (e.g. {@code "oauth2"}), matched case-sensitively against
     * {@code euler.security.web.login-methods.<name>.type}.
     * Never {@code null} nor empty.
     */
    String type();

    /**
     * Produces the view model for the login method named {@code name}.
     *
     * <p>Returns {@code null} when the declaration cannot currently be
     * resolved (e.g. it references an unknown OAuth2 client
     * registration); the dispatcher skips such entries without
     * rendering them.
     *
     * @param name       the login-method key under
     *                   {@code euler.security.web.login-methods};
     *                   serves as the default for omitted properties
     *                   such as {@code display-name}.
     * @param properties the type-specific properties bag; never
     *                   {@code null}, may be empty.
     * @return the view to render, or {@code null} to skip this entry.
     */
    LoginMethodView toView(String name, Map<String, Object> properties);
}
