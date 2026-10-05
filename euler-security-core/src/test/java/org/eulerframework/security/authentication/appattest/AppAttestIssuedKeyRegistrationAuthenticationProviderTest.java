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
 * Tests for {@link AppAttestIssuedKeyRegistrationAuthenticationProvider}: what an
 * authenticated App instance may register a key under, how the key ID is derived, and
 * which submitted keys are turned away.
 */
class AppAttestIssuedKeyRegistrationAuthenticationProviderTest {

    private static final String ATTEST_KID = "attest-kid-1";
    private static final String CLIENT_ID = "client-1";
    private static final String CHALLENGE = "challenge-1";
    private static final String ASSERTION = "assertion-1";

    @Test
    void registersTheKeyUnderTheAuthenticatedClientsId() throws Exception {
        RecordingChallengeService challengeService = new RecordingChallengeService(true);
        InMemoryAppAttestIssuedKeyService issuedKeyService = new InMemoryAppAttestIssuedKeyService();
        JWK submitted = new ECKeyGenerator(Curve.P_256).keyID("caller-chosen").generate();

        Authentication result = provider(challengeService, registration(CLIENT_ID), issuedKeyService)
                .authenticate(token(submitWithoutPrivateMembers(submitted)));

        assertTrue(challengeService.consumed.get(), "the one-time challenge must be consumed");
        assertTrue(result.isAuthenticated());

        AppAttestIssuedKey issuedKey =
                ((AppAttestIssuedKeyRegistrationAuthenticationToken) result).getIssuedKey();
        assertEquals(CLIENT_ID, issuedKey.issuer(),
                "the key is registered under the client bound to the App Attest KEY, which is its iss");
        // The kid is derived from the key material, not taken from the caller, so it is the
        // thumbprint - and the value a later assertion has to name.
        String expectedKeyId = JwkUtils.computeThumbprint(JwkUtils.toPublicJwk(submitted));
        assertEquals(expectedKeyId, issuedKey.keyId());
        assertFalse("caller-chosen".equals(JWK.parse(issuedKey.jwk()).getKeyID()),
                "a caller-chosen kid cannot be trusted to be unique, so it is replaced");
        assertEquals(expectedKeyId, JWK.parse(issuedKey.jwk()).getKeyID());

        assertSame(issuedKey, issuedKeyService.findByIssuerAndKeyId(CLIENT_ID, expectedKeyId));
        assertTrue(result.getAuthorities().isEmpty(), "key registration grants no user authorities");
    }

    @Test
    void persistsNoPrivateKeyMaterial() throws Exception {
        InMemoryAppAttestIssuedKeyService issuedKeyService = new InMemoryAppAttestIssuedKeyService();
        JWK withPrivateHalf = new ECKeyGenerator(Curve.P_256).generate();

        provider(new RecordingChallengeService(true), registration(CLIENT_ID), issuedKeyService)
                .authenticate(token(withPrivateHalf.toJSONString()));

        AppAttestIssuedKey stored = issuedKeyService.findByIssuerAndKeyId(
                CLIENT_ID, JwkUtils.computeThumbprint(JwkUtils.toPublicJwk(withPrivateHalf)));
        assertFalse(JWK.parse(stored.jwk()).isPrivate(),
                "the column is meant to be readable, so no private member may reach it");
    }

