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

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import org.eulerframework.security.authentication.ChallengeService;
import org.eulerframework.security.authentication.GeneratedChallenge;
import org.eulerframework.security.authentication.appattest.apple.AppleAppAttestValidationService;
import org.eulerframework.security.util.JwkUtils;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link AppAttestInstanceKeyRegistrationAuthenticationProvider}: what an
 * authenticated App instance may register a key under, how the key ID is derived, and
 * which submitted keys are turned away.
 */
class AppAttestInstanceKeyRegistrationAuthenticationProviderTest {

    private static final String ATTEST_KID = "attest-kid-1";
    private static final String CLIENT_ID = "client-1";
    private static final String CHALLENGE = "challenge-1";
    private static final String ASSERTION = "assertion-1";

    @Test
    void registersTheKeyUnderTheAuthenticatedAppAttestKey() throws Exception {
        RecordingChallengeService challengeService = new RecordingChallengeService(true);
        InMemoryAppAttestInstanceKeyRegistrationService instanceKeyRegistrationService =
                new InMemoryAppAttestInstanceKeyRegistrationService();
        JWK submitted = new ECKeyGenerator(Curve.P_256).keyID("caller-chosen").generate();

        Authentication result = provider(challengeService, registration(CLIENT_ID), instanceKeyRegistrationService)
                .authenticate(token(submitWithoutPrivateMembers(submitted)));

        assertTrue(challengeService.consumed.get(), "the one-time challenge must be consumed");
        assertTrue(result.isAuthenticated());

        AppAttestInstanceKeyRegistration instanceKey =
                ((AppAttestInstanceKeyRegistrationAuthenticationToken) result).getRegistration();
        assertEquals(ATTEST_KID, instanceKey.appAttestKid(),
                "the key is filed under the App Attest KEY that proved the caller, taken from the"
                        + " verified registration rather than from the request header");
        // The kid is derived from the key material, not taken from the caller, so it is the
        // thumbprint - and the value a later consumer has to name.
        String expectedJwkKid = JwkUtils.computeThumbprint(JwkUtils.toPublicJwk(submitted));
        assertEquals(expectedJwkKid, instanceKey.jwkKid());
        assertFalse("caller-chosen".equals(JWK.parse(instanceKey.jwk()).getKeyID()),
                "a caller-chosen kid cannot be trusted to be unique, so it is replaced");
        assertEquals(expectedJwkKid, JWK.parse(instanceKey.jwk()).getKeyID());

        assertSame(instanceKey, instanceKeyRegistrationService.findByAppAttestKidAndJwkKid(ATTEST_KID, expectedJwkKid));
        assertTrue(result.getAuthorities().isEmpty(), "key registration grants no user authorities");
    }

    @Test
    void persistsNoPrivateKeyMaterial() throws Exception {
        InMemoryAppAttestInstanceKeyRegistrationService instanceKeyRegistrationService =
                new InMemoryAppAttestInstanceKeyRegistrationService();
        JWK withPrivateHalf = new ECKeyGenerator(Curve.P_256).generate();

        provider(new RecordingChallengeService(true), registration(CLIENT_ID), instanceKeyRegistrationService)
                .authenticate(token(withPrivateHalf.toJSONString()));

        AppAttestInstanceKeyRegistration stored = instanceKeyRegistrationService.findByAppAttestKidAndJwkKid(
                ATTEST_KID, JwkUtils.computeThumbprint(JwkUtils.toPublicJwk(withPrivateHalf)));
        assertFalse(JWK.parse(stored.jwk()).isPrivate(),
                "the column is meant to be readable, so no private member may reach it");
    }

    @Test
    void registeringTheSameKeyTwiceLeavesOneEntry() throws Exception {
        InMemoryAppAttestInstanceKeyRegistrationService instanceKeyRegistrationService =
                new InMemoryAppAttestInstanceKeyRegistrationService();
        JWK submitted = new ECKeyGenerator(Curve.P_256).generate();
        AppAttestInstanceKeyRegistrationAuthenticationProvider provider =
                provider(new RecordingChallengeService(true), registration(CLIENT_ID), instanceKeyRegistrationService);

        AppAttestInstanceKeyRegistration first = authenticate(provider, submitted).getRegistration();
        AppAttestInstanceKeyRegistration second = authenticate(provider, submitted).getRegistration();

        // A retry of a registration that already succeeded presents the same key material, so
        // it lands on the same derived key ID and must not add a second entry.
        assertEquals(first.jwkKid(), second.jwkKid());
        assertEquals(first.jwk(), second.jwk());
    }

