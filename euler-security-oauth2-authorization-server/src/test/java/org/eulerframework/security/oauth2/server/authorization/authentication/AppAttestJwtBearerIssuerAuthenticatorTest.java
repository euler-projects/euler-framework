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

import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import org.eulerframework.security.authentication.appattest.AppAttestAttestationRegistration;
import org.eulerframework.security.authentication.appattest.AppAttestIssuedKey;
import org.eulerframework.security.authentication.appattest.InMemoryAppAttestIssuedKeyService;
import org.eulerframework.security.core.identity.UserIdentityService;
import org.eulerframework.security.oauth2.core.EulerClientAuthenticationMethod;
import org.eulerframework.security.oauth2.core.EulerClientAttestationProof;
import org.eulerframework.security.provisioning.jit.JitProvisioningPolicy;
import org.eulerframework.security.util.JwkUtils;
import org.junit.jupiter.api.Test;
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

    private final InMemoryAppAttestIssuedKeyService issuedKeyService = new InMemoryAppAttestIssuedKeyService();

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
        OAuth2ClientAuthenticationToken secretClient = new OAuth2ClientAuthenticationToken(registeredClient(),
                ClientAuthenticationMethod.CLIENT_SECRET_BASIC, null);

        assertFalse(authenticator().supports(CLIENT_ID, secretClient));
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
        assertEquals(UserIdentityService.IDENTITY_TYPE_PUBLIC_KEY, authenticator().identityType());
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
    void findsARegisteredKeyAndNothingAnIssuerNeverRegistered() throws Exception {
        ECKey key = new ECKeyGenerator(Curve.P_256).generate();
        JWK publicKey = JwkUtils.toPublicJwk(key);
        String keyId = JwkUtils.computeThumbprint(publicKey);
        this.issuedKeyService.saveKey(new AppAttestIssuedKey(CLIENT_ID, keyId,
                JwkUtils.withKeyId(publicKey, keyId).toJSONString()));

        AppAttestJwtBearerIssuerAuthenticator authenticator = authenticator();
        assertNotNull(authenticator.resolveRegisteredKey(CLIENT_ID, keyId));
        assertNull(authenticator.resolveRegisteredKey(CLIENT_ID, "a-kid-nobody-registered"));
        // The registry is keyed by issuer as well as key ID, so another issuer's copy of the same
        // key is not this issuer's to hand out.
        assertNull(authenticator.resolveRegisteredKey("some-other-issuer", keyId));
        assertNull(authenticator.resolveRegisteredKey(CLIENT_ID, null));
    }

    // ---- helpers ----

    private AppAttestJwtBearerIssuerAuthenticator authenticator() {
        return new AppAttestJwtBearerIssuerAuthenticator(this.issuedKeyService,
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
                new AppAttestAttestationRegistration("attest-kid", "ABCD1234EF", "com.example.app", CLIENT_ID,
                        null, null, null, null, null, null, 0),
                EulerClientAttestationProof.ASSERTION);
    }
}
