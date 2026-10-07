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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link AppAttestInstanceKeyRegistrationAuthenticationProvider}: what an
 * authenticated App instance may register a key under, what it has to say about the key it
 * submits, and which submissions are turned away.
 */
class AppAttestInstanceKeyRegistrationAuthenticationProviderTest {

    private static final String ATTEST_KID = "attest-kid-1";
    private static final String OTHER_ATTEST_KID = "attest-kid-2";
    private static final String CLIENT_ID = "client-1";
    private static final String CHALLENGE = "challenge-1";
    private static final String ASSERTION = "assertion-1";
    private static final String KEY_ID = "client-chosen-1";
    private static final String OTHER_KEY_ID = "client-chosen-2";

    @Test
    void registersTheKeyUnderTheAuthenticatedAppAttestKey() throws Exception {
        RecordingChallengeService challengeService = new RecordingChallengeService(true);
        InMemoryAppAttestInstanceKeyRegistrationService instanceKeyRegistrationService =
                new InMemoryAppAttestInstanceKeyRegistrationService();
        JWK submitted = new ECKeyGenerator(Curve.P_256).generate();

        Authentication result = provider(challengeService, registration(CLIENT_ID), instanceKeyRegistrationService)
                .authenticate(token(submit(submitted, KEY_ID)));

        assertTrue(challengeService.consumed.get(), "the one-time challenge must be consumed");
        assertTrue(result.isAuthenticated());

        AppAttestInstanceKeyRegistration keyRegistration =
                ((AppAttestInstanceKeyRegistrationAuthenticationToken) result).getRegistration();
        assertEquals(ATTEST_KID, keyRegistration.appAttestKid(),
                "the key is filed under the App Attest KEY that proved the caller, taken from the"
                        + " verified registration rather than from the request header");
        assertEquals(KEY_ID, keyRegistration.jwkKid(), "the caller's own key ID is kept as given");
        assertEquals(KEY_ID, JWK.parse(keyRegistration.jwk()).getKeyID(),
                "and the stored JWK carries it, so the row and the response agree on it");

        assertSame(keyRegistration,
                instanceKeyRegistrationService.findByAppAttestKidAndJwkKid(ATTEST_KID, KEY_ID));
        assertTrue(result.getAuthorities().isEmpty(), "key registration grants no user authorities");
    }

    /**
     * The key ID is a name, not a fingerprint. Nothing about an account is bound to it &mdash; that
     * is the key's own thumbprint, which the identity backend derives &mdash; so pinning it to the
     * thumbprint here would only make the identifier carry a second meaning it does not need.
     */
    @Test
    void theKeyIdIsNotDerivedFromTheKeyMaterial() throws Exception {
        InMemoryAppAttestInstanceKeyRegistrationService instanceKeyRegistrationService =
                new InMemoryAppAttestInstanceKeyRegistrationService();
        JWK submitted = new ECKeyGenerator(Curve.P_256).generate();

        AppAttestInstanceKeyRegistration keyRegistration =
                authenticate(provider(new RecordingChallengeService(true), registration(CLIENT_ID),
                        instanceKeyRegistrationService), submitted, KEY_ID).getRegistration();

        assertNotEquals(JwkUtils.computeThumbprint(JwkUtils.toPublicJwk(submitted)), keyRegistration.jwkKid());
    }

    @Test
    void persistsNoPrivateKeyMaterial() throws Exception {
        InMemoryAppAttestInstanceKeyRegistrationService instanceKeyRegistrationService =
                new InMemoryAppAttestInstanceKeyRegistrationService();
        ECKey withPrivateHalf = new ECKeyGenerator(Curve.P_256).keyID(KEY_ID).generate();

        provider(new RecordingChallengeService(true), registration(CLIENT_ID), instanceKeyRegistrationService)
                .authenticate(token(withPrivateHalf.toJSONString()));

        AppAttestInstanceKeyRegistration stored =
                instanceKeyRegistrationService.findByAppAttestKidAndJwkKid(ATTEST_KID, KEY_ID);
        assertFalse(JWK.parse(stored.jwk()).isPrivate(),
                "the column is meant to be readable, so no private member may reach it");
    }