    /**
     * A repeat registration replaces the entry rather than being ignored by it, and the
     * response reports what the store now holds. The thumbprint covers a key's required
     * members only, so the same key presented with a different {@code alg} lands on the same
     * entry - and echoing the request would have left the two descriptions of one key
     * disagreeing with no way to say which was registered.
     */
    @Test
    void aRepeatRegistrationOverwritesAndReportsWhatIsStored() throws Exception {
        InMemoryAppAttestInstanceKeyRegistrationService instanceKeyRegistrationService =
                new InMemoryAppAttestInstanceKeyRegistrationService();
        ECKey key = new ECKeyGenerator(Curve.P_256).generate();
        AppAttestInstanceKeyRegistrationAuthenticationProvider provider =
                provider(new RecordingChallengeService(true), registration(CLIENT_ID), instanceKeyRegistrationService);

        AppAttestInstanceKeyRegistration first = authenticate(provider, key).getRegistration();
        assertNull(JWK.parse(first.jwk()).getAlgorithm(), "the fixture's first submission declared none");

        ECKey sameKeyDeclaringAnAlgorithm =
                new ECKey.Builder(key.toPublicJWK()).algorithm(JWSAlgorithm.ES256).build();
        AppAttestInstanceKeyRegistration second = authenticate(provider, sameKeyDeclaringAnAlgorithm).getRegistration();

        assertEquals(first.jwkKid(), second.jwkKid(), "the same key material derives the same key ID");
        assertEquals(JWSAlgorithm.ES256, JWK.parse(second.jwk()).getAlgorithm(),
                "the latest registration is the one in force");
        assertEquals(second.jwk(),
                instanceKeyRegistrationService.findByAppAttestKidAndJwkKid(ATTEST_KID, first.jwkKid()).jwk(),
                "the response is the persisted row, not the submitted one");
    }

    /**
     * Overwriting is safe because of what identifies the entry: the key ID is the key's own
     * fingerprint, so a repeat cannot change the key material and a different key cannot reach
     * an existing entry at all. Only metadata the thumbprint does not cover is rewritable, and
     * no signature is verified against that.
     */
    @Test
    void anOverwriteCannotChangeTheKeyMaterialAndADifferentKeyCannotOverwrite() throws Exception {
        InMemoryAppAttestInstanceKeyRegistrationService instanceKeyRegistrationService =
                new InMemoryAppAttestInstanceKeyRegistrationService();
        ECKey key = new ECKeyGenerator(Curve.P_256).generate();
        ECKey otherKey = new ECKeyGenerator(Curve.P_256).generate();
        AppAttestInstanceKeyRegistrationAuthenticationProvider provider =
                provider(new RecordingChallengeService(true), registration(CLIENT_ID), instanceKeyRegistrationService);

        AppAttestInstanceKeyRegistration first = authenticate(provider, key).getRegistration();
        AppAttestInstanceKeyRegistration resubmitted = authenticate(provider,
                new ECKey.Builder(key.toPublicJWK()).algorithm(JWSAlgorithm.ES256).build()).getRegistration();
        AppAttestInstanceKeyRegistration different = authenticate(provider, otherKey).getRegistration();

        // The overwrite left the key itself alone...
        assertEquals(JWK.parse(first.jwk()).toECKey().getX(), JWK.parse(resubmitted.jwk()).toECKey().getX());
        assertEquals(JWK.parse(first.jwk()).toECKey().getY(), JWK.parse(resubmitted.jwk()).toECKey().getY());
        // ...and it landed on the first key's own entry, which now holds the latest
        // description of that key rather than the first one.
        assertEquals(resubmitted.jwk(),
                instanceKeyRegistrationService.findByAppAttestKidAndJwkKid(ATTEST_KID, first.jwkKid()).jwk());

        // A different key derives a different key ID, so it is its own entry and reaches
        // nothing that was already registered.
        assertFalse(first.jwkKid().equals(different.jwkKid()));
        assertEquals(different.jwk(),
                instanceKeyRegistrationService.findByAppAttestKidAndJwkKid(ATTEST_KID, different.jwkKid()).jwk());
        assertEquals(resubmitted.jwk(),
                instanceKeyRegistrationService.findByAppAttestKidAndJwkKid(ATTEST_KID, first.jwkKid()).jwk(),
                "registering another key must not disturb the one already registered");
    }

    /**
     * The registry is the App Attest domain's, and that domain has no OAuth2 vocabulary: the
     * instance is identified by its App Attest KEY alone. An instance that never completed
     * dynamic client registration therefore still registers its keys, and nothing about the
     * entry records a client.
     */
    @Test
    void registersAKeyForAnInstanceWithNoOAuthClientBound() throws Exception {
        InMemoryAppAttestInstanceKeyRegistrationService instanceKeyRegistrationService =
                new InMemoryAppAttestInstanceKeyRegistrationService();
        JWK submitted = new ECKeyGenerator(Curve.P_256).generate();

        Authentication result = provider(new RecordingChallengeService(true), registration(null),
                instanceKeyRegistrationService)
                .authenticate(token(submitWithoutPrivateMembers(submitted)));

        assertTrue(result.isAuthenticated());
        AppAttestInstanceKeyRegistration instanceKey =
                ((AppAttestInstanceKeyRegistrationAuthenticationToken) result).getRegistration();
        assertEquals(ATTEST_KID, instanceKey.appAttestKid());
        assertSame(instanceKey, instanceKeyRegistrationService.findByAppAttestKidAndJwkKid(
                ATTEST_KID, JwkUtils.computeThumbprint(JwkUtils.toPublicJwk(submitted))));
    }

