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

package org.eulerframework.security.authentication.apple;

import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * Represents a registered Apple App for Apple App Attest validation.
 * <p>
 * Each registered app is identified by a combination of {@link #teamId () teamId} and
 * {@link #bundleId () bundleId}, which together form the App ID ({@code teamId.bundleId}).
 * The SHA-256 hash of the App ID is used as the RP ID hash in the App Attest protocol.
 *
 * @see AppleAppRepository
 * @see DefaultAppleAppAttestValidationService
 */
public record RegisteredAppleApp(String teamId, String bundleId) implements Serializable {

    /**
     * Create a new {@code RegisteredAppleApp} with the specified Team ID and Bundle ID.
     *
     * @param teamId   the Apple Developer Team ID (10-character string)
     * @param bundleId the iOS app's Bundle ID (e.g. {@code com.example.myapp})
     */
    public RegisteredAppleApp {
        Assert.hasText(teamId, "teamId must not be empty");
        Assert.hasText(bundleId, "bundleId must not be empty");
    }

    /**
     * Return the Apple Developer Team ID.
     */
    @Override
    public String teamId() {
        return teamId;
    }

    /**
     * Return the iOS app's Bundle ID.
     */
    @Override
    public String bundleId() {
        return bundleId;
    }

    @Override
    @Nonnull
    public String toString() {
        return "RegisteredAppleApp{teamId='" + teamId + "', bundleId='" + bundleId + "'}";
    }
}
