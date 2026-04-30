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

import java.util.Collections;
import java.util.List;

/**
 * {@link RegisteredAppRepository} implementation that delegates all storage to an
 * {@link AppAttestAppService}.
 *
 * <p>This class acts as the bridge between the lightweight repository contract used
 * by the App Attest authenticators and the service-layer CRUDL API. The mapping is
 * symmetric to how {@code EulerRegisteredClientRepository} bridges Spring's
 * {@code RegisteredClientRepository} to {@code EulerOAuth2ClientService}.
 *
 * <p>{@link #save(RegisteredApp)} dispatches between
 * {@link AppAttestAppService#createApp(AppAttestApp) createApp} and
 * {@link AppAttestAppService#updateApp(AppAttestApp) updateApp} based on an
 * existence check against
 * {@link AppAttestAppService#findByRegistrationId(String) findByRegistrationId}.
 * The exists-then-write sequence is not transactional, so the race window between
 * the two service calls is accepted &mdash; matching the conventional
 * {@code RegisteredClientRepository.save} implementation. Callers should rely on
 * the underlying service's primary-key uniqueness constraint as the authoritative
 * guarantee.
 */
public class AppAttestServiceRegisteredAppRepository implements RegisteredAppRepository {

    private final AppAttestAppService appAttestAppService;
    private final List<RegisteredAppChangeListener> listeners;

    /**
     * Create a new repository backed by the given service, with no change
     * listeners.
     *
     * @param appAttestAppService the backing service; must not be {@code null}
     */
    public AppAttestServiceRegisteredAppRepository(AppAttestAppService appAttestAppService) {
        this(appAttestAppService, Collections.emptyList());
    }

    /**
     * Create a new repository backed by the given service and change listeners.
     *
     * @param appAttestAppService the backing service; must not be {@code null}
     * @param listeners           the listeners to notify after each save; may be
     *                            empty but not {@code null}
     */
    public AppAttestServiceRegisteredAppRepository(AppAttestAppService appAttestAppService,
                                                   List<RegisteredAppChangeListener> listeners) {
        Assert.notNull(appAttestAppService, "appAttestAppService must not be null");
        Assert.notNull(listeners, "listeners must not be null");
        this.appAttestAppService = appAttestAppService;
        this.listeners = List.copyOf(listeners);
    }

    @Override
    public RegisteredApp findByAppIdHash(byte[] appIdHash) {
        Assert.notNull(appIdHash, "appIdHash must not be null");
        AppAttestApp model = this.appAttestAppService.findByAppIdHash(appIdHash);
        return (model == null) ? null : toRegisteredApp(model);
    }

    @Override
    public void save(RegisteredApp app) {
        Assert.notNull(app, "app must not be null");
        DefaultAppAttestApp model = new DefaultAppAttestApp();
        model.reloadRegisteredApp(app);

        AppAttestApp existing = this.appAttestAppService.findByRegistrationId(app.getId());
        if (existing == null) {
            this.appAttestAppService.createApp(model);
        } else {
            this.appAttestAppService.updateApp(model);
        }
        for (RegisteredAppChangeListener listener : this.listeners) {
            listener.onRegisteredAppSaved(app);
        }
    }

    private static RegisteredApp toRegisteredApp(AppAttestApp m) {
        return RegisteredApp.withId(m.getRegistrationId())
                .teamId(m.getTeamId())
                .bundleId(m.getBundleId())
                .oauth2Enabled(m.isOauth2Enabled())
                .oauth2ClientType(m.getOauth2ClientType())
                .build();
    }
}