    @Test
    void registeringTheSameKeyTwiceLeavesOneEntry() throws Exception {
        InMemoryAppAttestIssuedKeyService issuedKeyService = new InMemoryAppAttestIssuedKeyService();
        JWK submitted = new ECKeyGenerator(Curve.P_256).generate();
        AppAttestIssuedKeyRegistrationAuthenticationProvider provider =
                provider(new RecordingChallengeService(true), registration(CLIENT_ID), issuedKeyService);

        AppAttestIssuedKey first = authenticate(provider, submitted).getIssuedKey();
        AppAttestIssuedKey second = authenticate(provider, submitted).getIssuedKey();

        // A retry of a registration that already succeeded presents the same key material, so
        // it lands on the same derived key ID and must not add a second entry.
        assertEquals(first.keyId(), second.keyId());
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
        InMemoryAppAttestIssuedKeyService issuedKeyService = new InMemoryAppAttestIssuedKeyService();
        ECKey key = new ECKeyGenerator(Curve.P_256).generate();
        AppAttestIssuedKeyRegistrationAuthenticationProvider provider =
                provider(new RecordingChallengeService(true), registration(CLIENT_ID), issuedKeyService);

        AppAttestIssuedKey first = authenticate(provider, key).getIssuedKey();
        assertNull(JWK.parse(first.jwk()).getAlgorithm(), "the fixture's first submission declared none");

        ECKey sameKeyDeclaringAnAlgorithm =
                new ECKey.Builder(key.toPublicJWK()).algorithm(JWSAlgorithm.ES256).build();
        AppAttestIssuedKey second = authenticate(provider, sameKeyDeclaringAnAlgorithm).getIssuedKey();

        assertEquals(first.keyId(), second.keyId(), "the same key material derives the same key ID");
        assertEquals(JWSAlgorithm.ES256, JWK.parse(second.jwk()).getAlgorithm(),
                "the latest registration is the one in force");
        assertEquals(second.jwk(), issuedKeyService.findByIssuerAndKeyId(CLIENT_ID, first.keyId()).jwk(),
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
        InMemoryAppAttestIssuedKeyService issuedKeyService = new InMemoryAppAttestIssuedKeyService();
        ECKey key = new ECKeyGenerator(Curve.P_256).generate();
        ECKey otherKey = new ECKeyGenerator(Curve.P_256).generate();
        AppAttestIssuedKeyRegistrationAuthenticationProvider provider =
                provider(new RecordingChallengeService(true), registration(CLIENT_ID), issuedKeyService);

        AppAttestIssuedKey first = authenticate(provider, key).getIssuedKey();
        AppAttestIssuedKey resubmitted = authenticate(provider,
                new ECKey.Builder(key.toPublicJWK()).algorithm(JWSAlgorithm.ES256).build()).getIssuedKey();
        AppAttestIssuedKey different = authenticate(provider, otherKey).getIssuedKey();

        // The overwrite left the key itself alone...
        assertEquals(JWK.parse(first.jwk()).toECKey().getX(), JWK.parse(resubmitted.jwk()).toECKey().getX());
        assertEquals(JWK.parse(first.jwk()).toECKey().getY(), JWK.parse(resubmitted.jwk()).toECKey().getY());
        // ...and it landed on the first key's own entry, which now holds the latest
        // description of that key rather than the first one.
        assertEquals(resubmitted.jwk(), issuedKeyService.findByIssuerAndKeyId(CLIENT_ID, first.keyId()).jwk());

        // A different key derives a different key ID, so it is its own entry and reaches
        // nothing that was already registered.
        assertFalse(first.keyId().equals(different.keyId()));
        assertEquals(different.jwk(), issuedKeyService.findByIssuerAndKeyId(CLIENT_ID, different.keyId()).jwk());
        assertEquals(resubmitted.jwk(), issuedKeyService.findByIssuerAndKeyId(CLIENT_ID, first.keyId()).jwk(),
                "registering another key must not disturb the one already registered");
    }

    @Test
    void rejectsAKeyRegisteredUnderAnUnboundAppAttestKey() throws Exception {
        JWK submitted = new ECKeyGenerator(Curve.P_256).generate();

        // The App Attest KEY was attested but never dynamically registered, so there is no
        // client_id and therefore no issuer to register a key under.
        BadCredentialsException ex = assertThrows(BadCredentialsException.class, () ->
                provider(new RecordingChallengeService(true), registration(null),
                        new InMemoryAppAttestIssuedKeyService())
                        .authenticate(token(submitWithoutPrivateMembers(submitted))));

        assertTrue(ex.getMessage().contains("dynamic client registration"));
    }

    @Test
    void rejectsASymmetricKey() {
        OctetSequenceKey secret = new OctetSequenceKey.Builder(
                "01234567890123456789012345678901".getBytes(StandardCharsets.UTF_8)).build();

        assertThrows(InvalidIssuedKeyException.class, () ->
                provider(new RecordingChallengeService(true), registration(CLIENT_ID),
                        new InMemoryAppAttestIssuedKeyService())
                        .authenticate(token(secret.toJSONString())));
    }

    @Test
    void rejectsABodyThatIsNotAJwk() {
        assertThrows(InvalidIssuedKeyException.class, () ->
                provider(new RecordingChallengeService(true), registration(CLIENT_ID),
                        new InMemoryAppAttestIssuedKeyService())
                        .authenticate(token("{\"not\":\"a jwk\"}")));
    }

    @Test
    void checksTheSubmittedKeyBeforeSpendingTheChallenge() {
        RecordingChallengeService challengeService = new RecordingChallengeService(true);

        // A request that could never succeed is answered without consuming the one-time
        // challenge, so the client does not have to fetch a fresh one to retry.
        assertThrows(InvalidIssuedKeyException.class, () ->
                provider(challengeService, registration(CLIENT_ID), new InMemoryAppAttestIssuedKeyService())
                        .authenticate(token("{\"not\":\"a jwk\"}")));

        assertFalse(challengeService.consumed.get());
    }

    @Test
    void invalidChallengeIsRejected() throws Exception {
        FakeValidationService validationService = new FakeValidationService(registration(CLIENT_ID));
        JWK submitted = new ECKeyGenerator(Curve.P_256).generate();

        assertThrows(BadCredentialsException.class, () ->
                new AppAttestIssuedKeyRegistrationAuthenticationProvider(
                        new RecordingChallengeService(false), validationService,
                        new InMemoryAppAttestIssuedKeyService())
                        .authenticate(token(submitWithoutPrivateMembers(submitted))));
        assertEquals(0, validationService.assertionCalls.get(),
                "the assertion must not be validated when the challenge is invalid");
    }

    // ---- helpers ----

    private static AppAttestIssuedKeyRegistrationAuthenticationToken authenticate(
            AppAttestIssuedKeyRegistrationAuthenticationProvider provider, JWK submitted) {
        return (AppAttestIssuedKeyRegistrationAuthenticationToken) provider.authenticate(
                token(submitWithoutPrivateMembers(submitted)));
    }

    /** What a client sends: the public half of the key it generated. */
    private static String submitWithoutPrivateMembers(JWK key) {
        return key.toPublicJWK().toJSONString();
    }

    private static AppAttestIssuedKeyRegistrationAuthenticationToken token(String publicKeyJson) {
        return AppAttestIssuedKeyRegistrationAuthenticationToken.unauthenticated(
                ATTEST_KID, CHALLENGE, ASSERTION, publicKeyJson);
    }

    private static AppAttestIssuedKeyRegistrationAuthenticationProvider provider(
            ChallengeService challengeService,
            AppAttestAttestationRegistration registration,
            AppAttestIssuedKeyService issuedKeyService) {
        return new AppAttestIssuedKeyRegistrationAuthenticationProvider(
                challengeService, new FakeValidationService(registration), issuedKeyService);
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
