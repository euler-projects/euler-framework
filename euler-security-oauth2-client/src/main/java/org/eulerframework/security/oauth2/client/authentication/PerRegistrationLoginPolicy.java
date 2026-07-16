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
package org.eulerframework.security.oauth2.client.authentication;

import org.springframework.util.Assert;

import java.util.List;

/**
 * Per-{@code registrationId} login-side policy consumed by
 * {@link OAuth2LoginPrincipalPromotingSuccessHandler} after a
 * successful upstream federation.
 *
 * <p>An entry is scoped to a single
 * {@code spring.security.oauth2.client.registration.<key>}; different
 * registrations may carry different policies (e.g. a corporate IdP may
 * grant elevated authorities on auto-provisioning, while a public
 * social IdP grants only the {@code user} authority).
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code autoCreateUser} &mdash; provision a local user on first
 *       successful federation when {@code true}; reject as unknown user
 *       when {@code false}.</li>
 *   <li>{@code defaultAuthorities} &mdash; authorities granted on
 *       auto-provisioning. Must reference existing rows in the local
 *       authorities table. Must not be empty when {@code autoCreateUser}
 *       is {@code true}.</li>
 *   <li>{@code identityType} &mdash; the value stored in
 *       {@code t_user_identity.identity_type} for the newly created
 *       binding. Typically equals the login-method key (e.g.
 *       {@code "google"}) but can diverge when the same IdP is used in
 *       multiple contexts (e.g. {@code "corp-google"}).</li>
 * </ul>
 */
public final class PerRegistrationLoginPolicy {

    private final boolean autoCreateUser;
    private final List<String> defaultAuthorities;
    private final String identityType;

    public PerRegistrationLoginPolicy(boolean autoCreateUser,
                                      List<String> defaultAuthorities,
                                      String identityType) {
        Assert.hasText(identityType, "identityType is required");
        if (autoCreateUser) {
            Assert.notEmpty(defaultAuthorities,
                    "defaultAuthorities must not be empty when autoCreateUser=true");
        }
        this.autoCreateUser = autoCreateUser;
        this.defaultAuthorities = defaultAuthorities == null
                ? List.of() : List.copyOf(defaultAuthorities);
        this.identityType = identityType;
    }

    public boolean isAutoCreateUser() {
        return autoCreateUser;
    }

    public List<String> getDefaultAuthorities() {
        return defaultAuthorities;
    }

    public String getIdentityType() {
        return identityType;
    }
}