    @Test
    void rejectsASymmetricKey() {
        OctetSequenceKey secret = new OctetSequenceKey.Builder(
                "01234567890123456789012345678901".getBytes(StandardCharsets.UTF_8)).build();

        assertThrows(InvalidInstanceKeyException.class, () ->
                provider(new RecordingChallengeService(true), registration(CLIENT_ID),
                        new InMemoryAppAttestInstanceKeyRegistrationService())
                        .authenticate(token(secret.toJSONString())));
    }

    @Test
    void rejectsABodyThatIsNotAJwk() {
        assertThrows(InvalidInstanceKeyException.class, () ->
                provider(new RecordingChallengeService(true), registration(CLIENT_ID),
                        new InMemoryAppAttestInstanceKeyRegistrationService())
                        .authenticate(token("{\"not\":\"a jwk\"}")));
    }

    @Test
    void checksTheSubmittedKeyBeforeSpendingTheChallenge() {
        RecordingChallengeService challengeService = new RecordingChallengeService(true);

        // A request that could never succeed is answered without consuming the one-time
        // challenge, so the client does not have to fetch a fresh one to retry.
        assertThrows(InvalidInstanceKeyException.class, () ->
                provider(challengeService, registration(CLIENT_ID),
                        new InMemoryAppAttestInstanceKeyRegistrationService())
                        .authenticate(token("{\"not\":\"a jwk\"}")));

        assertFalse(challengeService.consumed.get());
    }

    @Test
    void invalidChallengeIsRejected() throws Exception {
        FakeValidationService validationService = new FakeValidationService(registration(CLIENT_ID));
        JWK submitted = new ECKeyGenerator(Curve.P_256).generate();

        assertThrows(BadCredentialsException.class, () ->
                new AppAttestInstanceKeyRegistrationAuthenticationProvider(
                        new RecordingChallengeService(false), validationService,
                        new InMemoryAppAttestInstanceKeyRegistrationService())
                        .authenticate(token(submitWithoutPrivateMembers(submitted))));
        assertEquals(0, validationService.assertionCalls.get(),
                "the assertion must not be validated when the challenge is invalid");
    }

    // ---- helpers ----

    private static AppAttestInstanceKeyRegistrationAuthenticationToken authenticate(
            AppAttestInstanceKeyRegistrationAuthenticationProvider provider, JWK submitted) {
        return (AppAttestInstanceKeyRegistrationAuthenticationToken) provider.authenticate(
                token(submitWithoutPrivateMembers(submitted)));
    }

    /** What a client sends: the public half of the key it generated. */
    private static String submitWithoutPrivateMembers(JWK key) {
        return key.toPublicJWK().toJSONString();
    }

    private static AppAttestInstanceKeyRegistrationAuthenticationToken token(String publicKeyJson) {
        return AppAttestInstanceKeyRegistrationAuthenticationToken.unauthenticated(
                ATTEST_KID, CHALLENGE, ASSERTION, publicKeyJson);
    }

    private static AppAttestInstanceKeyRegistrationAuthenticationProvider provider(
            ChallengeService challengeService,
            AppAttestAttestationRegistration registration,
            AppAttestInstanceKeyRegistrationService instanceKeyRegistrationService) {
        return new AppAttestInstanceKeyRegistrationAuthenticationProvider(
                challengeService, new FakeValidationService(registration), instanceKeyRegistrationService);
    }

    private static AppAttestAttestationRegistration registration(String clientId) {
        return new AppAttestAttestationRegistration(
                ATTEST_KID, "ABCD1234EF", "com.example.app", clientId,
                new byte[16], new byte[0], new byte[0], new byte[0],
                null, "{}", 0);
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
            return new GeneratedChallenge(CHALLENGE);
        }

        @Override
        public boolean consumeChallenge(String challenge) {
            this.consumed.set(true);
            return this.valid;
        }
    }

    static class FakeValidationService implements AppleAppAttestValidationService {

        final AtomicInteger assertionCalls = new AtomicInteger();
        private final AppAttestAttestationRegistration registration;

        FakeValidationService(AppAttestAttestationRegistration registration) {
            this.registration = registration;
        }

        @Override
        public AppAttestAttestationRegistration validateAttestation(String attestation, String challenge)
                throws AuthenticationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public AppAttestAttestationRegistration validateAssertion(String keyId, String assertion, String challenge)
                throws AuthenticationException {
            this.assertionCalls.incrementAndGet();
            assertEquals(ATTEST_KID, keyId, "an assertion carries no credential ID, so the caller supplies it");
            return this.registration;
        }
    }
}
