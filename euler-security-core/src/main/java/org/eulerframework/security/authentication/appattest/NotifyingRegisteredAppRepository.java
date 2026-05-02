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

import java.util.List;

/**
 * A {@link RegisteredAppRepository} decorator that dispatches
 * {@link RegisteredAppChangeListener} callbacks after each successful
 * {@link #save(RegisteredApp)}.
 *
 * <p>Concrete repository implementations stay focused on their sole storage
 * responsibility; the listener fan-out is centralized here once, so adding a
 * new repository backend does not require re-implementing the notification
 * plumbing.
 *
 * <p>Listener invocations are sequential and synchronous, in the iteration
 * order of the provided list. If any listener throws, subsequent listeners are
 * not invoked and the exception propagates to the caller of {@code save}.
 */
public class NotifyingRegisteredAppRepository implements RegisteredAppRepository {

    private final RegisteredAppRepository delegate;
    private final List<RegisteredAppChangeListener> listeners;

    /**
     * Create a new decorator around the given delegate repository.
     *
     * @param delegate  the backing repository; must not be {@code null}
     * @param listeners the listeners to notify after each save; may be empty
     *                  but not {@code null}
     */
    public NotifyingRegisteredAppRepository(RegisteredAppRepository delegate,
                                            List<RegisteredAppChangeListener> listeners) {
        Assert.notNull(delegate, "delegate must not be null");
        Assert.notNull(listeners, "listeners must not be null");
        this.delegate = delegate;
        this.listeners = List.copyOf(listeners);
    }

    @Override
    public RegisteredApp findByAppIdHash(byte[] appIdHash) {
        return this.delegate.findByAppIdHash(appIdHash);
    }

    @Override
    public void save(RegisteredApp app) {
        Assert.notNull(app, "app must not be null");
        this.delegate.save(app);
        for (RegisteredAppChangeListener listener : this.listeners) {
            listener.onRegisteredAppSaved(app);
        }
    }
}
