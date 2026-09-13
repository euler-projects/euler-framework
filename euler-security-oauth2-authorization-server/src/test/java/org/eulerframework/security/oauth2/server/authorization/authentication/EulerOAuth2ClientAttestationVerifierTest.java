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

package org.eulerframework.security.oauth2.server.authorization.authentication;

import org.eulerframework.security.authentication.ChallengeService;
import org.eulerframework.security.authentication.GeneratedChallenge;
import org.eulerframework.security.authentication.InMemoryNonceService;
import org.eulerframework.security.authentication.appattest.AppAttestAttestationRegistration;
import org.eulerframework.security.authentication.appattest.apple.AppleAppAttestValidationService;
import org.eulerframework.security.oauth2.core.EulerOAuth2ClientAttestationType;
import org.eulerframework.security.oauth2.core.EulerOAuth2ErrorCodes;
import org.eulerframework.security.oauth2.core.endpoint.EulerOAuth2HeaderNames;
import org.eulerframework.security.oauth2.core.endpoint.EulerOAuth2ParameterNames;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the Apple App Attest branch of {@link EulerOAuth2ClientAttestationVerifier#verify(Map)},
 * the single verification entry point shared by the basic path (through
 * {@link EulerOAuth2ClientAttestationAuthenticationProvider}) and the enhanced path (through the
 * post-authentication filter). Covers the three valid {@code attestation} / {@code assertion}
 * combinations, the key ID each requires, the one-time challenge being consumed exactly once, the
 * bound-client requirement, and the RFC 6749 Section 6.3 {@code client_id} consistency check.
 */
class EulerOAuth2ClientAttestationVerifierTest {

    private static final String CLIENT_ID = "client-1";

    /**
     * The key ID an attestation yields: derived from the attestation's credentialId, never taken
     * from the request.
     */
    private static final String DERIVED_KID = "kid-derived";

    private static final String SUPPLIED_KID = "kid-supplied";

    @Test
    void attestationOnlyRegistersTheDeviceWithoutRequiringKid() {
        RecordingValidationService validationService = new RecordingValidationService();
        EulerOAuth2ClientAttestationVerifier verifier = verifier(validationService);

        Map<String, Object> params = appleParams();
        params.put(EulerOAuth2ParameterNames.ATTESTATION, "attestation-1");

        EulerOAuth2ClientAttestationVerifier.ClientAttestationVerification result = verifier.verify(params);

        assertEquals(1, validationService.attestationCalls.get());
        assertEquals(0, validationService.assertionCalls.get(),
                "no assertion was supplied, so nothing else must be verified");
        assertEquals(CLIENT_ID, result.clientId());
        assertSame(validationService.lastAttestationResult, result.registration());
    }

    @Test
    void assertionOnlyVerifiesTheSuppliedKid() {
        RecordingValidationService validationService = new RecordingValidationService();
        EulerOAuth2ClientAttestationVerifier verifier = verifier(validationService);

        Map<String, Object> params = appleParams();
        params.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_ASSERTION, "assertion-1");
        params.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_KID, SUPPLIED_KID);

        EulerOAuth2ClientAttestationVerifier.ClientAttestationVerification result = verifier.verify(params);

        assertEquals(0, validationService.attestationCalls.get(),
                "no attestation was supplied, so no device registration must happen");
        assertEquals(List.of(SUPPLIED_KID), validationService.assertionKeyIds);
        assertEquals(CLIENT_ID, result.clientId());
    }

    @Test
    void assertionOnlyWithoutKidIsRejected() {
        RecordingValidationService validationService = new RecordingValidationService();
        EulerOAuth2ClientAttestationVerifier verifier = verifier(validationService);

        Map<String, Object> params = appleParams();
        params.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_ASSERTION, "assertion-1");

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> verifier.verify(params));

        assertEquals(EulerOAuth2ErrorCodes.INVALID_CLIENT_ATTESTATION, ex.getError().getErrorCode());
        assertEquals(0, validationService.assertionCalls.get(),
                "an assertion cannot be verified without a key ID to locate the device");
    }

    @Test
    void attestationAndAssertionCompletesBothStepsWithTheDerivedKid() {
        RecordingValidationService validationService = new RecordingValidationService();
        EulerOAuth2ClientAttestationVerifier verifier = verifier(validationService);

        Map<String, Object> params = appleParams();
        params.put(EulerOAuth2ParameterNames.ATTESTATION, "attestation-1");
        params.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_ASSERTION, "assertion-1");

        EulerOAuth2ClientAttestationVerifier.ClientAttestationVerification result = verifier.verify(params);

        assertEquals(1, validationService.attestationCalls.get(), "the device KEY is registered first");
        assertEquals(List.of(DERIVED_KID), validationService.assertionKeyIds,
                "the assertion must be verified against the kid derived from the attestation");
        assertSame(validationService.lastAssertionResult, result.registration(),
                "the post-assertion registration is the one handed downstream");
    }

    @Test
    void suppliedKidIsIgnoredWhenAttestationIsPresent() {
        RecordingValidationService validationService = new RecordingValidationService();
        EulerOAuth2ClientAttestationVerifier verifier = verifier(validationService);

        Map<String, Object> params = appleParams();
        params.put(EulerOAuth2ParameterNames.ATTESTATION, "attestation-1");
        params.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_ASSERTION, "assertion-1");
        params.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_KID, SUPPLIED_KID);

        verifier.verify(params);

        assertEquals(List.of(DERIVED_KID), validationService.assertionKeyIds,
                "the attestation is the authoritative source of the kid");
    }

    @Test
    void combinedRequestConsumesTheChallengeExactlyOnce() {
        RecordingValidationService validationService = new RecordingValidationService();
        RecordingChallengeService challengeService = new RecordingChallengeService(true);
        EulerOAuth2ClientAttestationVerifier verifier = verifier(validationService, challengeService);

        Map<String, Object> params = appleParams();
        params.put(EulerOAuth2ParameterNames.ATTESTATION, "attestation-1");
        params.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_ASSERTION, "assertion-1");

        verifier.verify(params);

        assertEquals(1, challengeService.consumeCalls.get(),
                "a single challenge backs both the attestation and the assertion, so it is consumed once");
        assertEquals(1, validationService.attestationCalls.get());
        assertEquals(1, validationService.assertionCalls.get());
    }

    @Test
    void reusedOrExpiredChallengeIsRejectedBeforeVerification() {
        RecordingValidationService validationService = new RecordingValidationService();
        EulerOAuth2ClientAttestationVerifier verifier =
                verifier(validationService, new RecordingChallengeService(false));

        Map<String, Object> params = appleParams();
        params.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_ASSERTION, "assertion-1");
        params.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_KID, SUPPLIED_KID);

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> verifier.verify(params));

        assertEquals(EulerOAuth2ErrorCodes.INVALID_CLIENT_ATTESTATION, ex.getError().getErrorCode());
        assertEquals(0, validationService.assertionCalls.get(),
                "verification must not run once the challenge is spent");
    }

    @Test
    void missingAttestationAndAssertionIsRejectedWithoutConsumingTheChallenge() {
        RecordingValidationService validationService = new RecordingValidationService();
        RecordingChallengeService challengeService = new RecordingChallengeService(true);
        EulerOAuth2ClientAttestationVerifier verifier = verifier(validationService, challengeService);

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> verifier.verify(appleParams()));

        assertEquals(EulerOAuth2ErrorCodes.INVALID_CLIENT_ATTESTATION, ex.getError().getErrorCode());
        assertEquals(0, validationService.attestationCalls.get());
        assertEquals(0, validationService.assertionCalls.get());
        assertEquals(0, challengeService.consumeCalls.get(),
                "a malformed request must not burn an otherwise valid one-time challenge");
    }

    @Test
    void attestationForAnAppWithoutABoundClientIsRejectedAsUnauthorizedClient() {
        RecordingValidationService validationService = new RecordingValidationService();
        validationService.boundClientId = null; // DYNAMIC app before dynamic client registration
        EulerOAuth2ClientAttestationVerifier verifier = verifier(validationService);

        Map<String, Object> params = appleParams();
        params.put(EulerOAuth2ParameterNames.ATTESTATION, "attestation-1");

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> verifier.verify(params));

        assertEquals(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT, ex.getError().getErrorCode(),
                "the App Attest proof was valid; the app simply has no client to authenticate as");
        assertEquals(1, validationService.attestationCalls.get(),
                "the attestation is still verified, which is what registers the device KEY");
    }

    @Test
    void combinedRequestForAnAppWithoutABoundClientFailsBeforeVerifyingTheAssertion() {
        RecordingValidationService validationService = new RecordingValidationService();
        validationService.boundClientId = null;
        EulerOAuth2ClientAttestationVerifier verifier = verifier(validationService);

        Map<String, Object> params = appleParams();
        params.put(EulerOAuth2ParameterNames.ATTESTATION, "attestation-1");
        params.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_ASSERTION, "assertion-1");

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> verifier.verify(params));

        assertEquals(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT, ex.getError().getErrorCode());
        assertEquals(0, validationService.assertionCalls.get(),
                "no point verifying the assertion once the request cannot succeed");
    }

    @Test
    void assertionOnlyForAnAppWithoutABoundClientIsRejectedAsUnauthorizedClient() {
        RecordingValidationService validationService = new RecordingValidationService();
        validationService.boundClientId = null;
        EulerOAuth2ClientAttestationVerifier verifier = verifier(validationService);

        Map<String, Object> params = appleParams();
        params.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_ASSERTION, "assertion-1");
        params.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_KID, SUPPLIED_KID);

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> verifier.verify(params));

        assertEquals(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT, ex.getError().getErrorCode());
        assertEquals(List.of(SUPPLIED_KID), validationService.assertionKeyIds);
    }

    @Test
    void requestIdThatDisagreesWithTheAttestationIsRejected() {
        RecordingValidationService validationService = new RecordingValidationService();
        EulerOAuth2ClientAttestationVerifier verifier = verifier(validationService);

        Map<String, Object> params = appleParams();
        params.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_ASSERTION, "assertion-1");
        params.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_KID, SUPPLIED_KID);
        params.put(OAuth2ParameterNames.CLIENT_ID, "some-other-client");

        // Draft Section 6.3: a client_id carried in the request must match the one the attestation
        // resolves to.
        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> verifier.verify(params));

        assertEquals(OAuth2ErrorCodes.INVALID_CLIENT, ex.getError().getErrorCode());
    }

    // ---- helpers ----

    private static Map<String, Object> appleParams() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_TYPE,
                EulerOAuth2ClientAttestationType.APPLE_APP_ATTEST);
        params.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_CHALLENGE, "challenge-1");
        return params;
    }

    private static EulerOAuth2ClientAttestationVerifier verifier(AppleAppAttestValidationService validationService) {
        return verifier(validationService, new RecordingChallengeService(true));
    }

    private static EulerOAuth2ClientAttestationVerifier verifier(
            AppleAppAttestValidationService validationService, ChallengeService challengeService) {
        EulerOAuth2ClientAttestationVerifier verifier =
                new EulerOAuth2ClientAttestationVerifier(challengeService, new InMemoryNonceService());
        verifier.setAppleAppAttestValidationService(validationService);
        return verifier;
    }

    private static AppAttestAttestationRegistration registration(String keyId, String clientId) {
        return new AppAttestAttestationRegistration(keyId, "ABCD1234EF", "com.example.app", clientId,
                null, null, null, null, null, null, 0);
    }

    // ---- fakes ----

    static class RecordingChallengeService implements ChallengeService {

        final AtomicInteger consumeCalls = new AtomicInteger();
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
            this.consumeCalls.incrementAndGet();
            return this.valid;
        }
    }

    static class RecordingValidationService implements AppleAppAttestValidationService {

        final AtomicInteger attestationCalls = new AtomicInteger();
        final AtomicInteger assertionCalls = new AtomicInteger();
        final List<String> assertionKeyIds = new ArrayList<>();
        volatile AppAttestAttestationRegistration lastAttestationResult;
        volatile AppAttestAttestationRegistration lastAssertionResult;

        /**
         * The {@code client_id} bound to the registration. {@code null} simulates a DYNAMIC app that
         * has not completed dynamic client registration yet, or an app that is not OAuth2-enabled.
         */
        volatile String boundClientId = CLIENT_ID;

        @Override
        public AppAttestAttestationRegistration validateAttestation(String attestation, String challenge) {
            this.attestationCalls.incrementAndGet();
            this.lastAttestationResult = registration(DERIVED_KID, this.boundClientId);
            return this.lastAttestationResult;
        }

        @Override
        public AppAttestAttestationRegistration validateAssertion(String keyId, String assertion, String challenge) {
            this.assertionCalls.incrementAndGet();
            this.assertionKeyIds.add(keyId);
            this.lastAssertionResult = registration(keyId, this.boundClientId);
            return this.lastAssertionResult;
        }
    }
}
