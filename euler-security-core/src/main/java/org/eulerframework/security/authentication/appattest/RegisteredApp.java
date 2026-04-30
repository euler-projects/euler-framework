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

/**
 * Represents a registered app for device attestation and, optionally, OAuth2 client
 * integration.
 * <p>
 * Each registered app is identified by a combination of {@link #teamId() teamId} and
 * {@link #bundleId() bundleId}, which together form the app ID ({@code teamId.bundleId}).
 * The SHA-256 hash of the app ID is used as the RP ID hash in the Apple App Attest protocol.
 * <p>
 * When {@link #oauth2Enabled() oauth2Enabled} is {@code true}, the app is also allowed to
 * act as an OAuth2 client and request access tokens from the authorization server. The
 * {@link #oauth2ClientType() oauth2ClientType} then determines how its {@code client_id} is provisioned:
 * <ul>
 *   <li>{@link OAuth2ClientType#STATIC} &mdash; the {@code client_id} is deterministically derived
 *       from the app ID (base64url-encoded SHA-256 of {@code teamId.bundleId}), so every
 *       device running the same app shares a single OAuth2 client identity. This value
 *       matches the App Attest RP ID, allowing the {@code client_id} to be computed
 *       directly from the RP ID.</li>
 *   <li>{@link OAuth2ClientType#DYNAMIC} &mdash; each installation is treated as a distinct
 *       OAuth2 client and must self-register via the
 *       <a href="https://datatracker.ietf.org/doc/html/rfc7591">RFC 7591 OAuth 2.0 Dynamic
 *       Client Registration Protocol</a>.</li>
 * </ul>
 */
public record RegisteredApp(
        String teamId,
        String bundleId,
        boolean oauth2Enabled,
        OAuth2ClientType oauth2ClientType
) implements Serializable {

    /**
     * Canonical constructor.
     *
     * @param teamId           the Apple Developer Team ID (10-character string)
     * @param bundleId         the iOS app's Bundle ID (e.g. {@code com.example.myapp})
     * @param oauth2Enabled    whether this app may act as an OAuth2 client and request
     *                         access tokens from the authorization server
     * @param oauth2ClientType how the OAuth2 {@code client_id} is provisioned; required
     *                         when {@code oauth2Enabled} is {@code true}, may be
     *                         {@code null} otherwise
     */
    public RegisteredApp {
        Assert.hasText(teamId, "teamId must not be empty");
        Assert.hasText(bundleId, "bundleId must not be empty");
        if (oauth2Enabled) {
            Assert.notNull(oauth2ClientType, "oauth2ClientType must not be null when oauth2Enabled is true");
        }
    }

    /**
     * Create a new {@code RegisteredApp} that is used solely for App Attest and is not
     * allowed to act as an OAuth2 client.
     *
     * @param teamId   the Apple Developer Team ID (10-character string)
     * @param bundleId the iOS app's Bundle ID (e.g. {@code com.example.myapp})
     */
    public RegisteredApp(String teamId, String bundleId) {
        this(teamId, bundleId, false, null);
    }

    /**
     * Return the app ID formed by concatenating {@link #teamId} and {@link #bundleId}
     * with a dot separator, e.g. {@code ABCDE12345.com.example.myapp}.
     *
     * @return the fully qualified app ID
     */
    public String appId() {
        return this.teamId + "." + this.bundleId;
    }

    /**
     * Strategy for provisioning the OAuth2 {@code client_id} of a {@link RegisteredApp}.
     */
    public enum OAuth2ClientType {

        /**
         * The {@code client_id} is deterministically derived from the app ID
         * (base64url-encoded SHA-256 of {@code teamId.bundleId}); every device running
         * the same app shares one OAuth2 client identity, which aligns with the App
         * Attest RP ID.
         */
        STATIC,

        /**
         * Each app installation registers itself as a distinct OAuth2 client via the
         * <a href="https://datatracker.ietf.org/doc/html/rfc7591">RFC 7591 OAuth 2.0
         * Dynamic Client Registration Protocol</a>.
         */
        DYNAMIC
    }
}
