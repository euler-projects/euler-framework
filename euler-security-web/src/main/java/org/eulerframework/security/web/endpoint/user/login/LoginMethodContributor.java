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

import java.util.List;

/**
 * SPI implemented by any module that adds a selectable login method to
 * the shared login page (e.g. OAuth2 / OIDC redirect, Passkey /
 * WebAuthn, phone OTP, WeChat, ...).
 *
 * <p>The framework's {@code EulerSecurityUserPageController} aggregates
 * every {@code LoginMethodContributor} bean in the application context,
 * concatenates their contributions, and exposes the flattened list to
 * the login template as {@code loginMethods}. The template dispatches
 * per {@link LoginMethodView#type()} to render each entry.
 *
 * <p>Design notes:
 * <ul>
 *   <li>Contributors are the only extension seam &mdash; there is no
 *       central registry, no {@code euler.security.login.methods} list.
 *       New login methods opt in simply by publishing a bean.</li>
 *   <li>Enabling a login method (property toggles, dependency checks,
 *       registration allowlists, etc.) is the contributor's own
 *       responsibility &mdash; its module owns the corresponding
 *       {@code @ConfigurationProperties} and gates the bean
 *       accordingly. The controller never inspects enablement.</li>
 *   <li>Ordering across contributors is controlled by Spring's
 *       {@code @Order} / {@code Ordered}. Ordering <em>within</em> a
 *       contributor's returned list is preserved verbatim, then broken
 *       by {@link LoginMethodView#order()} across the flattened list.</li>
 *   <li>Password / username form is <strong>not</strong> a contribution;
 *       it is the login page's primary content and always renders (the
 *       form itself is disabled by omitting the module).</li>
 * </ul>
 */
@FunctionalInterface
public interface LoginMethodContributor {

    /**
     * Returns the login methods this contributor wants to add to the
     * shared login page. Never {@code null}; may be empty when the
     * module is on the classpath but the runtime configuration
     * produces nothing to render (e.g. no allowlisted OAuth2
     * registrations).
     */
    List<LoginMethodView> contribute();
}
