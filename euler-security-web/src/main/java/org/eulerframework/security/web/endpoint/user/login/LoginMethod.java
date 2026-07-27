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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A single login-method declaration bound from
 * {@code euler.security.web.login-methods.<name>}.
 *
 * <p>Top-level fields carry the {@code type} discriminator and the
 * cross-type login policy; the {@code properties} bag carries optional
 * type-specific settings interpreted by the
 * {@link LoginMethodTypeHandler} registered for the declared type.
 */
public class LoginMethod {

    private String type;

    /**
     * Identity type established by this login method, stored as
     * {@code t_user_identity.identity_type}. Defaults to the
     * login-method key.
     */
    private String identityType;

    /**
     * Whether to provision a local user on first successful login when
     * no matching local identity exists. Defaults to {@code true};
     * when {@code false}, only already-known users can sign in.
     */
    private boolean autoCreateUser = true;

    /**
     * Authorities granted to an auto-provisioned user. Defaults to
     * {@code user}; configured values replace the default. Must not be
     * empty when {@code autoCreateUser} is {@code true}.
     */
    private String[] defaultAuthorities = {"user"};

    private final Map<String, Object> properties = new LinkedHashMap<>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIdentityType() {
        return identityType;
    }

    public void setIdentityType(String identityType) {
        this.identityType = identityType;
    }

    public boolean isAutoCreateUser() {
        return autoCreateUser;
    }

    public void setAutoCreateUser(boolean autoCreateUser) {
        this.autoCreateUser = autoCreateUser;
    }

    public String[] getDefaultAuthorities() {
        return defaultAuthorities;
    }

    public void setDefaultAuthorities(String[] defaultAuthorities) {
        this.defaultAuthorities = defaultAuthorities;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }
}