    /**
     * One instance may hold several keys, and each is its own entry: the key ID addresses a row
     * and nothing else, so two keys are two rows however alike or unlike their identifiers are.
     */
    @Test
    void aSecondKeyUnderANewIdIsItsOwnEntry() throws Exception {
        InMemoryAppAttestInstanceKeyRegistrationService instanceKeyRegistrationService =
                new InMemoryAppAttestInstanceKeyRegistrationService();
        AppAttestInstanceKeyRegistrationAuthenticationProvider provider =
                provider(new RecordingChallengeService(true), registration(CLIENT_ID), instanceKeyRegistrationService);

        AppAttestInstanceKeyRegistration first =
                authenticate(provider, new ECKeyGenerator(Curve.P_256).generate(), KEY_ID).getRegistration();
        AppAttestInstanceKeyRegistration second =
                authenticate(provider, new ECKeyGenerator(Curve.P_256).generate(), OTHER_KEY_ID).getRegistration();

        assertNotEquals(first.jwk(), second.jwk());
        assertEquals(first.jwk(), instanceKeyRegistrationService
                .findByAppAttestKidAndJwkKid(ATTEST_KID, KEY_ID).jwk());
        assertEquals(second.jwk(), instanceKeyRegistrationService
                .findByAppAttestKidAndJwkKid(ATTEST_KID, OTHER_KEY_ID).jwk());
    }

    /**
     * Registration is a {@code POST} and behaves like one, including against a repeat of the very
     * same submission. Settling a collision by overwriting would let a caller discard the key an
     * account may already be bound to, and leave that account unreachable with nothing recording
     * why. What a caller loses by being refused is nothing it did not already have: it chose the
     * key ID, so it does not need the response to learn it.
     */
    @Test
    void registeringTheSameKeyIdAgainIsAConflict() throws Exception {
        InMemoryAppAttestInstanceKeyRegistrationService instanceKeyRegistrationService =
                new InMemoryAppAttestInstanceKeyRegistrationService();
        AppAttestInstanceKeyRegistrationAuthenticationProvider provider =
                provider(new RecordingChallengeService(true), registration(CLIENT_ID), instanceKeyRegistrationService);
        ECKey key = new ECKeyGenerator(Curve.P_256).generate();

        AppAttestInstanceKeyRegistration first = authenticate(provider, key, KEY_ID).getRegistration();
        DuplicateInstanceKeyException ex = assertThrows(DuplicateInstanceKeyException.class,
                () -> authenticate(provider, key, KEY_ID));

        assertEquals(KEY_ID, ex.getJwkKid());
        assertEquals(first.jwk(), instanceKeyRegistrationService
                        .findByAppAttestKidAndJwkKid(ATTEST_KID, KEY_ID).jwk(),
                "the refused repeat leaves the registered key exactly as it was");
    }

    /**
     * The key ID is unique across the whole registry, not merely within one instance, so what one
     * instance took is taken for everybody. Cheap to live with - a caller generates a fresh
     * identifier and moves on - and it is what keeps one identifier meaning one key everywhere.
     */
    @Test
    void aKeyIdAnotherInstanceTookIsAConflict() throws Exception {
        InMemoryAppAttestInstanceKeyRegistrationService instanceKeyRegistrationService =
                new InMemoryAppAttestInstanceKeyRegistrationService();
        ECKey key = new ECKeyGenerator(Curve.P_256).generate();

        authenticate(provider(new RecordingChallengeService(true), registration(CLIENT_ID),
                instanceKeyRegistrationService), key, KEY_ID);

        AppAttestInstanceKeyRegistrationAuthenticationProvider otherInstance =
                provider(new RecordingChallengeService(true),
                        registration(OTHER_ATTEST_KID, CLIENT_ID), instanceKeyRegistrationService);
        ECKey otherKey = new ECKeyGenerator(Curve.P_256).generate();
        assertThrows(DuplicateInstanceKeyException.class,
                () -> otherInstance.authenticate(token(OTHER_ATTEST_KID, submit(otherKey, KEY_ID))));
    }

