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

import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import org.eulerframework.resource.Tag;
import org.eulerframework.security.authentication.InMemoryNonceService;
import org.eulerframework.security.authentication.NonceService;
import org.eulerframework.security.authentication.appattest.AppAttestAttestationRegistration;
import org.eulerframework.security.authentication.appattest.AppAttestInstanceKeyRegistration;
import org.eulerframework.security.authentication.appattest.InMemoryAppAttestInstanceKeyRegistrationService;
import org.eulerframework.security.core.EulerAuthority;
import org.eulerframework.security.core.EulerUser;
import org.eulerframework.security.core.EulerUserService;
import org.eulerframework.security.core.identity.IdentityOccupiedException;
import org.eulerframework.security.core.identity.UserIdentity;
import org.eulerframework.security.core.identity.UserIdentityService;
import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.eulerframework.security.oauth2.core.EulerClientAuthenticationMethod;
import org.eulerframework.security.oauth2.core.EulerClientAttestationProof;
import org.eulerframework.security.provisioning.jit.JitProvisioningPolicy;
import org.eulerframework.security.provisioning.jit.JitProvisioningPolicyResolver;
import org.eulerframework.security.util.JwkUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContext;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.util.MultiValueMap;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the jwt-bearer grant, driven through {@link OAuth2JwtBearerAuthenticationProvider}
 * with the built-in {@link AppAttestJwtBearerIssuerAuthenticator} behind it: both ways an
 * assertion finds its account &mdash; by the {@code sub} it names, and by the key it was signed
 * with when it names none &mdash; the RFC 7523 checks that gate them, and the guarantees the
 * provider holds whatever an issuer authenticator does.
 */
class OAuth2JwtBearerAuthenticationProviderTest {

    private static final String ISSUER = "https://example.com";
    private static final String CLIENT_ID = "client-1";
    /**
     * The account an ordinary login resolves to. Deliberately not of the form
     * {@code usr_<n>}, which {@link RecordingUserService} mints for the accounts it creates,
     * so a test that has to tell an existing account from a freshly opened one can.
     */
    private static final String USER_ID = "usr-existing";
    /**
     * The App Attest KEY every fixture client authenticated with. This is what the key registry
     * is addressed by &mdash; not {@link #CLIENT_ID}, which is only what makes that KEY's
     * {@code client_id} usable as an assertion's {@code iss}.
     */
    private static final String ATTEST_KID = "attest-kid";

    private ECKey signingKey;
    private String keyId;
    private ECKey secondKey;
    private String secondKeyId;
    private InMemoryAppAttestInstanceKeyRegistrationService instanceKeyRegistrationService;
    private RecordingIdentityService identityService;
    private RecordingUserService userService;
    private InMemoryOAuth2AuthorizationService authorizationService;
    private InMemoryNonceService nonceService;

    @BeforeEach
    void setUp() throws Exception {
        AuthorizationServerContextHolder.setContext(new AuthorizationServerContext() {
            @Override
            public String getIssuer() {
                return ISSUER;
            }

            @Override
            public AuthorizationServerSettings getAuthorizationServerSettings() {
                return AuthorizationServerSettings.builder().build();
            }
        });

        this.signingKey = new ECKeyGenerator(Curve.P_256).keyIDFromThumbprint(true).generate();
        JWK publicKey = JwkUtils.toPublicJwk(this.signingKey);
        // A label, and deliberately not the key's thumbprint: a login selects a key by its ID and
        // an account is bound to one by its thumbprint, and the two must not be allowed to pass a
        // test by happening to be the same value.
        this.keyId = "key-1";
        publicKey = JwkUtils.withKeyId(publicKey, this.keyId);

        this.instanceKeyRegistrationService = new InMemoryAppAttestInstanceKeyRegistrationService();
        this.instanceKeyRegistrationService.saveRegistration(
                new AppAttestInstanceKeyRegistration(ATTEST_KID, this.keyId, publicKey.toJSONString()));

        // A second key, registered by the same App instance but bound to no account: a perfectly
        // valid key that a login naming somebody else's account must still be refused for.
        this.secondKey = new ECKeyGenerator(Curve.P_256).generate();
        JWK secondPublicKey = JwkUtils.toPublicJwk(this.secondKey);
        this.secondKeyId = "key-2";
        secondPublicKey = JwkUtils.withKeyId(secondPublicKey, this.secondKeyId);
        this.instanceKeyRegistrationService.saveRegistration(
                new AppAttestInstanceKeyRegistration(ATTEST_KID, this.secondKeyId, secondPublicKey.toJSONString()));

        this.identityService = new RecordingIdentityService();
        this.userService = new RecordingUserService();
        this.authorizationService = new InMemoryOAuth2AuthorizationService();
        // Shared across every provider() in one test, so a replay is still a replay when the
        // provider instance is not.
        this.nonceService = new InMemoryNonceService();
    }

    @AfterEach
    void tearDown() {
        AuthorizationServerContextHolder.resetContext();
    }

    // ---- the two ways an assertion finds its account ----

    @Test
    void loginWithASubjectVerifiesAgainstTheIdentityKeyAndIssuesTokens() throws Exception {
        bindExistingAccount();

        Authentication result = provider().authenticate(token(assertion(this.keyId, "alice", 0)));

        assertIssuedToken(result);
        assertStoredPrincipalIsAPublicKeyAuthentication();
        assertEquals(0, this.userService.created.size(), "a login that names its subject opens no account");
        assertEquals(0, this.identityService.created.size());
    }

