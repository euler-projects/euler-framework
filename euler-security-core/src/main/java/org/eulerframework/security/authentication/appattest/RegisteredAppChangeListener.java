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
 * Callback interface invoked when a {@link RegisteredApp} is saved through a
 * {@link RegisteredAppRepository}.
 * <p>
 * Implementations may react to new or updated app registrations—for example, by
 * provisioning a corresponding OAuth2 client in the authorization server.
 *
 * @see RegisteredAppRepository#save(RegisteredApp)
 */
@FunctionalInterface
public interface RegisteredAppChangeListener {

    /**
     * Called after a {@link RegisteredApp} has been persisted.
     *
     * @param app the registered app that was just saved
     */
    void onRegisteredAppSaved(RegisteredApp app);
}
