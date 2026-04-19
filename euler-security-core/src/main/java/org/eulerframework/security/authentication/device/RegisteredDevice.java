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

package org.eulerframework.security.authentication.device;

import org.springframework.util.Assert;

import java.io.Serializable;

/**
 * Represents a registered device for device attestation.
 * <p>
 * Each registered device is identified by a combination of {@link #teamId () teamId} and
 * {@link #bundleId () bundleId}, which together form the device ID ({@code teamId.bundleId}).
 * The SHA-256 hash of the device ID is used as the RP ID hash in the Apple App Attest protocol.
 *
 */
public record RegisteredDevice(String teamId, String bundleId) implements Serializable {

    /**
     * Create a new {@code RegisteredAppleApp} with the specified Team ID and Bundle ID.
     *
     * @param teamId   the Apple Developer Team ID (10-character string)
     * @param bundleId the iOS app's Bundle ID (e.g. {@code com.example.myapp})
     */
    public RegisteredDevice {
        Assert.hasText(teamId, "teamId must not be empty");
        Assert.hasText(bundleId, "bundleId must not be empty");
    }

    public String deviceId() {
        return this.teamId + "." + this.bundleId;
    }
}