    @Test
    void subjectlessAssertionProvisionsAnAccountBoundToTheKey() throws Exception {
        Authentication result = provider().authenticate(token(assertion(this.keyId, null, 0)));

        assertIssuedToken(result);
        assertEquals(1, this.userService.created.size());
        assertEquals(1, this.identityService.created.size());
        assertEquals(AppAttestInstanceKeyRegistration.USER_IDENTITY_TYPE,
                this.identityService.created.get(0).getIdentityType());
        // The key is the identity's whole uniqueness: its thumbprint is the persisted subject,
        // which is not the kid that addressed it - the kid only found the row.
        assertEquals(JwkUtils.computeThumbprint(this.signingKey),
                this.identityService.created.get(0).getSubject());
    }

    @Test
    void subjectlessAssertionForAnAlreadyProvisionedKeyOpensNoSecondAccount() throws Exception {
        provider().authenticate(token(assertion(this.keyId, null, 0)));
        int accountsAfterFirst = this.userService.created.size();

        Authentication result = provider().authenticate(token(assertion(this.keyId, null, 1)));

        assertIssuedToken(result);
        assertEquals(accountsAfterFirst, this.userService.created.size(),
                "the same key resolves to the same account however often it logs in");
        assertEquals(1, this.identityService.created.size());
    }

    // ---- what the exclusivity rule does ----

    @Test
    void rejectsALoginForAnAccountThatAlsoHasAnotherIdentityType() throws Exception {
        bindExistingAccount();
        this.identityService.addOtherTypeIdentity(USER_ID);

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> provider().authenticate(token(assertion(this.keyId, "alice", 0))));

