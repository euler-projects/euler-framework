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

package org.eulerframework.security.util;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the JWK helpers the jwt-bearer flow and the issued-key registration endpoint
 * share.
 * <p>
 * The cases that matter most are the refusals. A registered key is the only thing standing
 * between a caller and an accepted signature, so what is turned away here &mdash; a
 * symmetric key, a key family no verifier is built for, an algorithm the key cannot have
 * made &mdash; is what keeps a published public key from being reused as an HMAC secret.
 */
class JwkUtilsTest {

    @Test
    void thumbprintIsStableAndIgnoresTheManagementFields() throws Exception {
        ECKey key = new ECKeyGenerator(Curve.P_256).generate();

        String fromPrivate = JwkUtils.computeThumbprint(key);
        String fromPublic = JwkUtils.computeThumbprint(JwkUtils.toPublicJwk(key));

        // RFC 7638 covers the required members only, so the two variants of one key agree.
        assertEquals(fromPrivate, fromPublic);
        // A label someone attached is not part of the key, so it cannot change its identity.
        assertEquals(fromPublic, JwkUtils.computeThumbprint(JwkUtils.withKeyId(key, "some-other-kid")));
    }

    @Test
    void stripsPrivateMembersFromASubmittedKey() throws Exception {
        ECKey key = new ECKeyGenerator(Curve.P_256).keyID("caller-chosen").generate();

        JWK publicKey = JwkUtils.toPublicJwk(key);

        assertFalse(publicKey.isPrivate(), "private key material must never be persisted");
        assertEquals("caller-chosen", publicKey.getKeyID(), "an existing kid is left alone here");
        assertEquals(key.toECPublicKey(), publicKey.toECKey().toECPublicKey());
    }

    @Test
    void rejectsASymmetricKey() {
        // A shared secret is not a public key, and accepting one would let an attacker sign
        // with a value the server had itself published.
        OctetSequenceKey secret = new OctetSequenceKey.Builder(
                "01234567890123456789012345678901".getBytes(StandardCharsets.UTF_8))
                .keyID("secret")
                .build();

        JOSEException ex = assertThrows(JOSEException.class, () -> JwkUtils.toPublicJwk(secret));

        assertTrue(ex.getMessage().contains("oct"));
    }

    @Test
    void rejectsAKeyFamilyNoVerifierIsBuiltFor() throws Exception {
        // Ed25519 is deliberately not supported yet: Apple's Secure Enclave only produces EC
        // P-256, and a Curve25519 key there is an exportable software key. Admitting it is a
        // one-branch change in JwkUtils once a deployment wants it - this test is what makes
        // that change deliberate rather than accidental.
        OctetKeyPair okp = new OctetKeyPair.Builder(Curve.Ed25519,
                new Base64URL("11qYAYKxCrfVS_7TyWQHOg7hcvPapiMlrwIaaPcHURo"))
                .build();

        assertFalse(JwkUtils.isSupportedSignatureKeyType(okp.getKeyType()));
        assertThrows(JOSEException.class, () -> JwkUtils.toPublicJwk(okp));
        assertThrows(JOSEException.class,
                () -> JwkUtils.createJwsVerifier(new JWSHeader(JWSAlgorithm.EdDSA), okp));
    }

    @Test
    void verifiesAnEcSignature() throws Exception {
        ECKey key = new ECKeyGenerator(Curve.P_256).generate();
        JWK publicKey = JwkUtils.toPublicJwk(key);
        SignedJWT jwt = signed(new JWSHeader(JWSAlgorithm.ES256), key);

        assertTrue(jwt.verify(JwkUtils.createJwsVerifier(jwt.getHeader(), publicKey)));
    }

    @Test
    void verifiesAnRsaSignature() throws Exception {
        RSAKey key = new RSAKeyGenerator(2048).generate();
        JWK publicKey = JwkUtils.toPublicJwk(key);
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims());
        jwt.sign(new RSASSASigner(key));

        assertTrue(jwt.verify(JwkUtils.createJwsVerifier(jwt.getHeader(), publicKey)));
    }

    @Test
    void rejectsAnHmacAlgorithmPresentedToAnAsymmetricKey() throws Exception {
        ECKey key = new ECKeyGenerator(Curve.P_256).generate();
        JWK publicKey = JwkUtils.toPublicJwk(key);

        // The classic algorithm-confusion attempt: sign with the published public key used as
        // an HMAC secret and claim HS256. The verifier is chosen from the key's family, never
        // from the header, so the header's algorithm is simply not one it can process.
        SignedJWT confused = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims());
        confused.sign(new MACSigner(publicKey.toECKey().toECPublicKey().getEncoded()));

        assertThrows(JOSEException.class,
                () -> confused.verify(JwkUtils.createJwsVerifier(confused.getHeader(), publicKey)));
    }

    @Test
    void rejectsASignatureMadeByADifferentKey() throws Exception {
        ECKey registered = new ECKeyGenerator(Curve.P_256).generate();
        ECKey impostor = new ECKeyGenerator(Curve.P_256).generate();
        SignedJWT jwt = signed(new JWSHeader(JWSAlgorithm.ES256), impostor);

        assertFalse(jwt.verify(JwkUtils.createJwsVerifier(jwt.getHeader(), JwkUtils.toPublicJwk(registered))));
    }

    @Test
    void withKeyIdReplacesTheLabelAndKeepsTheKey() throws Exception {
        ECKey key = new ECKeyGenerator(Curve.P_256).keyID("supplied").generate();

        JWK relabelled = JwkUtils.withKeyId(key, "derived");

        assertEquals("derived", relabelled.getKeyID());
        assertEquals(key.toECPublicKey(), relabelled.toECKey().toECPublicKey());
        assertEquals(Curve.P_256, relabelled.toECKey().getCurve());
        assertEquals(JwkUtils.computeThumbprint(key), JwkUtils.computeThumbprint(relabelled),
                "re-labelling a key must not change which key it is");
    }

    // ---- helpers ----

    private static SignedJWT signed(JWSHeader header, ECKey key) throws Exception {
        SignedJWT jwt = new SignedJWT(header, claims());
        jwt.sign(new ECDSASigner(key));
        return jwt;
    }

    private static JWTClaimsSet claims() {
        Date now = new Date();
        return new JWTClaimsSet.Builder()
                .issuer("client-1")
                .subject("alice")
                .issueTime(now)
                .expirationTime(new Date(now.getTime() + 60_000))
                .build();
    }
}
