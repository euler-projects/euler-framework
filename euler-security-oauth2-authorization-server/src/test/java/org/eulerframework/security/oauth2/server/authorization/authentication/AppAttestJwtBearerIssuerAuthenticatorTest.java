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

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.eulerframework.security.authentication.appattest.AppAttestAttestationRegistration;
import org.eulerframework.security.authentication.appattest.AppAttestInstanceKeyRegistration;
import org.eulerframework.security.authentication.appattest.InMemoryAppAttestInstanceKeyRegistrationService;
import org.eulerframework.security.oauth2.core.EulerClientAuthenticationMethod;
import org.eulerframework.security.oauth2.core.EulerClientAttestationProof;
import org.eulerframework.security.provisioning.jit.JitProvisioningPolicy;
import org.eulerframework.security.util.JwkUtils;
import org.junit.jupiter.api.Test;
import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link AppAttestJwtBearerIssuerAuthenticator}: which issuer it claims, and where it
 * looks for a first login's key. The flow itself is covered end to end in
 * {@link OAuth2JwtBearerAuthenticationProviderTest}, whose in-memory identity and user backends are
 * reused here rather than written twice &mdash; nothing this class tests reaches either of them.
 * <p>
 * The refusals carry the weight. {@code iss} is a string in a payload anybody can write, so what
 * makes it believable is that this very request was authenticated as the App instance it names.
 */
class AppAttestJwtBearerIssuerAuthenticatorTest {

    private static final String CLIENT_ID = "client-1";
    private static final String ATTEST_KID = "attest-kid";

    private final InMemoryAppAttestInstanceKeyRegistrationService instanceKeyRegistrationService =
            new InMemoryAppAttestInstanceKeyRegistrationService();

    /**
     * An issuer is claimed for the App instance that authenticated this request, and only for it.
     */
    @Test
    void claimsTheAppInstanceThatAuthenticatedThisRequest() {
        assertTrue(authenticator().supports(CLIENT_ID, attestedClient()));
    }

    /**
     * Naming one's own {@code client_id} as the issuer is not enough. A client that authenticated
     * with a secret proved it holds the secret, not that it is the App instance the attestation
     * registry was written for, so this anchor stays out of it and the assertion is refused for
     * want of any issuer that vouches for it.
     */
    @Test
    void doesNotClaimAnIssuerForAClientThatAuthenticatedSomeOtherWay() {
        assertFalse(authenticator().supports(CLIENT_ID, secretClient()));
    }

    /** An assertion may only be read against the issuer this request was proved to be. */
    @Test
    void doesNotClaimAnIssuerOtherThanTheOneTheVerifiedKeyIsBoundTo() {
        assertFalse(authenticator().supports("some-other-issuer", attestedClient()));
    }

    /**
     * The namespace this issuer's keys live in. It is what keeps a key registered for one kind of
     * issuer invisible to another, and what a second, higher-assurance issuer would have to name
     * differently to avoid sharing an account's key space with this one.
     */
    @Test
    void namesTheIdentityTypeItsKeysAreStoredUnder() {
        assertEquals(AppAttestInstanceKeyRegistration.USER_IDENTITY_TYPE, authenticator().identityType());
    }

    /**
     * Opening an anonymous account is what this issuer is for; whether a given deployment wants it
     * is the provisioning policy's answer, not this one.
     */
    @Test
    void letsASubjectlessAssertionProvision() {
        assertTrue(authenticator().allowsProvisioningWithoutSubject());
    }

    @Test
    void findsARegisteredKeyAndNothingTheInstanceNeverRegistered() throws Exception {
        String jwkKid = registerKeyFor(ATTEST_KID);

        AppAttestJwtBearerIssuerAuthenticator authenticator = authenticator();
        assertNotNull(authenticator.resolveRegisteredKey(assertion(jwkKid, attestedClient())));
        assertNull(authenticator.resolveRegisteredKey(assertion("a-kid-nobody-registered", attestedClient())));
        // No kid in the header is not a wildcard: there is nothing to look up with.
        assertNull(authenticator.resolveRegisteredKey(assertion(null, attestedClient())));
    }

