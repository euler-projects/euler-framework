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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/// An in-memory implementation of [RegisteredAppRepository] that stores registered Apps
/// in a [ConcurrentHashMap] keyed by the hex-encoded SHA-256 hash of the App ID.
///
/// This implementation is suitable for development, testing, or deployments where the
/// set of registered Apps is known at startup and configured via application properties.
public class InMemoryRegisteredAppRepository implements RegisteredAppRepository {

    private final Map<String /* App ID Hash Hex */, RegisteredApp> registeredApps
            = new ConcurrentHashMap<>();

    private final List<RegisteredAppChangeListener> listeners;

    /**
     * Create a new {@code InMemoryRegisteredAppRepository} with the specified registered apps
     * and no change listeners.
     *
     * @param registeredApps the registered apps
     */
    public InMemoryRegisteredAppRepository(RegisteredApp... registeredApps) {
        this(Arrays.asList(registeredApps), Collections.emptyList());
    }

    /**
     * Create a new {@code InMemoryRegisteredAppRepository} with the specified registered apps
     * and no change listeners.
     *
     * @param registeredApps the list of registered apps
     */
    public InMemoryRegisteredAppRepository(List<RegisteredApp> registeredApps) {
        this(registeredApps, Collections.emptyList());
    }

    /**
     * Create a new {@code InMemoryRegisteredAppRepository} with the specified registered apps
     * and change listeners.
     * <p>
     * Each app in the initial list is saved via {@link #save(RegisteredApp)}, which triggers
     * listener notifications.
     *
     * @param registeredApps the list of registered apps
     * @param listeners      the listeners to notify on save; may be empty but not {@code null}
     */
    public InMemoryRegisteredAppRepository(List<RegisteredApp> registeredApps,
                                           List<RegisteredAppChangeListener> listeners) {
        Assert.notEmpty(registeredApps, "registeredApps must not be empty");
        Assert.notNull(listeners, "listeners must not be null");
        this.listeners = List.copyOf(listeners);
        for (RegisteredApp app : registeredApps) {
            save(app);
        }
    }

    @Override
    public RegisteredApp findByAppIdHash(byte[] appIdHash) {
        Assert.notNull(appIdHash, "appIdHash must not be null");
        return this.registeredApps.get(HexFormat.of().formatHex(appIdHash));
    }

    @Override
    public void save(RegisteredApp app) {
        Assert.notNull(app, "app must not be null");
        String hashHex = AppAttestUtils.appIdHashHex(app);
        this.registeredApps.put(hashHex, app);
        for (RegisteredAppChangeListener listener : this.listeners) {
            listener.onRegisteredAppSaved(app);
        }
    }
}
