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
 * SPI implemented per {@code type} value under
 * {@code euler.security.web.login-methods.<name>.type}. Each handler
 * knows how to translate a single login-method declaration (the raw
 * {@code Map<String, Object> properties} bag) into a {@link LoginMethodView}
 * for the shared login page.
 *
 * <p>Registered as a Spring {@code @Bean}. Multiple handlers may
 * coexist &mdash; one per well-known {@code type} (e.g. {@code oauth2},
 * {@code otp}, {@code passkey}). Dispatch happens in the generic
 * {@code LoginMethodConfigDrivenContributor}, which iterates the
 * {@code login-methods} map and delegates each entry to the handler
 * whose {@link #type()} matches.
 *
 * <h2>Scope</h2>
 * View production only. Filter-chain wiring, success handlers,
 * repositories and any other runtime beans are the responsibility of
 * each type's own {@code SecurityConfigurer} / autoconfigure &mdash; the
 * SPI stays intentionally narrow so that a new type can be a "just
 * publish two beans (a handler + a configurer)" affair without
 * expanding this interface.
 *
 * <h2>Contract</h2>
 * <ul>
 *   <li>{@link #type()} returns the stable, lower-case identifier used
 *       as the {@code type} value in the YAML (e.g. {@code "oauth2"}).
 *       Never {@code null} nor empty.</li>
 *   <li>{@link #toView(String, Map)} may return {@code null} to signal
 *       "the declaration is syntactically well-formed but presently
 *       unresolvable" (e.g. an OAuth2 method whose
 *       {@code oauth-client-registration-id} does not resolve to an
 *       existing {@code ClientRegistration}). The dispatcher treats
 *       {@code null} as "skip &mdash; do not render" and logs at
 *       {@code WARN}. This is preferred over throwing so a single
 *       misconfigured entry does not break the whole login page.</li>
 * </ul>
 */
public interface LoginMethodTypeHandler {

    /**
     * The {@code type} value this handler responds to (case-sensitive,
     * matched against
     * {@code euler.security.web.login-methods.<name>.type}).
     */
    String type();

    /**
     * Produce a view model for the login method named {@code name} with
     * the raw {@code properties} bag as declared in configuration.
     *
     * @param name       the login-method key (map key under
     *                   {@code euler.security.web.login-methods}); useful
     *                   as a default for id / display-name / identity-type
     *                   when the {@code properties} bag omits them.
     * @param properties the raw properties map as bound by Spring Boot;
     *                   never {@code null}, may be empty.
     * @return the view to render, or {@code null} to skip this entry.
     */
    LoginMethodView toView(String name, Map<String, Object> properties);
}
