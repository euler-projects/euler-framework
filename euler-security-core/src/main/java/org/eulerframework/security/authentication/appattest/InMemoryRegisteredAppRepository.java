/*
 * Copyright 2013-2026 the original author or authors.
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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/// An in-memory implementation of [RegisteredAppRepository] that stores registered Apps
/// in a [ConcurrentHashMap] keyed by the hex-encoded SHA-256 hash of the App ID.
///
/// This implementation is suitable for development, testing, or deployments where the
/// set of registered Apps is known at startup and configured via application properties.
public class InMemoryRegisteredAppRepository implements RegisteredAppRepository {

    private final Map<String /* App ID Hash Hex */, RegisteredApp> registeredApps
            = new ConcurrentHashMap<>();

    /**
     * Create a new {@code InMemoryDeviceRepository} with the specified registered devices.
     *
     * @param registeredApps the registered devices
     */
    public InMemoryRegisteredAppRepository(RegisteredApp... registeredApps) {
        this(Arrays.asList(registeredApps));
    }

    /**
     * Create a new {@code InMemoryAppleAppRepository} with the specified registered apps.
     *
     * @param registeredApps the list of registered Apple Apps
     */
    public InMemoryRegisteredAppRepository(List<RegisteredApp> registeredApps) {
        Assert.notEmpty(registeredApps, "registeredDevices must not be empty");
        for (RegisteredApp device : registeredApps) {
            String deviceIdHashHex = bytesToHex(computeAppIdHash(device));
            this.registeredApps.put(deviceIdHashHex, device);
        }
    }

    @Override
    public RegisteredApp findByAppIdHash(byte[] appIdHash) {
        Assert.notNull(appIdHash, "deviceIdHash must not be null");
        return this.registeredApps.get(bytesToHex(appIdHash));
    }

    private static byte[] computeAppIdHash(RegisteredApp registeredApp) {
        String appId = registeredApp.appId();
        return sha256(appId.getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] sha256(byte[] data) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
