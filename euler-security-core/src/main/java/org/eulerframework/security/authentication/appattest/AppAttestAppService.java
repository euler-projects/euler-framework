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

import java.util.List;

/**
 * Service contract for managing {@link AppAttestApp} registrations.
 *
 * <p>Methods follow strict CRUDL semantics; no upsert is provided. The business
 * primary key is {@link AppAttestApp#getRegistrationId() registrationId}; the
 * protocol-side lookup key is the SHA-256 digest of
 * {@link AppAttestApp#getAppId() appId} (see
 * {@link AppAttestUtils#appIdHash(String)}).
 */
public interface AppAttestAppService {

    /**
     * Creates a new App Attest app registration.
     * <p>
     * When {@link AppAttestApp#getRegistrationId() registrationId} is
     * {@code null} or blank, the implementation allocates a new UUID-formatted
     * identifier. When it is provided, the value must not collide with an
     * existing entry.
     *
     * @param app the app to create
     * @return the created app with its {@link AppAttestApp#getRegistrationId()
     * registrationId} populated
     */
    AppAttestApp createApp(AppAttestApp app);

    /**
     * Creates a new App Attest app registration from a {@link RegisteredApp}
     * instance.
     * <p>
     * {@code registeredApp} is assumed to be a fully-assembled registration:
     * {@link RegisteredApp#getId() id} is persisted verbatim as the
     * {@code registrationId} and must not collide with an existing entry.
     * The server does not allocate identifiers on this path.
     *
     * @param registeredApp the {@link RegisteredApp} instance
     * @return the created app
     */
    AppAttestApp createApp(RegisteredApp registeredApp);

    /**
     * Loads an app by its {@link AppAttestApp#getRegistrationId() registrationId}.
     *
     * @param registrationId the registration identifier
     * @return the app, or {@code null} if not found
     */
    AppAttestApp findByRegistrationId(String registrationId);

    /**
     * Loads an app by the SHA-256 digest of its
     * {@link AppAttestApp#getAppId() appId}.
     * <p>
     * This is the protocol-side lookup path used by App Attest to resolve an RP
     * ID hash back to a registered app.
     *
     * @param appIdHash the SHA-256 digest of the app ID
     * @return the app, or {@code null} if not found
     */
    AppAttestApp findByAppIdHash(byte[] appIdHash);

    /**
     * Loads an app by its {@link AppAttestApp#getAppId() appId} (i.e.
     * {@code teamId + "." + bundleId}).
     *
     * @param appId the fully qualified app ID
     * @return the app, or {@code null} if not found
     */
    AppAttestApp findByAppId(String appId);

    /**
     * Updates an existing app using full-overwrite (replace) semantics on all
     * non-audit, non-primary-key fields. Mirrors HTTP {@code PUT}: fields
     * omitted from {@code app} are reset to their default / cleared state
     * rather than preserved. Use {@link #patchApp(AppAttestApp)} for partial
     * updates.
     * <p>
     * Fails when no entry with the given
     * {@link AppAttestApp#getRegistrationId() registrationId} exists.
     *
     * @param app the app with updated state
     */
    void updateApp(AppAttestApp app);

    /**
     * Updates an existing app from a {@link RegisteredApp} instance using
     * full-overwrite semantics, mirroring the contract of
     * {@link RegisteredAppRepository#save(RegisteredApp) RegisteredAppRepository#save}.
     * <p>
     * The registration is located by {@link RegisteredApp#getId() id} and every
     * mapped field is then replaced with the value carried by
     * {@code registeredApp}. This is the repository-bridge counterpart to
     * {@link #updateApp(AppAttestApp)}.
     *
     * @param registeredApp the {@link RegisteredApp} instance carrying the updated state
     */
    void updateApp(RegisteredApp registeredApp);

    /**
     * Updates an existing app using patch semantics: only fields carrying a
     * non-{@code null} value on {@code app} are applied; {@code null} fields
     * leave the persisted state unchanged. Mirrors HTTP {@code PATCH}.
     * <p>
     * {@link AppAttestApp#getTeamId() teamId} and
     * {@link AppAttestApp#getBundleId() bundleId} must be patched together
     * (both present or both absent) &mdash; see {@link AppAttestApp#getAppId()}
     * for rationale; supplying exactly one raises
     * {@link IllegalArgumentException}.
     * <p>
     * Fails when no entry with the given
     * {@link AppAttestApp#getRegistrationId() registrationId} exists.
     *
     * @param app the app carrying the fields to patch
     */
    void patchApp(AppAttestApp app);

    /**
     * Deletes an app by its {@link AppAttestApp#getRegistrationId() registrationId}.
     *
     * @param registrationId the registration identifier
     */
    void deleteByRegistrationId(String registrationId);

    /**
     * Lists apps with pagination.
     *
     * @param offset the offset of the first result
     * @param limit  the maximum number of results
     * @return a list of apps
     */
    List<AppAttestApp> listApps(int offset, int limit);
}