    /**
     * The registry is addressed by App Attest KEY rather than by issuer, so a key another instance
     * registered stays invisible here even though that instance is bound to the same
     * {@code client_id} and would therefore name the same {@code iss}. This is the tighter of the
     * two addressings: the key has to have been registered by the very KEY that authenticated this
     * request.
     */
    @Test
    void findsNothingRegisteredUnderAnotherAppAttestKey() throws Exception {
        String jwkKid = registerKeyFor("some-other-attest-kid");

        assertNull(authenticator().resolveRegisteredKey(assertion(jwkKid, attestedClient())));
    }

    /**
     * A request that carries no verified attestation has no App Attest KEY to look a key up under,
     * so there is nothing to resolve &mdash; whatever the assertion header names.
     */
    @Test
    void findsNothingForAClientThatWasNotAuthenticatedByAppAttest() throws Exception {
        String jwkKid = registerKeyFor(ATTEST_KID);

        assertNull(authenticator().resolveRegisteredKey(assertion(jwkKid, secretClient())));
    }

    // ---- helpers ----

    /**
     * Register a freshly generated public key under the given App Attest KEY and return the
     * {@code kid} a client would quote to have it looked up.
     */
    private String registerKeyFor(String appAttestKid) throws Exception {
        ECKey key = new ECKeyGenerator(Curve.P_256).generate();
        JWK publicKey = JwkUtils.toPublicJwk(key);
        String jwkKid = JwkUtils.computeThumbprint(publicKey);
        this.instanceKeyRegistrationService.saveRegistration(new AppAttestInstanceKeyRegistration(appAttestKid, jwkKid,
                JwkUtils.withKeyId(publicKey, jwkKid).toJSONString()));
        return jwkKid;
    }

    /**
     * An assertion as the grant provider hands one over. Unsigned on purpose: nothing this class
     * tests verifies a signature, and what is under test is which key the anchor resolves.
     */
    private static JwtBearerAssertion assertion(@Nullable String keyId,
                                                OAuth2ClientAuthenticationToken clientPrincipal) throws Exception {
        SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(keyId).build(),
                new JWTClaimsSet.Builder().issuer(CLIENT_ID).build());
        return new JwtBearerAssertion(jwt, jwt.getJWTClaimsSet(), clientPrincipal);
    }

    private AppAttestJwtBearerIssuerAuthenticator authenticator() {
        return new AppAttestJwtBearerIssuerAuthenticator(this.instanceKeyRegistrationService,
                new OAuth2JwtBearerAuthenticationProviderTest.RecordingIdentityService(),
                new OAuth2JwtBearerAuthenticationProviderTest.RecordingUserService(),
                type -> JitProvisioningPolicy.disabled());
    }

    private static RegisteredClient registeredClient() {
        return RegisteredClient.withId("id-1")
                .clientId(CLIENT_ID)
                .clientAuthenticationMethod(EulerClientAuthenticationMethod.ATTEST_APPATTEST_CLIENT_AUTH)
                .authorizationGrantType(AuthorizationGrantType.JWT_BEARER)
                .build();
    }

    private static OAuth2ClientAuthenticationToken attestedClient() {
        return new EulerOAuth2ClientAttestationAuthenticationToken(registeredClient(),
                EulerClientAuthenticationMethod.ATTEST_APPATTEST_CLIENT_AUTH, null,
                new AppAttestAttestationRegistration(ATTEST_KID, "ABCD1234EF", "com.example.app", CLIENT_ID,
                        null, null, null, null, null, null, 0),
                EulerClientAttestationProof.ASSERTION);
    }

    private static OAuth2ClientAuthenticationToken secretClient() {
        return new OAuth2ClientAuthenticationToken(registeredClient(),
                ClientAuthenticationMethod.CLIENT_SECRET_BASIC, null);
    }
}
