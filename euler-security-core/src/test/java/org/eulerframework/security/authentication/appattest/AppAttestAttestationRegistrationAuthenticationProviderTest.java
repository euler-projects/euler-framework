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

import org.eulerframework.security.authentication.ChallengeService;
import org.eulerframework.security.authentication.GeneratedChallenge;
import org.eulerframework.security.authentication.appattest.apple.AppleAppAttestValidationService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link AppAttestAttestationRegistrationAuthenticationProvider}, which is a
 * device-KEY registration operation: it consumes the one-time challenge, delegates
 * attestation validation, and returns a token whose principal is the device
 * registration. It creates no user and grants no authorities.
 * <p>
 * The idempotency of {@code validateAttestation} (return the existing registration for
 * an already-registered KEY) lives inside the validation service and is gated behind
 * Apple certificate-chain verification, so it is covered by integration tests with a
 * real attestation rather than here; this test verifies the provider surfaces whatever
 * registration the validation service returns without any user provisioning.
 */
class AppAttestAttestationRegistrationAuthenticationProviderTest {

    private static AppAttestAttestationRegistration registration(String keyId, String clientId) {
        return new AppAttestAttestationRegistration(
                keyId, "ABCD1234EF", "com.example.app", clientId,
                new byte[16], new byte[0], new byte[0], new byte[0],
                null, "{}", 0);
    }

    @Test
    void registersDeviceKeyWithoutCreatingAUser() {
        RecordingChallengeService challengeService = new RecordingChallengeService(true);
        AppAttestAttestationRegistration registration = registration("kid-1", null);
        FakeValidationService validationService = new FakeValidationService(registration);
        AppAttestAttestationRegistrationAuthenticationProvider provider =
                new AppAttestAttestationRegistrationAuthenticationProvider(challengeService, validationService);

        Authentication result = provider.authenticate(
                AppAttestAttestationRegistrationAuthenticationToken.unauthenticated("attestation", "challenge-1"));

        assertTrue(challengeService.consumed.get(), "the one-time challenge must be consumed");
        assertEquals(1, validationService.attestationCalls.get(), "attestation must be validated exactly once");
        assertTrue(result.isAuthenticated());

        AppAttestAttestationRegistrationAuthenticationToken token =
                (AppAttestAttestationRegistrationAuthenticationToken) result;
        assertSame(registration, token.getPrincipal(), "principal is the App instance registration, not a user");
        assertEquals("kid-1", token.getKeyId());
        assertTrue(token.getAuthorities().isEmpty(), "App instance registration grants no user authorities");
    }

    @Test
    void surfacesAnAlreadyRegisteredKeyWithoutCreatingAUser() {
        RecordingChallengeService challengeService = new RecordingChallengeService(true);
        AppAttestAttestationRegistration existing = registration("kid-1", "client-1");
        FakeValidationService validationService = new FakeValidationService(existing);
        AppAttestAttestationRegistrationAuthenticationProvider provider =
                new AppAttestAttestationRegistrationAuthenticationProvider(challengeService, validationService);

        Authentication result = provider.authenticate(
                AppAttestAttestationRegistrationAuthenticationToken.unauthenticated("attestation", "challenge-1"));

        assertSame(existing, ((AppAttestAttestationRegistrationAuthenticationToken) result).getPrincipal(),
                "an idempotently returned existing registration is surfaced unchanged");
    }

    @Test
    void invalidChallengeIsRejectedBeforeValidation() {
        RecordingChallengeService challengeService = new RecordingChallengeService(false);
        FakeValidationService validationService = new FakeValidationService(registration("kid-1", null));
        AppAttestAttestationRegistrationAuthenticationProvider provider =
                new AppAttestAttestationRegistrationAuthenticationProvider(challengeService, validationService);

        assertThrows(BadCredentialsException.class, () -> provider.authenticate(
                AppAttestAttestationRegistrationAuthenticationToken.unauthenticated("attestation", "bad")));
        assertEquals(0, validationService.attestationCalls.get(),
                "attestation must not be validated when the challenge is invalid");
    }

    // ---- fakes ----

    static class RecordingChallengeService implements ChallengeService {

        final AtomicBoolean consumed = new AtomicBoolean(false);
        private final boolean valid;

        RecordingChallengeService(boolean valid) {
            this.valid = valid;
        }

        @Override
        public GeneratedChallenge generateChallenge() {
            return new GeneratedChallenge("challenge-1");
        }

        @Override
        public boolean consumeChallenge(String challenge) {
            this.consumed.set(true);
            return this.valid;
        }
    }

    static class FakeValidationService implements AppleAppAttestValidationService {

        final AtomicInteger attestationCalls = new AtomicInteger();
        final AtomicInteger assertionCalls = new AtomicInteger();
        private final AppAttestAttestationRegistration registration;

        FakeValidationService(AppAttestAttestationRegistration registration) {
            this.registration = registration;
        }

        @Override
        public AppAttestAttestationRegistration validateAttestation(String attestation, String challenge) {
            this.attestationCalls.incrementAndGet();
            return this.registration;
        }

        @Override
        public AppAttestAttestationRegistration validateAssertion(String keyId, String assertion, String challenge) {
            this.assertionCalls.incrementAndGet();
            return this.registration;
        }
    }
}
