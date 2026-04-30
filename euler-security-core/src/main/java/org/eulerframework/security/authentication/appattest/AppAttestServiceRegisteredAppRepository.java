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
import org.springframework.util.CollectionUtils;

import java.util.Collection;

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
 * {@link AppAttestAppService#createApp(RegisteredApp) createApp(RegisteredApp)} and
 * {@link AppAttestAppService#updateApp(RegisteredApp) updateApp(RegisteredApp)} based
 * on an existence check against
 * {@link AppAttestAppService#findByRegistrationId(String) findByRegistrationId}. The
 * exists-then-write sequence is not transactional, so the race window between the two
 * service calls is accepted &mdash; matching the conventional
 * {@code RegisteredClientRepository.save} implementation. Callers should rely on the
 * underlying service's primary-key uniqueness constraint as the authoritative
 * guarantee.
 *
 * <p>Listener notifications are not dispatched by this class. Because every write
 * terminates in {@link AppAttestAppService}, it is the service implementation's
 * responsibility to invoke {@link RegisteredAppChangeListener#onRegisteredAppSaved}
 * after its own persistence transaction succeeds. This keeps the notification inside
 * the same transactional boundary that owns the write, at the cost of each service
 * implementation having to wire in the listener list itself. A cleaner scheme is
 * pending; treat this contract as temporary.
 */
public class AppAttestServiceRegisteredAppRepository implements RegisteredAppRepository {

    private final AppAttestAppService appAttestAppService;

    /**
     * Create a new repository backed by the given service.
     *
     * @param appAttestAppService the backing service; must not be {@code null}
     */
    public AppAttestServiceRegisteredAppRepository(AppAttestAppService appAttestAppService) {
        Assert.notNull(appAttestAppService, "appAttestAppService must not be null");
        this.appAttestAppService = appAttestAppService;
    }

    /**
     * Create a new repository backed by the given service and preload it with the
     * supplied apps.
     *
     * <p>Each app in {@code registeredApps} is persisted via {@link #save(RegisteredApp)},
     * which routes through the service's specialized
     * {@link AppAttestAppService#createApp(RegisteredApp) createApp} /
     * {@link AppAttestAppService#updateApp(RegisteredApp) updateApp} overloads. This
     * mirrors the preload contract exposed by
     * {@code EulerRegisteredClientRepository(EulerOAuth2ClientService, Collection)}.
     *
     * @param appAttestAppService the backing service; must not be {@code null}
     * @param registeredApps      the apps to preload; may be {@code null} or empty
     */
    public AppAttestServiceRegisteredAppRepository(AppAttestAppService appAttestAppService,
                                                   Collection<RegisteredApp> registeredApps) {
        this(appAttestAppService);
        if (!CollectionUtils.isEmpty(registeredApps)) {
            for (RegisteredApp app : registeredApps) {
                this.save(app);
            }
        }
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
        // RegisteredApp's Builder guarantees a non-null id, so a single lookup
        // by registrationId is sufficient to decide between insert and update.
        AppAttestApp existing = this.appAttestAppService.findByRegistrationId(app.getId());
        if (existing == null) {
            this.appAttestAppService.createApp(app);
        } else {
            this.appAttestAppService.updateApp(app);
        }
    }

    private static RegisteredApp toRegisteredApp(AppAttestApp m) {
        return RegisteredApp.withId(m.getRegistrationId())
                .teamId(m.getTeamId())
                .bundleId(m.getBundleId())
                .oauth2Enabled(Boolean.TRUE.equals(m.getOauth2Enabled()))
                .oauth2ClientType(m.getOauth2ClientType())
                .build();
    }
}
