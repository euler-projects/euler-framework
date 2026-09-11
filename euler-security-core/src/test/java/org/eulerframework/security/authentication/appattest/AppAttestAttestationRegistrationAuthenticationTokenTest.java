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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the device-registration token.
 */
class AppAttestAttestationRegistrationAuthenticationTokenTest {

    private static AppAttestAttestationRegistration registration(String keyId, String clientId) {
        return new AppAttestAttestationRegistration(
                keyId, "ABCD1234EF", "com.example.app", clientId,
                new byte[16], new byte[0], new byte[0], new byte[0],
                null, "{}", 0);
    }

    @Test
    void registeredCarriesDevicePrincipalAndNoAuthorities() {
        AppAttestAttestationRegistration registration = registration("kid-1", null);

        AppAttestAttestationRegistrationAuthenticationToken token =
                AppAttestAttestationRegistrationAuthenticationToken.registered(registration);

        assertTrue(token.isAuthenticated());
        assertSame(registration, token.getPrincipal(), "principal is the device registration, not a user");
        assertEquals("kid-1", token.getKeyId());
        assertTrue(token.getAuthorities().isEmpty(), "device registration grants no user authorities");
    }

    @Test
    void unauthenticatedCarriesRequestDataAndIsNotAuthenticated() {
        AppAttestAttestationRegistrationAuthenticationToken token =
                AppAttestAttestationRegistrationAuthenticationToken.unauthenticated("attestation", "challenge");

        assertFalse(token.isAuthenticated());
        assertNull(token.getKeyId(), "keyId is derived from the attestation, so it is absent before validation");
        assertEquals("attestation", token.getCredentials());
        assertEquals("challenge", token.getChallenge());
    }
}
