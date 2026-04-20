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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of {@link AppAttestAttestationRegistrationService}.
 * <p>
 * Suitable for single-instance deployments or development/testing environments.
 */
public class InMemoryAppAttestAttestationRegistrationService implements AppAttestAttestationRegistrationService {

    private final Map<String, AppAttestAttestationRegistration> registrations = new ConcurrentHashMap<>();

    @Override
    public void saveRegistration(AppAttestAttestationRegistration registration) {
        Assert.notNull(registration, "registration must not be null");
        Assert.hasText(registration.getKeyId(), "keyId must not be empty");
        this.registrations.put(registration.getKeyId(), registration);
    }

    @Override
    public AppAttestAttestationRegistration findByKeyId(String keyId) {
        return this.registrations.get(keyId);
    }

    @Override
    public void updateSignCount(String keyId, long newSignCount) {
        AppAttestAttestationRegistration registration = this.registrations.get(keyId);
        if (registration != null && newSignCount > registration.getSignCount()) {
            registration.setSignCount(newSignCount);
        }
    }
}
