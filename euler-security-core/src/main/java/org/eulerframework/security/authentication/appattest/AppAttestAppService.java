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
     * Updates an existing app using full-overwrite semantics on all non-audit,
     * non-primary-key fields.
     * <p>
     * Fails when no entry with the given
     * {@link AppAttestApp#getRegistrationId() registrationId} exists.
     *
     * @param app the app with updated state
     */
    void updateApp(AppAttestApp app);

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
