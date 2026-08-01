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

import org.springframework.util.Assert;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A login method registered with the application: the {@code type}
 * discriminator, the cross-type login policy, and a bag of
 * type-specific settings interpreted by the {@link LoginMethodHandler}
 * matching that type.
 *
 * <p>This is the server-side registration; the publishable projection
 * is {@link LoginMethod}. Just-in-time provisioning is not part of the
 * registration: it is an identity-type concern resolved through
 * {@code JitProvisioningPolicyResolver}.
 *
 * <p>Instances are immutable and applied verbatim: business defaults
 * are the responsibility of the registering layer (typically a
 * configuration-properties binding in the autoconfigure module).
 *
 * <p>Obtain a builder via {@link #withId(String)}.
 */
public final class RegisteredLoginMethod {

    private final String id;
    private final String type;
    private final String name;
    private final String identityType;
    private final boolean primary;
    private final Map<String, Object> properties;

    private RegisteredLoginMethod(Builder builder) {
        Assert.hasText(builder.type, "type must not be empty");
        this.id = builder.id;
        this.type = builder.type;
        this.name = builder.name;
        this.identityType = builder.identityType;
        this.primary = builder.primary;
        this.properties = builder.properties.isEmpty()
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(builder.properties));
    }

    /**
     * The identifier this registration is stored under: the declaration
     * key beneath {@code euler.security.login-method}, or a
     * repository id. Registry-local and never published; clients
     * address methods by {@link #getName() name}. Never {@code null}.
     */
    public String getId() {
        return this.id;
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
     * The declared method name identifying this registration towards
     * clients. {@code null} lets the {@link LoginMethodHandler} derive
     * one (e.g. from the OAuth2 provider or the OTP channel). Effective
     * names must be unique across all registrations.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Identity type established by this login method, stored as
     * {@code t_user_identity.identity_type}. Also the semantic anchor
     * handlers derive from (e.g. the OAuth2 provider, the OTP channel).
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

    /**
     * Type-specific settings, interpreted solely by the handler
     * registered for {@link #getType() type}. Never {@code null}.
     */
    public Map<String, Object> getProperties() {
        return this.properties;
    }

    /**
     * Returns a builder with the mandatory {@code id} already set.
     *
     * @param id the registration identifier; must not be empty
     */
    public static Builder withId(String id) {
        Assert.hasText(id, "id must not be empty");
        return new Builder(id);
    }

    /**
     * A builder for {@link RegisteredLoginMethod}. Obtain an instance
     * via {@link RegisteredLoginMethod#withId(String)}.
     */
    public static final class Builder {

        private final String id;
        private String type;
        private String name;
        private String identityType;
        private boolean primary;
        private final Map<String, Object> properties = new LinkedHashMap<>();

        private Builder(String id) {
            this.id = id;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder identityType(String identityType) {
            this.identityType = identityType;
            return this;
        }

        public Builder primary(boolean primary) {
            this.primary = primary;
            return this;
        }

        public Builder property(String key, Object value) {
            Assert.hasText(key, "key must not be empty");
            this.properties.put(key, value);
            return this;
        }

        /**
         * Adds all entries of the given map to the type-specific
         * settings. {@code null} is ignored.
         */
        public Builder properties(Map<String, Object> properties) {
            if (properties != null) {
                this.properties.putAll(properties);
            }
            return this;
        }

        public RegisteredLoginMethod build() {
            return new RegisteredLoginMethod(this);
        }
    }
}
