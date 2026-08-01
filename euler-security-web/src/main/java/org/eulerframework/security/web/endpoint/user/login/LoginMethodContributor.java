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
 * SPI implemented by any module that offers a selectable login method
 * (e.g. password form, OAuth2 / OIDC redirect, Passkey / WebAuthn,
 * phone OTP, ...).
 *
 * <p>Every {@code LoginMethodContributor} bean in the application
 * context is aggregated into a single flat list of available login
 * methods, consumed by the bundled login page or by any client that
 * reads the method list.
 *
 * <p>Design notes:
 * <ul>
 *   <li>Contributors are the only extension seam &mdash; new login
 *       methods opt in simply by publishing a bean.</li>
 *   <li>Enabling a login method (property toggles, dependency checks,
 *       registration allowlists, etc.) is the contributor's own
 *       responsibility &mdash; its module owns the corresponding
 *       {@code @ConfigurationProperties} and gates the bean
 *       accordingly. Consumers never inspect enablement.</li>
 *   <li>Ordering across contributors is controlled by Spring's
 *       {@code @Order} / {@code Ordered}; ordering <em>within</em> a
 *       contributor's returned list is preserved verbatim.</li>
 * </ul>
 */
@FunctionalInterface
public interface LoginMethodContributor {

    /**
     * Returns the login methods this contributor offers. Never
     * {@code null}; may be empty when the module is on the classpath
     * but the runtime configuration produces nothing to offer (e.g. no
     * usable OAuth2 registrations).
     */
    List<LoginMethod> contribute();
}
