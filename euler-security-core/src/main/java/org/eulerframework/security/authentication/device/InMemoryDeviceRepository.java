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

package org.eulerframework.security.authentication.device;

import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/// An in-memory implementation of [DeviceRepository] that stores registered devices
/// in a [ConcurrentHashMap] keyed by the hex-encoded SHA-256 hash of the device ID.
///
/// This implementation is suitable for development, testing, or deployments where the
/// set of registered devices is known at startup and configured via application properties.
public class InMemoryDeviceRepository implements DeviceRepository {

    private final Map<String /* Device ID Hash Hex */, RegisteredDevice> devices = new ConcurrentHashMap<>();

    /**
     * Create a new {@code InMemoryDeviceRepository} with the specified registered devices.
     *
     * @param registeredDevices the registered devices
     */
    public InMemoryDeviceRepository(RegisteredDevice... registeredDevices) {
        this(Arrays.asList(registeredDevices));
    }

    /**
     * Create a new {@code InMemoryAppleAppRepository} with the specified registered apps.
     *
     * @param registeredDevices the list of registered Apple Apps
     */
    public InMemoryDeviceRepository(List<RegisteredDevice> registeredDevices) {
        Assert.notEmpty(registeredDevices, "registeredDevices must not be empty");
        for (RegisteredDevice device : registeredDevices) {
            String deviceIdHashHex = bytesToHex(computeDeviceIdHash(device));
            this.devices.put(deviceIdHashHex, device);
        }
    }

    @Override
    public RegisteredDevice findByDeviceIdHash(byte[] deviceIdHash) {
        Assert.notNull(deviceIdHash, "deviceIdHash must not be null");
        return this.devices.get(bytesToHex(deviceIdHash));
    }

    private static byte[] computeDeviceIdHash(RegisteredDevice registeredDevice) {
        String deviceId = registeredDevice.deviceId();
        return sha256(deviceId.getBytes(StandardCharsets.UTF_8));
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