    /**
     * The key ID is the only thing a later assertion can quote to reach this row, so a submission
     * without one could be stored and never addressed again.
     */
    @Test
    void rejectsAKeyWithNoId() throws Exception {
        JWK submitted = new ECKeyGenerator(Curve.P_256).generate();

        assertThrows(InvalidInstanceKeyException.class, () ->
                provider(new RecordingChallengeService(true), registration(CLIENT_ID),
                        new InMemoryAppAttestInstanceKeyRegistrationService())
                        .authenticate(token(submitWithoutPrivateMembers(submitted))));
    }

    @Test
    void rejectsAnOverlongKeyId() throws Exception {
        JWK submitted = new ECKeyGenerator(Curve.P_256).generate();

        assertThrows(InvalidInstanceKeyException.class, () ->
                provider(new RecordingChallengeService(true), registration(CLIENT_ID),
                        new InMemoryAppAttestInstanceKeyRegistrationService())
                        .authenticate(token(submit(submitted, "k".repeat(129)))));
    }

    /** It is echoed into a JSON response and written into a log line, neither of which sanitises. */
    @Test
    void rejectsAKeyIdWithControlCharacters() throws Exception {
        JWK submitted = new ECKeyGenerator(Curve.P_256).generate();

        assertThrows(InvalidInstanceKeyException.class, () ->
                provider(new RecordingChallengeService(true), registration(CLIENT_ID),
                        new InMemoryAppAttestInstanceKeyRegistrationService())
                        .authenticate(token(submit(submitted, "key\ninjected: yes"))));
    }

    @Test
    void rejectsASymmetricKey() {
        OctetSequenceKey secret = new OctetSequenceKey.Builder(
                "01234567890123456789012345678901".getBytes(StandardCharsets.UTF_8))
                .keyID(KEY_ID)
                .build();

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
                        .authenticate(token(submit(submitted, KEY_ID))));
        assertEquals(0, validationService.assertionCalls.get(),
                "the assertion must not be validated when the challenge is invalid");
    }

    // ---- helpers ----

    private static AppAttestInstanceKeyRegistrationAuthenticationToken authenticate(
            AppAttestInstanceKeyRegistrationAuthenticationProvider provider, JWK key, String keyId) throws Exception {
        return (AppAttestInstanceKeyRegistrationAuthenticationToken) provider.authenticate(
                token(submit(key, keyId)));
    }

    /** What a client sends: the public half of the key it generated, under the ID it chose. */
    private static String submit(JWK key, String keyId) throws Exception {
        return JwkUtils.withKeyId(JwkUtils.toPublicJwk(key), keyId).toJSONString();
    }

    private static String submitWithoutPrivateMembers(JWK key) {
        return key.toPublicJWK().toJSONString();
    }

    private static AppAttestInstanceKeyRegistrationAuthenticationToken token(String publicKeyJson) {
        return token(ATTEST_KID, publicKeyJson);
    }

    private static AppAttestInstanceKeyRegistrationAuthenticationToken token(String attestKid, String publicKeyJson) {
        return AppAttestInstanceKeyRegistrationAuthenticationToken.unauthenticated(
                attestKid, CHALLENGE, ASSERTION, publicKeyJson);
    }

    private static AppAttestInstanceKeyRegistrationAuthenticationProvider provider(
            ChallengeService challengeService,
            AppAttestAttestationRegistration registration,
            AppAttestInstanceKeyRegistrationService instanceKeyRegistrationService) {
        return new AppAttestInstanceKeyRegistrationAuthenticationProvider(
                challengeService, new FakeValidationService(registration), instanceKeyRegistrationService);
    }

    private static AppAttestAttestationRegistration registration(String clientId) {
        return registration(ATTEST_KID, clientId);
    }

    private static AppAttestAttestationRegistration registration(String appAttestKid, String clientId) {
        return new AppAttestAttestationRegistration(
                appAttestKid, "ABCD1234EF", "com.example.app", clientId,
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
            assertEquals(this.registration.getKeyId(), keyId,
                    "an assertion carries no credential ID, so the caller supplies it");
            return this.registration;
        }
    }
}
