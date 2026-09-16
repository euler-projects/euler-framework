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

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a registered app for device attestation and, optionally, OAuth2 client
 * integration.
 *
 * <h2>Identity</h2>
 * <ul>
 *   <li>{@link #getId() id} &mdash; the internal registration identifier ({@code
 *       registrationId}); the business-side primary key and also the key of the
 *       {@code euler.security.authentication.app-attest.apps} configuration map.</li>
 *   <li>{@link #getAppId() appId} &mdash; the external identity formed by concatenating
 *       {@link #getTeamId() teamId} and {@link #getBundleId() bundleId} with a dot
 *       separator ({@code teamId.bundleId}); aligned with an OAuth2 client's
 *       {@code clientId}.</li>
 *   <li>The SHA-256 hash of {@code appId} is used as the RP ID hash in the Apple App
 *       Attest protocol.</li>
 * </ul>
 *
 * <h2>OAuth2 integration</h2>
 * When {@link #isOauth2Enabled() oauth2Enabled} is {@code true}, the app is also allowed
 * to act as an OAuth2 client and request access tokens from the authorization server.
 * Each app installation registers itself as a distinct OAuth2 client via the
 * <a href="https://datatracker.ietf.org/doc/html/rfc7591">RFC 7591 OAuth 2.0 Dynamic
 * Client Registration Protocol</a>.
 *
 * <p>Instances are immutable and constructed via {@link #withId(String)}, which
 * requires the mandatory {@code id} up front; the convenience factory
 * {@link #withTeamAndBundle(String, String, String)} produces an App-Attest-only
 * registration.
 */
public final class RegisteredApp implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final String teamId;
    private final String bundleId;
    private final boolean oauth2Enabled;

    private RegisteredApp(String id,
                          String teamId,
                          String bundleId,
                          boolean oauth2Enabled) {
        this.id = id;
        this.teamId = teamId;
        this.bundleId = bundleId;
        this.oauth2Enabled = oauth2Enabled;
    }

    /**
     * Returns the internal registration identifier (aka {@code registrationId}).
     *
     * @return the registration id; never {@code null}
     */
    public String getId() {
        return this.id;
    }

    /**
     * Returns the Apple Developer Team ID (10-character string).
     *
     * @return the team id; never {@code null}
     */
    public String getTeamId() {
        return this.teamId;
    }

    /**
     * Returns the app's Bundle ID (e.g. {@code com.example.myapp}).
     *
     * @return the bundle id; never {@code null}
     */
    public String getBundleId() {
        return this.bundleId;
    }

    /**
     * Returns whether this app may act as an OAuth2 client and request access tokens
     * from the authorization server.
     *
     * @return {@code true} if OAuth2 is enabled for this app
     */
    public boolean isOauth2Enabled() {
        return this.oauth2Enabled;
    }

    /**
     * Returns the app ID formed by concatenating {@link #getTeamId() teamId} and
     * {@link #getBundleId() bundleId} with a dot separator, e.g.
     * {@code ABCDE12345.com.example.myapp}.
     *
     * @return the fully qualified app ID
     */
    public String getAppId() {
        return this.teamId + "." + this.bundleId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegisteredApp other)) return false;
        return this.oauth2Enabled == other.oauth2Enabled
                && Objects.equals(this.id, other.id)
                && Objects.equals(this.teamId, other.teamId)
                && Objects.equals(this.bundleId, other.bundleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.teamId, this.bundleId, this.oauth2Enabled);
    }

    @Override
    public String toString() {
        return "RegisteredApp{id='" + this.id
                + "', appId='" + getAppId()
                + "', oauth2Enabled=" + this.oauth2Enabled
                + '}';
    }

    /**
     * Returns a new {@link Builder} seeded with the given mandatory
     * registration id.
     *
     * @param id the internal registration identifier (aka {@code registrationId});
     *           must not be empty
     * @return a fresh builder with {@code id} already set
     */
    public static Builder withId(String id) {
        Assert.hasText(id, "id must not be empty");
        return new Builder().id(id);
    }

    /**
     * Create a new {@code RegisteredApp} that is used solely for App Attest and is not
     * allowed to act as an OAuth2 client.
     *
     * @param id       the internal registration identifier
     * @param teamId   the Apple Developer Team ID (10-character string)
     * @param bundleId the iOS app's Bundle ID (e.g. {@code com.example.myapp})
     * @return a new App-Attest-only registration
     */
    public static RegisteredApp withTeamAndBundle(String id, String teamId, String bundleId) {
        return withId(id)
                .teamId(teamId)
                .bundleId(bundleId)
                .oauth2Enabled(false)
                .build();
    }

    /**
     * A builder for {@link RegisteredApp}. Obtain an instance via
     * {@link RegisteredApp#withId(String)} so that the mandatory
     * {@code id} is supplied up front.
     */
    public static final class Builder {

        private String id;
        private String teamId;
        private String bundleId;
        private boolean oauth2Enabled;

        private Builder() {
        }

        /**
         * Sets the internal registration identifier (aka {@code registrationId}).
         * <p>
         * Not exposed publicly; {@code id} must be supplied through
         * {@link RegisteredApp#withId(String)}.
         *
         * @param id the registration id
         * @return this builder
         */
        private Builder id(String id) {
            this.id = id;
            return this;
        }

        /**
         * Sets the Apple Developer Team ID.
         *
         * @param teamId the team id
         * @return this builder
         */
        public Builder teamId(String teamId) {
            this.teamId = teamId;
            return this;
        }

        /**
         * Sets the app's Bundle ID.
         *
         * @param bundleId the bundle id
         * @return this builder
         */
        public Builder bundleId(String bundleId) {
            this.bundleId = bundleId;
            return this;
        }

        /**
         * Sets whether this app may act as an OAuth2 client.
         *
         * @param oauth2Enabled the flag
         * @return this builder
         */
        public Builder oauth2Enabled(boolean oauth2Enabled) {
            this.oauth2Enabled = oauth2Enabled;
            return this;
        }

        /**
         * Builds a new immutable {@link RegisteredApp}.
         *
         * @return the registered app
         */
        public RegisteredApp build() {
            Assert.hasText(this.id, "id must not be empty");
            Assert.hasText(this.teamId, "teamId must not be empty");
            Assert.hasText(this.bundleId, "bundleId must not be empty");
            return new RegisteredApp(this.id, this.teamId, this.bundleId, this.oauth2Enabled);
        }
    }
}
