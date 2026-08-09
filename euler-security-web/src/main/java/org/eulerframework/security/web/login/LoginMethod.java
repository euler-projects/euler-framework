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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A login method offered to the client: the publishable projection of a
 * {@link RegisteredLoginMethod}, produced by {@link LoginMethodHandler}s
 * and aggregated by {@link LoginMethodService}s.
 *
 * <p>Only resolved, publishable data appears here; server-side policy
 * carried by the registration is never exposed. A registration that
 * cannot currently be served (e.g. an OAuth2 client registration that
 * is not defined) yields no instance at all.
 *
 * <p>Fields are purely semantic: they state what the method is, leaving
 * every presentation and routing decision to the client. Submission
 * targets are not published &mdash; clients either post to the
 * login-method dispatch endpoint using {@link #getName() name}, or
 * route to the underlying endpoints on their own. Type-specific
 * semantics (e.g. the OAuth2 provider, the OTP channel) are published
 * through {@link #getAttributes() attributes}.
 *
 * <p>Instances are immutable. Obtain a builder via
 * {@link #withType(String)}.
 */
public final class LoginMethod {

    private final String type;
    private final String name;
    private final boolean primary;
    private final Map<String, String> attributes;

    private LoginMethod(Builder builder) {
        this.type = builder.type;
        this.name = builder.name;
        this.primary = builder.primary;
        this.attributes = builder.attributes.isEmpty()
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(builder.attributes));
    }

    /**
     * Login-method family identifier, matching the {@code type()} of
     * the {@link LoginMethodHandler} that produced this instance (e.g.
     * {@code "password"}, {@code "oauth2"}, {@code "otp"}). Never
     * {@code null}.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Unique name identifying this method towards clients: the
     * declared {@code method-name}, or a handler-derived default (e.g.
     * the OAuth2 provider, the OTP channel). Used as the method
     * parameter value at the login-method dispatch endpoint.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Whether this method belongs to the expanded (primary) group
     * rather than the secondary group.
     */
    public boolean isPrimary() {
        return this.primary;
    }

    /**
     * Type-specific semantics resolved by the producing
     * {@link LoginMethodHandler} (e.g. {@code provider} for oauth2,
     * {@code channel} for otp). Values are curated, publishable
     * results &mdash; never a passthrough of the registration's
     * {@code properties}. Never {@code null}.
     */
    public Map<String, String> getAttributes() {
        return this.attributes;
    }

    /**
     * Returns a builder with the mandatory {@code type} already set.
     *
     * @param type the login-method family identifier; must not be empty
     */
    public static Builder withType(String type) {
        Assert.hasText(type, "type must not be empty");
        return new Builder(type);
    }

    /**
     * A builder for {@link LoginMethod}. Obtain an instance via
     * {@link LoginMethod#withType(String)}.
     */
    public static final class Builder {

        private final String type;
        private String name;
        private boolean primary;
        private final Map<String, String> attributes = new LinkedHashMap<>();

        private Builder(String type) {
            this.type = type;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder primary(boolean primary) {
            this.primary = primary;
            return this;
        }

        public Builder attribute(String key, String value) {
            Assert.hasText(key, "key must not be empty");
            this.attributes.put(key, value);
            return this;
        }

        public LoginMethod build() {
            return new LoginMethod(this);
        }
    }
}
