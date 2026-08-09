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

import org.springframework.util.Assert;

/**
 * A login method registered with the application, holding what every
 * type has in common. Each type contributes a subclass carrying its own
 * settings as ordinary fields, e.g.
 * {@link RegisteredOAuth2LoginMethod} or
 * {@link RegisteredOtpLoginMethod}; the
 * {@link LoginMethodHandler} serving that type receives instances of it.
 *
 * <p>This is the server-side registration; the publishable projection
 * is {@link LoginMethod}. Just-in-time provisioning is not part of the
 * registration: it is an identity-type concern resolved through
 * {@code JitProvisioningPolicyResolver}.
 *
 * <p>Instances are immutable and applied verbatim: business defaults
 * are the responsibility of the registering layer (typically a
 * configuration-properties binding in the autoconfigure module).
 */
public abstract class RegisteredLoginMethod {

    private final String type;
    private final String id;
    private final String name;
    private final String identityType;
    private final boolean primary;

    /**
     * @param type         the type discriminator, supplied by the
     *                     subclass; must not be empty
     * @param id           the identifier this registration is stored
     *                     under; must not be empty
     * @param name         the name clients address this method by; must
     *                     not be empty
     * @param identityType the identity type this method establishes, or
     *                     {@code null} for methods establishing none
     * @param primary      whether the method belongs to the primary group
     */
    protected RegisteredLoginMethod(String type, String id, String name,
                                    String identityType, boolean primary) {
        Assert.hasText(type, "type must not be empty");
        Assert.hasText(id, "id must not be empty");
        Assert.hasText(name, "name must not be empty");
        this.type = type;
        this.id = id;
        this.name = name;
        this.identityType = identityType;
        this.primary = primary;
    }

    /**
     * The type discriminator selecting the {@link LoginMethodHandler}
     * that serves this method (e.g. {@code password}, {@code oauth2},
     * {@code otp}). Never {@code null}.
     */
    public String getType() {
        return this.type;
    }

    /**
     * The identifier this registration is stored under: the declaration
     * key beneath {@code euler.security.login-method.<method-type>}, or
     * a repository id. Registry-local and never published; clients
     * address methods by {@link #getName() name}. Never {@code null}.
     */
    public String getId() {
        return this.id;
    }

    /**
     * The name identifying this method towards clients, carried by the
     * dispatch method parameter. Unique across all registrations;
     * duplicates are rejected at aggregation time. Never {@code null}.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Identity type established by this login method, stored as
     * {@code t_user_identity.identity_type}. Also the semantic anchor
     * types may fall back on (e.g. the OAuth2 provider, the OTP
     * channel). {@code null} for methods establishing no identity of
     * their own, such as password login.
     */
    public String getIdentityType() {
        return this.identityType;
    }

    /**
     * Whether this method belongs to the expanded (primary) group of
     * the login page rather than the secondary group.
     */
    public boolean isPrimary() {
        return this.primary;
    }
}
