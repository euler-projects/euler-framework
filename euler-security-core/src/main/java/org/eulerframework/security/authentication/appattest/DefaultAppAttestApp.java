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
package org.eulerframework.security.authentication.appattest;

import org.springframework.util.Assert;

/**
 * Default mutable implementation of {@link AppAttestApp}.
 *
 * <p>Holds the five core fields ({@code registrationId} / {@code teamId} /
 * {@code bundleId} / {@code oauth2Enabled} / {@code oauth2ClientType}). The
 * {@code appId} view is derived inline; the SHA-256 digest of {@code appId}
 * is computed on demand by callers via {@link AppAttestUtils}.
 */
public class DefaultAppAttestApp implements AppAttestApp {

    private String registrationId;
    private String teamId;
    private String bundleId;
    private Boolean oauth2Enabled;
    private RegisteredApp.OAuth2ClientType oauth2ClientType;

    @Override
    public String getRegistrationId() {
        return this.registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    @Override
    public String getTeamId() {
        return this.teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    @Override
    public String getBundleId() {
        return this.bundleId;
    }

    public void setBundleId(String bundleId) {
        this.bundleId = bundleId;
    }

    @Override
    public Boolean getOauth2Enabled() {
        return this.oauth2Enabled;
    }

    public void setOauth2Enabled(Boolean oauth2Enabled) {
        this.oauth2Enabled = oauth2Enabled;
    }

    @Override
    public RegisteredApp.OAuth2ClientType getOauth2ClientType() {
        return this.oauth2ClientType;
    }

    public void setOauth2ClientType(RegisteredApp.OAuth2ClientType oauth2ClientType) {
        this.oauth2ClientType = oauth2ClientType;
    }

    @Override
    public void reloadRegisteredApp(RegisteredApp app) {
        Assert.notNull(app, "app must not be null");
        this.registrationId = app.getId();
        this.teamId = app.getTeamId();
        this.bundleId = app.getBundleId();
        this.oauth2Enabled = app.isOauth2Enabled();
        this.oauth2ClientType = app.getOauth2ClientType();
    }
}
