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

/**
 * Service-layer model of a registered app for App Attest (and, optionally, OAuth2
 * client provisioning).
 *
 * <h2>Identity</h2>
 * <ul>
 *   <li>{@link #getRegistrationId() registrationId} &mdash; the internal business
 *       primary key; also aligned with the map key of
 *       {@code euler.security.app-attest.apps} configuration.</li>
 *   <li>{@link #getAppId() appId} &mdash; the external identity derived from
 *       {@code teamId + "." + bundleId}; aligned with an OAuth2 client's
 *       {@code clientId}. Its SHA-256 digest is used as the RP ID hash in the
 *       Apple App Attest protocol and as the protocol-side lookup key; the
 *       digest is computed on demand via {@link AppAttestUtils#appIdHash(String)}
 *       / {@link AppAttestUtils#appIdHashHex(String)} rather than held as a
 *       model property.</li>
 * </ul>
 *
 * <h2>OAuth2 integration</h2>
 * When {@link #getOauth2Enabled() oauth2Enabled} is {@link Boolean#TRUE}, the app
 * is also allowed to act as an OAuth2 client. The {@link #getOauth2ClientType()}
 * determines how its {@code client_id} is provisioned (see
 * {@link RegisteredApp.OAuth2ClientType}). A {@code null} value of
 * {@code oauth2Enabled} is reserved for patch-style updates and means
 * "unchanged".
 *
 * <p>Implementations are mutable so that state can be re-hydrated via
 * {@link #reloadRegisteredApp(RegisteredApp)}.
 */
public interface AppAttestApp {

    // -- Euler extension --

    /**
     * Returns the internal registration identifier for this app.
     * <p>
     * Aligned with the map key of the {@code euler.security.app-attest.apps}
     * configuration.
     *
     * @return the registration id
     */
    String getRegistrationId();

    // -- Identity --

    /**
     * Returns the Apple Developer Team ID (10-character string).
     *
     * @return the team id
     */
    String getTeamId();

    /**
     * Returns the app's Bundle ID (e.g. {@code com.example.myapp}).
     *
     * @return the bundle id
     */
    String getBundleId();

    /**
     * Returns the app ID formed by concatenating {@link #getTeamId() teamId} and
     * {@link #getBundleId() bundleId} with a dot separator, e.g.
     * {@code ABCDE12345.com.example.myapp}.
     *
     * <p>{@link #getTeamId() teamId} and {@link #getBundleId() bundleId} must be
     * both present or both absent &mdash; mutating only one side would yield a
     * malformed {@code appId} such as {@code null.com.example.myapp}. Under the
     * patch-style {@code patchApp(AppAttestApp)} path, leaving both fields
     * {@code null} signals "keep the persisted {@code appId} unchanged".
     *
     * @return the fully qualified app ID, or {@code null} if both
     * {@link #getTeamId() teamId} and {@link #getBundleId() bundleId} are
     * {@code null}
     * @throws IllegalStateException if exactly one of {@link #getTeamId() teamId}
     *                               and {@link #getBundleId() bundleId} is {@code null}
     */
    default String getAppId() {
        String teamId = getTeamId();
        String bundleId = getBundleId();
        if (teamId == null && bundleId == null) {
            return null;
        }
        if (teamId == null || bundleId == null) {
            throw new IllegalStateException(
                    "teamId and bundleId must both be null or both be non-null");
        }
        return teamId + "." + bundleId;
    }

    // -- OAuth2 extension --

    /**
     * Returns whether this app may act as an OAuth2 client and request access
     * tokens from the authorization server.
     *
     * <p>A {@code null} value carries patch semantics: it denotes
     * "field unspecified; leave the persisted value unchanged" on the
     * {@code patchApp(AppAttestApp)} path. Non-null values always overwrite.
     *
     * @return {@link Boolean#TRUE} / {@link Boolean#FALSE} if OAuth2 is
     * enabled / disabled; {@code null} when the value is unspecified
     */
    Boolean getOauth2Enabled();

    /**
     * Returns how the OAuth2 {@code client_id} of this app is provisioned.
     *
     * @return the OAuth2 client type; {@code null} when
     * {@link #getOauth2Enabled() oauth2Enabled} is {@link Boolean#FALSE}
     */
    RegisteredApp.OAuth2ClientType getOauth2ClientType();

    // -- Bridge --

    /**
     * Reloads this app's state from the given lightweight repository-contract
     * model.
     *
     * @param app the registered app to read from
     */
    void reloadRegisteredApp(RegisteredApp app);
}