        assertEquals(OAuth2ErrorCodes.INVALID_GRANT, ex.getError().getErrorCode());
    }

    // ---- issuer trust ----

    @Test
    void rejectsAnIssuerNoResolverVouchesFor() throws Exception {
        bindExistingAccount();
        // The assertion names some other issuer, so the App Attest resolver - which only
        // vouches for the client that authenticated this request - does not claim it.
        SignedJWT foreign = sign(header(this.keyId), claims("some-other-issuer", "alice", 0));

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> provider().authenticate(token(foreign.serialize())));

        assertEquals(OAuth2ErrorCodes.INVALID_GRANT, ex.getError().getErrorCode());
    }

    @Test
    void rejectsAnAssertionSignedByAKeyTheIssuerNeverRegistered() throws Exception {
        ECKey unregistered = new ECKeyGenerator(Curve.P_256).keyIDFromThumbprint(true).generate();
        String unregisteredKid = JwkUtils.computeThumbprint(JwkUtils.toPublicJwk(unregistered));

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> provider().authenticate(token(assertion(unregisteredKid, null, 0, unregistered))));

        assertEquals(OAuth2ErrorCodes.INVALID_GRANT, ex.getError().getErrorCode());
        assertEquals(0, this.userService.created.size(),
                "an unregistered key must not open an account");
    }

    @Test
    void rejectsASubjectlessAssertionWhenProvisioningIsDisabled() throws Exception {
        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> provider(type -> JitProvisioningPolicy.disabled())
                        .authenticate(token(assertion(this.keyId, null, 0))));

        assertEquals(OAuth2ErrorCodes.INVALID_GRANT, ex.getError().getErrorCode());
        assertEquals(0, this.userService.created.size());
    }

    /**
     * The provisioning switch gates <em>creating</em> an account, not finding one. Turning it
     * off has to stop new anonymous accounts without stranding the ones already open, whose
     * key verifies exactly as it did before.
     */
    @Test
    void provisioningDisabledDoesNotLockOutAnAccountThatIsAlreadyOpen() throws Exception {
        provider().authenticate(token(assertion(this.keyId, null, 0)));
        int accounts = this.userService.created.size();

        assertIssuedToken(provider(type -> JitProvisioningPolicy.disabled())
                .authenticate(token(assertion(this.keyId, null, 1))));

        assertEquals(accounts, this.userService.created.size(), "no new account is opened");
    }

    // ---- which client may open an account ----

    /**
     * The anchor opens accounts only for a client that authenticated <em>by</em> App Attest, and
     * reaching it is not the same thing. Two ways the token endpoint produces a client that reaches
     * it some other way: a traditional credential presented alongside an attestation, which the
     * success handler republishes as an attestation token keeping the method it really
     * authenticated by; and a public client whose authentication stays {@code none} with the
     * attestation carried as an additional signal. Both carry a verified registration naming the
     * issuer, so both pass {@code supports} and both can look a key up &mdash; but in both the
     * attestation describes the request rather than being how the client proved itself, and an
     * anonymous account is minted on that proof alone.
     */
    @Test
    void rejectsASubjectlessAssertionFromAClientThatAuthenticatedBySomethingElse() throws Exception {
        assertEquals(JwtBearerErrors.NO_ACCOUNT_DETAIL, refusal(provider(),
                token(assertion(this.keyId, null, 0),
                        attestationCarryingClient(ClientAuthenticationMethod.CLIENT_SECRET_BASIC))));
        assertEquals(JwtBearerErrors.NO_ACCOUNT_DETAIL, refusal(provider(),
                token(assertion(this.keyId, null, 1),
                        attestationCarryingClient(ClientAuthenticationMethod.NONE))));

        assertEquals(0, this.userService.created.size());
        assertEquals(0, this.identityService.created.size());
    }

    /**
     * The restriction gates opening an account, not using one. A client that authenticated some
     * other way still logs into an account it holds: that asks nothing of the issuer beyond the
     * account's own key, and refusing it would strand an account over how the client got in the
     * door.
     */
    @Test
    void aClientThatAuthenticatedBySomethingElseCanStillLogIntoAnAccountItHolds() throws Exception {
        bindExistingAccount();

        assertIssuedToken(provider().authenticate(token(assertion(this.keyId, "alice", 0),
                attestationCarryingClient(ClientAuthenticationMethod.CLIENT_SECRET_BASIC))));

        assertEquals(0, this.userService.created.size());
    }

    // ---- one key per account ----

    /**
     * The refusal that keeps an account's subject from becoming its credential. The key here
     * is a perfectly good one &mdash; registered under this very issuer, and the assertion it
     * signed verifies &mdash; and it is still turned away, because what named the account was
     * a username rather than anything the account already trusts. Accepting it would let
     * anyone who learns a subject take the account over, and the subject rides in every
     * access token this server issues, so plenty of parties get to learn it.
     */
    @Test
    void refusesAKeyTheAccountDoesNotHoldEvenThoughTheIssuerVouchesForIt() throws Exception {
        bindExistingAccount();

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> provider().authenticate(token(sign(header(this.secondKeyId),
                        claims(CLIENT_ID, "alice", 0), this.secondKey).serialize())));

        assertEquals(OAuth2ErrorCodes.INVALID_GRANT, ex.getError().getErrorCode());
        assertEquals(0, this.identityService.created.size(), "the caller's key is not bound to the account");
        assertEquals(0, this.userService.created.size());
    }

    /**
     * The {@code kid} selects the key and the {@code sub} selects the account, and neither stands
     * in for the other. An account holding one key has nothing to choose between, but choosing for
     * the caller is what would let "the signature does not match" mean "this account exists and
     * holds exactly one key", so the header is required on both paths.
     */
    @Test
    void refusesAnAssertionThatOmitsTheKid() throws Exception {
        bindExistingAccount();

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> provider().authenticate(token(sign(
                        new JWSHeader.Builder(JWSAlgorithm.ES256).build(),
                        claims(CLIENT_ID, "alice", 0)).serialize())));

        assertEquals(OAuth2ErrorCodes.INVALID_GRANT, ex.getError().getErrorCode());
        assertTrue(ex.getError().getDescription().contains("kid"),
                "a missing header describes the request and nothing else, so it is reported precisely");
    }

    /**
     * The account rule the named-subject path has always applied, and the reason the subject-less
     * one goes through the same gate: an account upgraded with an identity that proves who the
     * person is has to stop being reachable by the weak factor that opened it, and dropping the
     * {@code sub} must not be a way around that.
     */
    @Test
    void rejectsASubjectlessLoginForAnAccountThatHasSinceGainedAnotherIdentityType() throws Exception {
        provider().authenticate(token(assertion(this.keyId, null, 0)));
        String userId = this.identityService.created.get(0).getUserId();
        this.identityService.addOtherTypeIdentity(userId);

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> provider().authenticate(token(assertion(this.keyId, null, 1))));

        assertEquals(OAuth2ErrorCodes.INVALID_GRANT, ex.getError().getErrorCode());
        // Collapsed like every other account-state refusal, and the same answer both paths give.
        assertEquals(JwtBearerErrors.NO_ACCOUNT_DETAIL, ex.getError().getDescription());
        assertEquals(1, this.userService.created.size(), "and no second account is opened for the key");
    }

    // ---- signature and RFC 7523 claim checks ----

    @Test
    void rejectsAnAssertionWhoseSignatureDoesNotMatchTheRegisteredKey() throws Exception {
        bindExistingAccount();
        ECKey impostor = new ECKeyGenerator(Curve.P_256).generate();
        // Same kid, so the identity is found; the signature is what has to fail.
        SignedJWT forged = sign(header(this.keyId), claims(CLIENT_ID, "alice", 0), impostor);

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> provider().authenticate(token(forged.serialize())));

        assertEquals(OAuth2ErrorCodes.INVALID_GRANT, ex.getError().getErrorCode());
    }

    @Test
    void rejectsAnExpiredAssertion() throws Exception {
        bindExistingAccount();

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> provider().authenticate(token(assertion(this.keyId, "alice", 0,
                        Instant.now().minus(2, ChronoUnit.HOURS), Instant.now().minus(1, ChronoUnit.HOURS)))));

        assertEquals(OAuth2ErrorCodes.INVALID_GRANT, ex.getError().getErrorCode());
    }

    @Test
    void rejectsAnAssertionAddressedToAnotherAudience() throws Exception {
        bindExistingAccount();
        SignedJWT misaddressed = sign(header(this.keyId),
                new JWTClaimsSet.Builder(claims(CLIENT_ID, "alice", 0))
                        .audience("https://some-other-server.example.com")
                        .build());

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> provider().authenticate(token(misaddressed.serialize())));

        assertEquals(OAuth2ErrorCodes.INVALID_GRANT, ex.getError().getErrorCode());
    }

    @Test
    void acceptsTheTokenEndpointUrlAsTheAudienceToo() throws Exception {
        bindExistingAccount();
        // RFC 7523 names the token endpoint; RFC 8414 style deployments configure the issuer.
        // Both identify this server, so both are accepted.
        JWTClaimsSet claims = new JWTClaimsSet.Builder(claims(CLIENT_ID, "alice", 0))
                .audience(ISSUER + AuthorizationServerSettings.builder().build().getTokenEndpoint())
                .build();
        SignedJWT assertion = sign(header(this.keyId), claims);

        assertIssuedToken(provider().authenticate(token(assertion.serialize())));
    }

    @Test
    void rejectsAReplayedAssertion() throws Exception {
        bindExistingAccount();
        String once = assertion(this.keyId, "alice", 0);
        provider().authenticate(token(once));

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> provider().authenticate(token(once)));

        assertEquals(OAuth2ErrorCodes.INVALID_GRANT, ex.getError().getErrorCode());
    }

    /**
     * A replayed first-login assertion is stopped by the {@code jti} check <em>before</em> it
     * can reach provisioning, so a replay is never what opens an account.
     */
    @Test
    void rejectsAReplayedSubjectlessAssertionBeforeItCanProvision() throws Exception {
        String once = assertion(this.keyId, null, 0);
        provider().authenticate(token(once));
        int accounts = this.userService.created.size();

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> provider().authenticate(token(once)));

        assertEquals(OAuth2ErrorCodes.INVALID_GRANT, ex.getError().getErrorCode());
        assertEquals(accounts, this.userService.created.size(), "a replay opens nothing");
        assertEquals(accounts, this.identityService.created.size());
    }

    @Test
    void rejectsAnAssertionWithNoJti() throws Exception {
        bindExistingAccount();
        SignedJWT noJti = sign(header(this.keyId),
                new JWTClaimsSet.Builder(claims(CLIENT_ID, "alice", 0)).jwtID(null).build());

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> provider().authenticate(token(noJti.serialize())));

        assertEquals(OAuth2ErrorCodes.INVALID_GRANT, ex.getError().getErrorCode());
    }

    // ---- grant-level checks ----

    @Test
    void rejectsAClientThatDoesNotDeclareTheGrant() throws Exception {
        bindExistingAccount();
        RegisteredClient withoutTheGrant = RegisteredClient.withId("id-1")
                .clientId(CLIENT_ID)
                .clientAuthenticationMethod(EulerClientAuthenticationMethod.ATTEST_APPATTEST_CLIENT_AUTH)
                // Any grant the client is not registered for; client_credentials keeps the
                // fixture free of the redirect URI authorization_code would require.
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .build();

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> provider().authenticate(new OAuth2JwtBearerAuthenticationToken(
                        assertion(this.keyId, "alice", 0), attestedClient(withoutTheGrant), null, Map.of())));

        assertEquals(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT, ex.getError().getErrorCode());
    }

    @Test
    void rejectsAnUnknownSubject() throws Exception {
        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> provider().authenticate(token(assertion(this.keyId, "nobody", 0))));

        assertEquals(OAuth2ErrorCodes.INVALID_GRANT, ex.getError().getErrorCode());
    }

    // ---- the order the provider calls an anchor in ----

    /**
     * The one ordering rule this grant has, and the reason an anchor answers in two steps. The
     * assertion has to be single-use by the time anything is written, or a replay writes it twice;
     * and it must not be single-use before the signature has held, or a caller holding no key can
     * spend values out of the nonce store and deny an assertion it intercepted.
     */
    @Test
    void spendsTheAssertionAfterVerifyingAndBeforeProvisioning() throws Exception {
        StubIssuerAuthenticator anchor = StubIssuerAuthenticator.openingAnAccount(this.nonceService);

        assertIssuedToken(providerWith(anchor).authenticate(token(assertion(this.keyId, "alice", 0))));

        assertTrue(anchor.askedToProvision);
        assertTrue(anchor.assertionWasSpentBeforeProvisioning,
                "the provider makes the assertion single-use before an anchor may write");
    }

    /**
     * Opening an account is a command, not a result: the provider asks the anchor to authenticate
     * the assertion again afterwards, so a first login is resolved by the very call that resolves
     * every later login of the same account and cannot come out with a different principal.
     */
    @Test
    void resolvesAProvisionedAccountThroughTheSameCallAsAnyOtherLogin() throws Exception {
        StubIssuerAuthenticator anchor = StubIssuerAuthenticator.openingAnAccount(this.nonceService);

        assertIssuedToken(providerWith(anchor).authenticate(token(assertion(this.keyId, "alice", 0))));

        assertEquals(2, anchor.authentications, "once to find no account, once to resolve the one it opened");
    }

    /** An ordinary login writes nothing, so an anchor that named an account is not asked to open one. */
    @Test
    void doesNotAskAnAnchorToOpenAnAccountItAlreadyResolved() throws Exception {
        StubIssuerAuthenticator anchor = StubIssuerAuthenticator.resolvingAnAccount(this.nonceService);

        assertIssuedToken(providerWith(anchor).authenticate(token(assertion(this.keyId, "alice", 0))));

        assertFalse(anchor.askedToProvision);
        assertEquals(1, anchor.authentications);
    }

    /** An issuer that opens no accounts turns a genuine but unmatched assertion into a refusal. */
    @Test
    void refusesAnAssertionNoAccountCarriesAndWhoseIssuerOpensNone() throws Exception {
        StubIssuerAuthenticator anchor = StubIssuerAuthenticator.openingNoAccounts(this.nonceService);

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> providerWith(anchor).authenticate(token(assertion(this.keyId, "alice", 0))));

        assertEquals(OAuth2ErrorCodes.INVALID_GRANT, ex.getError().getErrorCode());
        assertTrue(anchor.askedToProvision, "the provider asked, and the anchor said it opens none");
    }

    /**
     * A verification that fails has spent nothing, so a caller whose key is wrong has not also
     * burned its assertion: the same {@code jti} still authenticates once signed by the right key.
     */
    @Test
    void aFailedVerificationDoesNotSpendTheAssertion() throws Exception {
        bindExistingAccount();
        ECKey impostor = new ECKeyGenerator(Curve.P_256).generate();
        JWTClaimsSet claims = claims(CLIENT_ID, "alice", 0);

        assertThrows(OAuth2AuthenticationException.class, () -> provider().authenticate(
                token(sign(header(this.keyId), claims, impostor).serialize())));

        assertIssuedToken(provider().authenticate(token(sign(header(this.keyId), claims).serialize())));
    }

    /** Two anchors could claim one issuer; the first is the one that gets the assertion. */
    @Test
    void asksTheFirstIssuerAuthenticatorThatClaimsTheIssuer() throws Exception {
        StubIssuerAuthenticator first = StubIssuerAuthenticator.resolvingAnAccount(this.nonceService);
        StubIssuerAuthenticator second = StubIssuerAuthenticator.resolvingAnAccount(this.nonceService);

        assertIssuedToken(providerWith(first, second).authenticate(token(assertion(this.keyId, "alice", 0))));

        assertEquals(1, first.authentications);
        assertEquals(0, second.authentications, "the rest are not asked once one has claimed the issuer");
    }

    /** The request details the endpoint filter set are carried onto the stored principal. */
    @Test
    void carriesTheRequestDetailsOntoTheStoredPrincipal() throws Exception {
        bindExistingAccount();
        OAuth2JwtBearerAuthenticationToken request = token(assertion(this.keyId, "alice", 0));
        request.setDetails("details-from-the-endpoint-filter");

        provider().authenticate(request);

        OAuth2Authorization authorization =
                this.authorizationService.findByToken("access-token", OAuth2TokenType.ACCESS_TOKEN);
        assertNotNull(authorization);
        Authentication stored = authorization.getAttribute(Principal.class.getName());
        assertEquals("details-from-the-endpoint-filter", ((AbstractAuthenticationToken) stored).getDetails());
    }

    // ---- what a refusal is allowed to say ----

    /**
     * No account enumeration. Every check on the way to an account runs before the signature does,
     * because the key that would verify the assertion is the one being looked for, so a caller
     * holding no key at all can reach all of them. Left precise, one request would say whether a
     * subject exists, whether it is bound to something stronger than a key, and whether it holds the
     * key named; and omitting the {@code kid} while signing with anything would make even "the
     * signature does not match" mean "this account exists and holds exactly one key". All four
     * answer alike, and the reason goes to the server log instead.
     */
    @Test
    void saysNothingAboutAnAccountWhoseKeyTheCallerDoesNotHold() throws Exception {
        bindExistingAccount();
        ECKey impostor = new ECKeyGenerator(Curve.P_256).generate();

        // A subject that does not exist.
        assertEquals(JwtBearerErrors.NO_ACCOUNT_DETAIL, refusal(token(assertion(this.keyId, "nobody", 0))));
        // A subject that does, holding no key by the name given.
        assertEquals(JwtBearerErrors.NO_ACCOUNT_DETAIL, refusal(token(
                sign(header(this.secondKeyId), claims(CLIENT_ID, "alice", 1), this.secondKey).serialize())));
        // A subject that does, holding the key named, but not signed by it.
        assertEquals(JwtBearerErrors.NO_ACCOUNT_DETAIL, refusal(token(
                sign(header(this.keyId), claims(CLIENT_ID, "alice", 2), impostor).serialize())));
        // A subject that has since been bound to something stronger than a key. One-way, so last.
        this.identityService.addOtherTypeIdentity(USER_ID);
        assertEquals(JwtBearerErrors.NO_ACCOUNT_DETAIL, refusal(token(assertion(this.keyId, "alice", 3))));
    }

    /**
     * Nor whether an account could have been opened. "No such account", "this deployment opens
     * none" and "this client may not open one" are one answer, so a refused first login does not
     * even confirm that the key is unbound.
     */
    @Test
    void saysNothingAboutWhetherAnAccountCouldHaveBeenOpened() throws Exception {
        assertEquals(JwtBearerErrors.NO_ACCOUNT_DETAIL,
                refusal(provider(type -> JitProvisioningPolicy.disabled()),
                        token(assertion(this.keyId, null, 0))));
        assertEquals(JwtBearerErrors.NO_ACCOUNT_DETAIL,
                refusal(provider(), token(assertion(this.keyId, null, 1),
                        attestationCarryingClient(ClientAuthenticationMethod.CLIENT_SECRET_BASIC))));
    }

    /** The description a refused request came back with, having first been checked as the right error. */
    private String refusal(OAuth2JwtBearerAuthenticationToken request) {
        return refusal(provider(), request);
    }

    private String refusal(OAuth2JwtBearerAuthenticationProvider provider,
                           OAuth2JwtBearerAuthenticationToken request) {
        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> provider.authenticate(request));
        assertEquals(OAuth2ErrorCodes.INVALID_GRANT, ex.getError().getErrorCode());
        return ex.getError().getDescription();
    }

    // ---- helpers ----

    /** The account an ordinary login resolves to, already bound to the registered key. */
    private void bindExistingAccount() {
        this.identityService.createUserIdentity(USER_ID, UserIdentity.builder()
                .identityType(AppAttestInstanceKeyRegistration.USER_IDENTITY_TYPE)
                .property(UserIdentityService.PROPERTY_JWK, publicJwkJson())
                .build());
        // This is arrangement, not the act under test: the recordings it produced would
        // otherwise be counted as the provider's doing.
        this.identityService.created.clear();
        this.userService.created.clear();
    }

    private String publicJwkJson() {
        try {
            return JwkUtils.withKeyId(JwkUtils.toPublicJwk(this.signingKey), this.keyId).toJSONString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private OAuth2JwtBearerAuthenticationProvider provider() {
        return provider(type -> JitProvisioningPolicy.enabled(List.of(EulerAuthority.USER)));
    }

    private OAuth2JwtBearerAuthenticationProvider provider(JitProvisioningPolicyResolver policyResolver) {
        return providerWith(new AppAttestJwtBearerIssuerAuthenticator(
                this.instanceKeyRegistrationService,
                this.identityService,
                this.userService,
                policyResolver));
    }

    /**
     * The grant provider as an application gets it: the shell, plus whichever anchors vouch for
     * an issuer. Everything the tests vary about the flow lives in the anchors, which is the point
     * of the split.
     */
    private OAuth2JwtBearerAuthenticationProvider providerWith(JwtBearerIssuerAuthenticator... issuerAuthenticators) {
        return new OAuth2JwtBearerAuthenticationProvider(
                List.of(issuerAuthenticators),
                this.nonceService,
                this.authorizationService,
                new FakeTokenGenerator());
    }

    private static OAuth2JwtBearerAuthenticationToken token(String assertion) {
        return token(assertion, attestedClient(registeredClient()));
    }

    private static OAuth2JwtBearerAuthenticationToken token(String assertion, Authentication clientPrincipal) {
        return new OAuth2JwtBearerAuthenticationToken(assertion, clientPrincipal, null, Map.of());
    }

    /**
     * A client whose authentication carries a verified App Attest registration but whose method is
     * something else, which is what the token endpoint produces both for a traditional credential
     * presented alongside an attestation and for a public client whose authentication stays
     * {@code none}. It reaches the anchor, because what binds the issuer is the verified
     * registration rather than the method.
     */
    private static Authentication attestationCarryingClient(ClientAuthenticationMethod method) {
        RegisteredClient registeredClient = RegisteredClient.withId("id-1")
                .clientId(CLIENT_ID)
                .clientAuthenticationMethod(method)
                .authorizationGrantType(AuthorizationGrantType.JWT_BEARER)
                .build();
        return new EulerOAuth2ClientAttestationAuthenticationToken(registeredClient, method,
                ClientAuthenticationMethod.NONE.equals(method) ? null : "credential",
                new AppAttestAttestationRegistration(ATTEST_KID, "ABCD1234EF", "com.example.app", CLIENT_ID,
                        null, null, null, null, null, null, 0),
                EulerClientAttestationProof.ASSERTION);
    }

    private static RegisteredClient registeredClient() {
        return RegisteredClient.withId("id-1")
                .clientId(CLIENT_ID)
                .clientAuthenticationMethod(EulerClientAuthenticationMethod.ATTEST_APPATTEST_CLIENT_AUTH)
                .authorizationGrantType(AuthorizationGrantType.JWT_BEARER)
                .build();
    }

    /**
     * A client that authenticated with a verified App Attest credential, which is what makes
     * its {@code client_id} usable as an assertion issuer.
     */
    private static Authentication attestedClient(RegisteredClient registeredClient) {
        return new EulerOAuth2ClientAttestationAuthenticationToken(registeredClient,
                EulerClientAuthenticationMethod.ATTEST_APPATTEST_CLIENT_AUTH, null,
                new AppAttestAttestationRegistration(ATTEST_KID, "ABCD1234EF", "com.example.app", CLIENT_ID,
                        null, null, null, null, null, null, 0),
                EulerClientAttestationProof.ASSERTION);
    }

    private String assertion(String kid, String subject, int jtiSuffix) throws Exception {
        return assertion(kid, subject, jtiSuffix, this.signingKey);
    }

    private String assertion(String kid, String subject, int jtiSuffix, ECKey key) throws Exception {
        return sign(header(kid), claims(CLIENT_ID, subject, jtiSuffix), key).serialize();
    }

    private String assertion(String kid, String subject, int jtiSuffix, Instant issuedAt, Instant expiresAt)
            throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder(claims(CLIENT_ID, subject, jtiSuffix))
                .issueTime(Date.from(issuedAt))
                .expirationTime(Date.from(expiresAt))
                .build();
        return sign(header(kid), claims).serialize();
    }

    private static JWSHeader header(String kid) {
        return new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(kid).build();
    }

    private static JWTClaimsSet claims(String issuer, String subject, int jtiSuffix) {
        Instant now = Instant.now();
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .audience(ISSUER)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(5, ChronoUnit.MINUTES)))
                .jwtID("jti-" + jtiSuffix);
        if (subject != null) {
            builder.subject(subject);
        }
        return builder.build();
    }

    private SignedJWT sign(JWSHeader header, JWTClaimsSet claims) throws Exception {
        return sign(header, claims, this.signingKey);
    }

    private static SignedJWT sign(JWSHeader header, JWTClaimsSet claims, ECKey key) throws Exception {
        SignedJWT jwt = new SignedJWT(header, claims);
        jwt.sign(new ECDSASigner(key));
        return jwt;
    }

    /**
     * Assert a token was issued. {@code OAuth2AccessTokenAuthenticationToken} is deliberately
     * not marked authenticated: it feeds the token endpoint response rather than the
     * {@code SecurityContext}, and its principal is the client, not the user.
     */
    private static void assertIssuedToken(Authentication result) {
        assertInstanceOf(OAuth2AccessTokenAuthenticationToken.class, result);
        assertNotNull(((OAuth2AccessTokenAuthenticationToken) result).getAccessToken());
    }

    /**
     * The user principal stored on the authorization is what an authorization store has to
     * serialize, so its type is part of the contract and not an implementation detail.
     */
    private void assertStoredPrincipalIsAPublicKeyAuthentication() {
        OAuth2Authorization authorization =
                this.authorizationService.findByToken("access-token", OAuth2TokenType.ACCESS_TOKEN);
        assertNotNull(authorization);
        assertInstanceOf(PublicKeyAuthentication.class, authorization.getAttribute(Principal.class.getName()));
    }

    // ---- fakes ----

    /**
     * An issuer authenticator that does the minimum the split interface asks of it and records the
     * order the provider called it in, for the cases where what is under test is the provider's own
     * orchestration rather than any issuer's. Its principal is not a {@code UserDetails}, so the
     * account status check has nothing to check and cannot interfere.
     * <p>
     * Opening an account makes the next {@code authenticate} find one, which is what the real thing
     * does and what lets the provider resolve a first login through the same call as any other.
     */
    static class StubIssuerAuthenticator implements JwtBearerIssuerAuthenticator {

        private final boolean opensAccounts;
        private final NonceService nonceService;

        private boolean accountExists;

        int authentications;
        boolean askedToProvision;
        boolean assertionWasSpentBeforeProvisioning;

        /** An anchor whose {@code authenticate} names an account, so none has to be opened. */
        static StubIssuerAuthenticator resolvingAnAccount(NonceService nonceService) {
            StubIssuerAuthenticator stub = new StubIssuerAuthenticator(false, nonceService);
            stub.accountExists = true;
            return stub;
        }

        /** An anchor whose {@code authenticate} finds nothing and whose {@code provision} opens one. */
        static StubIssuerAuthenticator openingAnAccount(NonceService nonceService) {
            return new StubIssuerAuthenticator(true, nonceService);
        }

        /** An anchor that finds no account and opens none: an issuer that requires a {@code sub}. */
        static StubIssuerAuthenticator openingNoAccounts(NonceService nonceService) {
            return new StubIssuerAuthenticator(false, nonceService);
        }

        private StubIssuerAuthenticator(boolean opensAccounts, NonceService nonceService) {
            this.opensAccounts = opensAccounts;
            this.nonceService = nonceService;
        }

        @Override
        public boolean supports(String issuer, OAuth2ClientAuthenticationToken clientPrincipal) {
            return CLIENT_ID.equals(issuer);
        }

        @Override
        public Authentication authenticate(JwtBearerAssertion assertion) {
            this.authentications++;
            return this.accountExists ? result() : null;
        }

        @Override
        public EulerUser provision(JwtBearerAssertion assertion) {
            this.askedToProvision = true;
            // recordIfAbsent answering false is the proof: the value was already recorded, and the
            // only thing that records it is the provider, between the two calls. Asking the store
            // rather than reading a flag the provider sets is what makes this a test of the ordering
            // and not of the stub.
            this.assertionWasSpentBeforeProvisioning = !this.nonceService
                    .recordIfAbsent(assertion.getClaims().getJWTID(), Duration.ofMinutes(1));
            if (!this.opensAccounts) {
                return null;
            }
            this.accountExists = true;
            return user("usr_provisioned", "alice");
        }

        private static Authentication result() {
            return UsernamePasswordAuthenticationToken.authenticated("alice", null,
                    List.of(new SimpleGrantedAuthority(EulerAuthority.USER)));
        }
    }

    /**
     * In-memory identity backend honouring the three invariants the provider relies on: the
     * subject is derived from the key, {@code (identity_type, subject)} is unique, and an
     * account holds at most one key.
     */
    static class RecordingIdentityService implements UserIdentityService {

        final List<UserIdentity> created = new ArrayList<>();
        private final Map<String, UserIdentity> bySubject = new LinkedHashMap<>();
        private final List<UserIdentity> all = new ArrayList<>();

        void addOtherTypeIdentity(String userId) {
            this.all.add(UserIdentity.builder()
                    .identityId("idn_phone")
                    .identityType("phone")
                    .subject("some-phone-hash")
                    .userId(userId)
                    .boundAt(Instant.now())
                    .build());
        }

        @Override
        public String identityType() {
            return AppAttestInstanceKeyRegistration.USER_IDENTITY_TYPE;
        }

        @Override
        public UserIdentity createUserIdentity(String userId, MultiValueMap<String, String> params) {
            throw new UnsupportedOperationException();
        }

        @Override
        public UserIdentity createUserIdentity(String userId, UserIdentity prototype) {
            String jwkJson = prototype.getProperty(UserIdentityService.PROPERTY_JWK);
            String subject = JwkUtils.computeThumbprint(parse(jwkJson));
            boolean alreadyHasKey = this.all.stream().anyMatch(identity ->
                    identity.getUserId().equals(userId) && identityType().equals(identity.getIdentityType()));
            if (alreadyHasKey) {
                throw new IdentityOccupiedException(identityType(),
                        "The account already carries a public key and cannot be given a second one");
            }
            if (this.bySubject.containsKey(subject)) {
                throw new IdentityOccupiedException(identityType());
            }
            // No jwk is projected: a persisted identity carries the thumbprint as its subject and
            // the key material stays with the issuer, which is where a login reads it from.
            UserIdentity identity = UserIdentity.withExtensions(new LinkedHashMap<>())
                    .identityId("idn_" + UUID.randomUUID())
                    .identityType(identityType())
                    .subject(subject)
                    .userId(userId)
                    .boundAt(Instant.now())
                    .build();
            this.bySubject.put(subject, identity);
            this.all.add(identity);
            this.created.add(identity);
            return identity;
        }

        @Override
        public Optional<UserIdentity> getUserIdentity(String userId, String identityId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<UserIdentity> listUserIdentities(String userId) {
            return this.all.stream().filter(identity -> identity.getUserId().equals(userId)).toList();
        }

        @Override
        public List<UserIdentity> listUserIdentities(String userId, String identityType) {
            return listUserIdentities(userId).stream()
                    .filter(identity -> identity.getIdentityType().equals(identityType))
                    .toList();
        }

        @Override
        public UserIdentity updateUserIdentity(String userId, String identityId, MultiValueMap<String, String> params) {
            throw new UnsupportedOperationException();
        }

        @Override
        public UserIdentity updateUserIdentity(String userId, String identityId, UserIdentity prototype) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteUserIdentity(String userId, String identityId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<UserIdentity> findUserIdentityByRawSubject(String identityType, String rawSubject) {
            if (!identityType().equals(identityType)) {
                return Optional.empty();
            }
            return Optional.ofNullable(this.bySubject.get(JwkUtils.computeThumbprint(parse(rawSubject))));
        }

        @Override
        public Optional<String> getRawFieldValue(String userId, String identityId, String fieldName) {
            return Optional.empty();
        }

        private static JWK parse(String jwkJson) {
            try {
                return JWK.parse(jwkJson);
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    static class RecordingUserService implements EulerUserService {

        final List<EulerUserDetails> created = new ArrayList<>();
        private int nextId = 1;

        @Override
        public EulerUser createUser(EulerUserDetails userDetails) {
            this.created.add(userDetails);
            return user("usr_" + this.nextId++, userDetails.getUsername());
        }

        @Override
        public EulerUser createUser(EulerUser eulerUser) {
            throw new UnsupportedOperationException();
        }

        @Override
        public EulerUser loadUserById(String userId) {
            return user(userId, "alice");
        }

        @Override
        public EulerUser loadUserByUsername(String username) {
            // Only the account the fixtures bound a key to exists; anything else is unknown,
            // which is what a login naming a foreign subject has to run into.
            return USER_ID.equals(userIdFor(username)) ? user(USER_ID, username) : null;
        }

        private static String userIdFor(String username) {
            return "alice".equals(username) ? USER_ID : null;
        }

        @Override
        public List<EulerUser> listUsers(int offset, int limit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void updateUser(EulerUser eulerUser) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void updatePassword(String userId, String newPassword) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteUser(String userId) {
            throw new UnsupportedOperationException();
        }
    }

    private static EulerUser user(String userId, String username) {
        return new EulerUser() {
            @Override
            public String getUserId() {
                return userId;
            }

            @Override
            public String getUsername() {
                return username;
            }

            @Override
            public Collection<? extends EulerAuthority> getAuthorities() {
                return List.of(authority(EulerAuthority.USER));
            }

            @Override
            public Collection<Tag> getTags() {
                return List.of();
            }

            @Override
            public String getPassword() {
                return "{noop}unused";
            }

            @Override
            public void eraseCredentials() {
            }

            @Override
            public void reloadUserDetails(EulerUserDetails userDetails) {
            }
        };
    }

    private static EulerAuthority authority(String name) {
        return new EulerAuthority() {
            @Override
            public String getAuthority() {
                return name;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getDescription() {
                return name;
            }
        };
    }

    static class FakeTokenGenerator implements OAuth2TokenGenerator<OAuth2Token> {

        @Override
        public OAuth2Token generate(OAuth2TokenContext context) {
            if (!OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                return null;
            }
            return new FixedOAuth2Token("access-token");
        }
    }

    static class FixedOAuth2Token implements OAuth2Token {

        private final String value;
        private final Instant issuedAt = Instant.now();

        FixedOAuth2Token(String value) {
            this.value = value;
        }

        @Override
        public String getTokenValue() {
            return this.value;
        }

        @Override
        public Instant getIssuedAt() {
            return this.issuedAt;
        }

        @Override
        public Instant getExpiresAt() {
            return this.issuedAt.plusSeconds(3600);
        }
    }
}
