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

import com.nimbusds.jose.jwk.JWK;
import org.eulerframework.security.core.EulerUser;
import org.eulerframework.security.core.EulerUserService;
import org.eulerframework.security.core.identity.UserIdentity;
import org.eulerframework.security.core.identity.UserIdentityService;
import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.eulerframework.security.core.userdetails.RandomUsernameGenerator;
import org.eulerframework.security.provisioning.jit.JitProvisioningPolicy;
import org.eulerframework.security.provisioning.jit.JitProvisioningPolicyResolver;
import org.eulerframework.security.util.UserDetailsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A {@link JwtBearerIssuerAuthenticator} for an issuer whose keys are held on
 * {@link UserIdentity user identities}: the account is found through the identity SPI, and the
 * key an assertion verifies against is the one that identity stores.
 * <p>
 * Which identity type an issuer's keys live under is this class's one piece of required
 * knowledge about it, supplied by {@link #identityType()}. That is what lets several issuers of
 * different assurance share the flow without sharing an account's key space: each names its own
 * type, so a key registered for one is invisible to another, and a deployment that wants a
 * second kind of issuer adds a type and a subclass rather than a branch here.
 *
 * <h2>The two ways an assertion finds its account</h2>
 * <ul>
 *   <li><b>It names its {@code sub}</b> &mdash; an ordinary login. The subject routes to an
 *       account and the signature is verified against the key that account's own identity
 *       holds. The issuer's key registry is never consulted on this path, which is what keeps
 *       an account reachable when the issuer's own credentials are later rotated or revoked: an
 *       app that re-registers and gets a new {@code client_id} keeps the accounts its users
 *       already opened. Nothing here writes, so a caller who cannot produce the account's own
 *       key simply does not get in.</li>
 *   <li><b>It names no {@code sub}</b> &mdash; a first login, which opens an account. Nothing
 *       identifies the caller except the key, so the signature is verified against the key the
 *       issuer registered ({@link #resolveRegisteredKey}) and that key becomes the identity the
 *       new account is bound to. Allowed only where {@link #allowsProvisioningWithoutSubject()}
 *       and the deployment's provisioning policy for this identity type both say so.</li>
 * </ul>
 *
 * <h2>The two steps the provider calls, in its order</h2>
 * {@link #authenticate} reads and {@link #provision} writes, and the provider makes the assertion
 * single-use between them. Nothing here has to know that: the split is what lets an ordinary login,
 * which writes nothing at all, and a first login, which writes an account, share one class without
 * either of them being able to write before a replay has been ruled out. It also leaves
 * {@link #toAuthentication} the only place a result is built, so the account a first login opens is
 * resolved by the same code that resolves every later login of it.
 *
 * <h2>Concurrent first logins</h2>
 * Two subject-less assertions over the same key can arrive at once. Both verify, and both try to
 * bind the same key; the unique {@code (identity_type, subject)} constraint lets one through and
 * fails the other, whose transaction rolls back &mdash; including the account it had just
 * created, when a {@link TransactionOperations} is configured. The loser then re-reads the
 * identity and completes as an ordinary login, so exactly one account exists and both callers
 * land on it. No lock is taken to arrange this.
 */
public abstract class AbstractUserIdentityJwtBearerIssuerAuthenticator implements JwtBearerIssuerAuthenticator {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final UserIdentityService userIdentityService;
    private final EulerUserService userService;
    private final JitProvisioningPolicyResolver jitProvisioningPolicyResolver;

    /**
     * Wraps account-plus-identity creation of a first login, so that losing the race on the
     * unique identity constraint rolls the freshly created account back with it. Defaults to
     * running inline, which still resolves the race correctly but can leave an orphan account
     * behind.
     */
    private TransactionOperations transactionOperations = TransactionOperations.withoutTransaction();

    protected AbstractUserIdentityJwtBearerIssuerAuthenticator(UserIdentityService userIdentityService,
                                                               EulerUserService userService,
                                                               JitProvisioningPolicyResolver jitProvisioningPolicyResolver) {
        Assert.notNull(userIdentityService, "userIdentityService must not be null");
        Assert.notNull(userService, "userService must not be null");
        Assert.notNull(jitProvisioningPolicyResolver, "jitProvisioningPolicyResolver must not be null");
        this.userIdentityService = userIdentityService;
        this.userService = userService;
        this.jitProvisioningPolicyResolver = jitProvisioningPolicyResolver;
    }

    public void setTransactionOperations(TransactionOperations transactionOperations) {
        Assert.notNull(transactionOperations, "transactionOperations must not be null");
        this.transactionOperations = transactionOperations;
    }

    /**
     * The user identity type this issuer's keys are stored under.
     * <p>
     * It is the namespace an assertion from this issuer selects: the account's identities are
     * filtered to this type, its uniqueness is scoped to it, and a key registered under a
     * different issuer's type is not visible here. An issuer and a type therefore have to
     * correspond one to one &mdash; two types claiming one issuer, or one type claimed by two
     * issuers with different keys for the same account, would leave an assertion with more than
     * one key space to be read against and no way to say which was meant.
     *
     * @return the identity type, never empty
     */
    protected abstract String identityType();

    /**
     * The public key this issuer registered under the given {@code kid}.
     * <p>
     * Consulted only when the account has to be found from the key itself, that is when the
     * assertion carries no {@code sub}. A login that names its subject verifies against the key
     * stored on that account's identity instead, so this may answer {@code null} for an issuer
     * that keeps no registry of its own.
     *
     * @param issuer the assertion's {@code iss}, which {@link #supports} has already claimed
     * @param keyId  the assertion header's {@code kid}; never empty on the path that calls this
     * @return the public JWK, or {@code null} if the issuer has no such key
     */
    @Nullable
    protected abstract JWK resolveRegisteredKey(String issuer, @Nullable String keyId);

    /**
     * Whether an assertion from this issuer may open an account when it carries no {@code sub}.
     * <p>
     * {@code false} by default, which is the safe answer for an issuer that has already
     * identified its subject and therefore has no reason to omit it: a missing {@code sub} then
     * becomes the protocol error it is. An issuer that proves only that a genuine client
     * generated a key overrides this, and takes on the consequence that what it opens is an
     * anonymous account.
     * <p>
     * Answering {@code true} is necessary but not sufficient. Opening an account also requires
     * the {@link JitProvisioningPolicy} resolved for {@link #identityType()} to be enabled and
     * to admit the client authentication mechanism this request came in under, so a deployment
     * keeps one switch for the whole behaviour.
     *
     * @return {@code true} if a subject-less assertion from this issuer may open an account
     */
    protected boolean allowsProvisioningWithoutSubject() {
        return false;
    }

    /**
     * Whether an identity of this type may only ever authenticate an account that carries no
     * identity of any other type.
     * <p>
     * {@code true} by default, because the type this grant opens accounts for proves possession
     * of a device rather than who is holding it: an account identified that way is an anonymous
     * trial until something better is bound to it, and from that moment this weak factor has to
     * stop authenticating it. Enforcing it here as well as in the identity backend covers the
     * account that was bound the other way round, and makes the retirement immediate with
     * nothing to delete and no data lost.
     * <p>
     * An issuer whose keys stand for an identified subject overrides this: coexisting with a
     * phone or email identity is the whole point of a second factor.
     *
     * @return {@code true} if the account must carry no other identity type
     */
    protected boolean requiresExclusiveAccount() {
        return true;
    }

    /**
     * Verify the signature and authenticate the account, reading only.
     *
     * @return the authenticated result, or {@code null} when the assertion is genuine and no account
     *         carries its key yet &mdash; the provider's cue to spend it and ask
     *         {@link #provision(JwtBearerAssertion)}
     */
    @Override
    @Nullable
    public final Authentication authenticate(JwtBearerAssertion assertion) {
        String subject = assertion.getSubject();
        UserIdentity userIdentity = subject != null
                ? authenticateNamedSubject(assertion, subject)
                : authenticateSubjectless(assertion);
        return userIdentity == null ? null : toAuthentication(userIdentity);
    }

    /**
     * Open an account for a first login. Called only once the provider has made the assertion
     * single-use, so this is the one place here that writes.
     * <p>
     * The registered key is read again rather than carried over from {@link #authenticate}: an
     * authenticator is a singleton serving concurrent requests and has nowhere to put per-request
     * state. Re-reading is safe because a key ID is its key's own thumbprint, so the same ID always
     * yields the same key material, and because the registry has no removal path. It costs one extra
     * lookup on a first login and nothing on any other request.
     * <p>
     * The provisioning gate is consulted here rather than in {@code authenticate}, and only for
     * <em>creating</em> an account: turning it off stops new anonymous accounts from being opened
     * without stranding the ones already open, and it is not readable by a caller who holds no
     * registered key.
     */
    @Override
    @Nullable
    public final EulerUser provision(JwtBearerAssertion assertion) {
        JWK registeredKey = assertion.getSubject() == null && allowsProvisioningWithoutSubject()
                ? registeredKey(assertion)
                : null;
        if (registeredKey == null) {
            // authenticate() has already refused an assertion that names a subject, comes from an
            // issuer that opens no accounts, or names no key it vouches for; a null answer here just
            // says there is nothing left to open.
            return null;
        }
        return createAccount(registeredKey.toJSONString(), requireProvisioningAllowed(assertion));
    }

    /**
     * Whether an account may be opened for this request, and under which policy.
     * <p>
     * Reads the deployment's provisioning policy for {@link #identityType()}, which is the same
     * answer every other provisioning point in the framework gives for that identity type: an
     * account opened here is opened on the terms the deployment set for the type, not on terms this
     * grant invented.
     * <p>
     * An issuer with a condition of its own &mdash; one where only some of the callers that reach
     * it should be able to open an account &mdash; overrides this, applies the condition, and
     * delegates here for the policy. The condition stays with the issuer because it is a fact about
     * that issuer, not about the identity type: {@code public_key} says nothing about who may ask
     * for one, whereas an issuer vouching only for device authenticity has everything to say about
     * it. Keeping the two apart is what leaves the policy a statement about an identity type that
     * a browser login and a token grant can both read the same way.
     *
     * @param assertion the assertion that verified but named no account
     * @return the policy to provision under, for the authorities to grant
     * @throws OAuth2AuthenticationException {@code invalid_grant} if no account may be opened
     */
    protected JitProvisioningPolicy requireProvisioningAllowed(JwtBearerAssertion assertion) {
        JitProvisioningPolicy policy = this.jitProvisioningPolicyResolver.resolve(identityType());
        if (policy == null || !policy.isEnabled()) {
            throw refuse("JIT provisioning is disabled for identity type '" + identityType() + "'");
        }
        return policy;
    }

    // ========== Path 1: the assertion names its subject ==========

    /**
     * An ordinary login. Read-only: the caller either produces the account's own key or does not
     * get in, and nothing about a failed attempt changes what is stored.
     * <p>
     * A {@code kid} the account does not hold is refused rather than read as a request to bind
     * it. Binding would prove only that the caller knows the account's subject, and a subject is
     * an account's username: the token generator stamps it into every access and id token this
     * server issues, so anything the client ever presents a token to has seen it and it cannot be
     * treated as a secret. Letting knowledge of it stand for control of the account would hand
     * anyone who learns a username a way in. Nor is a second proof available to ask for: the one
     * case binding would serve &mdash; a key the account has never held &mdash; is exactly the
     * case where the account's own key is lost or on another device, so there is nothing the
     * caller could co-sign with.
     */
    private UserIdentity authenticateNamedSubject(JwtBearerAssertion assertion, String subject) {
        try {
            return locateNamedSubject(assertion, subject);
        } catch (OAuth2AuthenticationException ex) {
            // Every way this can fail has to answer the same way, including the signature. The key
            // that would verify the assertion is the one being looked for, so all of these checks
            // run before the signature does and a caller holding no key at all can reach every one
            // of them. Left precise they are an oracle: one request would say whether a subject
            // exists, whether it is bound to something stronger than a key, and whether it holds
            // the key named - and omitting the kid while signing with anything would make even "the
            // signature does not match" mean "this account exists and holds exactly one key".
            throw refuse(ex.getError().getDescription());
        }
    }

    /**
     * Find the account and check the signature, describing precisely what was wrong.
     * <p>
     * None of these descriptions reaches the caller: {@link #authenticateNamedSubject} catches them
     * all and answers with the one refusal that says nothing about an account, logging the reason.
     * They are written for that log, so keep them specific and keep this method private to it.
     */
    private UserIdentity locateNamedSubject(JwtBearerAssertion assertion, String subject) {
        EulerUser user = this.userService.loadUserByUsername(subject);
        if (user == null) {
            throw JwtBearerErrors.invalidGrant("the assertion subject is unknown");
        }

        List<UserIdentity> identities = this.userIdentityService.listUserIdentities(user.getUserId());
        List<UserIdentity> ownTypeIdentities = identities.stream()
                .filter(identity -> identityType().equals(identity.getIdentityType()))
                .toList();
        if (requiresExclusiveAccount() && identities.size() != ownTypeIdentities.size()) {
            throw JwtBearerErrors.invalidGrant("the account is bound to another identity type");
        }

        UserIdentity userIdentity = findByKeyId(ownTypeIdentities, assertion.getKeyId());
        if (userIdentity == null) {
            throw JwtBearerErrors.invalidGrant(assertion.getKeyId() != null
                    ? "the account has no " + identityType() + " identity for this kid"
                    : "the account has no " + identityType() + " identity");
        }

        assertion.verifySignature(identityJwk(userIdentity));
        return userIdentity;
    }

    /**
     * The identity the assertion's {@code kid} names, or {@code null} when the account holds none
     * that matches. An account holding exactly one key of this type does not need the caller to
     * say which, so a missing {@code kid} selects it; an account holding several does, and one
     * naming a key the account does not hold matches nothing.
     */
    @Nullable
    private static UserIdentity findByKeyId(List<UserIdentity> identities, @Nullable String keyId) {
        if (keyId == null) {
            return identities.size() == 1 ? identities.get(0) : null;
        }
        return identities.stream()
                .filter(identity -> keyId.equals(identityJwk(identity).getKeyID()))
                .findFirst()
                .orElse(null);
    }

    private static JWK identityJwk(UserIdentity userIdentity) {
        Object jwk = userIdentity.getProperty(UserIdentityService.PROPERTY_JWK);
        try {
            // The backend hands the key back in its projected JSON object form; a JSON string is
            // accepted as well, so a caller that assembled the identity itself does not have to
            // know which of the two the backend chose.
            if (jwk instanceof String json) {
                return JWK.parse(json);
            }
            if (jwk instanceof Map<?, ?> members) {
                return JWK.parse(jwkMembers(members));
            }
        } catch (ParseException | RuntimeException e) {
            throw new IllegalStateException(
                    "The identity '" + userIdentity.getIdentityId() + "' carries an unparsable jwk", e);
        }
        // The backend persisted this identity without a usable key, which no login could ever
        // verify against; a server-side data fault rather than something to blame the caller for.
        throw new IllegalStateException(
                "The identity '" + userIdentity.getIdentityId() + "' carries no jwk");
    }

    @SuppressWarnings("unchecked")  // a JWK's JSON object form is Map<String, Object> by construction
    private static Map<String, Object> jwkMembers(Map<?, ?> members) {
        return (Map<String, Object>) members;
    }

    // ========== Path 2: the assertion names no subject ==========

    /**
     * A first login: nothing identifies the caller except the key the assertion was signed with, so
     * the signature is checked against the key the issuer registered rather than against anything
     * an account holds.
     * <p>
     * An issuer that opens no accounts is refused here, before any signature is looked at: what it
     * would take to answer otherwise is a caller holding a registered key, and telling such a
     * caller that this issuer does not do first logins is the only thing that check could leak.
     *
     * @return the identity that key is already bound to, or {@code null} when it is bound to none
     *         and an account has to be opened for it
     */
    @Nullable
    private UserIdentity authenticateSubjectless(JwtBearerAssertion assertion) {
        if (!allowsProvisioningWithoutSubject()) {
            // Whether an issuer opens accounts is a deployment fact, and saying so would let a
            // caller tell it apart from an account that could not be opened for another reason.
            throw refuse("this issuer opens no account for an assertion without a sub");
        }
        JWK registeredKey = registeredKey(assertion);
        if (registeredKey == null) {
            throw JwtBearerErrors.invalidGrant(assertion.getKeyId() == null
                    // The issuer's registry is keyed by (issuer, kid), so without a key ID there is
                    // nothing to verify against.
                    ? "the assertion must carry a kid header to open an account"
                    : "the assertion issuer has not registered a key for this kid");
        }
        assertion.verifySignature(registeredKey);

        // The key's own JSON is the raw subject; the identity backend derives the persisted subject
        // from it, so the same key always lands on the same identity however it was serialised here.
        return this.userIdentityService
                .findUserIdentityByRawSubject(identityType(), registeredKey.toJSONString())
                .orElse(null);
    }

    /**
     * The key this issuer registered under the assertion's {@code kid}, or {@code null} when the
     * assertion names no key or the issuer vouches for none by that name.
     */
    @Nullable
    private JWK registeredKey(JwtBearerAssertion assertion) {
        String keyId = assertion.getKeyId();
        return keyId == null ? null : resolveRegisteredKey(assertion.getIssuer(), keyId);
    }

    /**
     * Create an account and bind the key to it, or fall in behind a concurrent request that got
     * there first.
     * <p>
     * The username is generated rather than derived from anything the caller supplied, so it is what
     * the issued tokens report as {@code sub} and what the client has to present on every later
     * login; the password is unusable, since this identity has no password path.
     * <p>
     * The account is returned rather than an authentication of it: the provider calls
     * {@link #authenticate} again on it, so that the account a first login opens is resolved by the
     * same code that resolves every later login of it and cannot come out with a different result.
     */
    private EulerUser createAccount(String rawSubject, JitProvisioningPolicy policy) {
        try {
            return this.transactionOperations.execute(status -> {
                EulerUserDetails newUser = EulerUserDetails.builder()
                        .username(RandomUsernameGenerator.generate())
                        .password("{noop}" + org.eulerframework.common.util.StringUtils.randomString(32))
                        .authorities(policy.defaultAuthoritiesArray())
                        .build();
                EulerUser createdUser = this.userService.createUser(newUser);
                UserIdentity prototype = UserIdentity.builder()
                        .identityType(identityType())
                        .property(UserIdentityService.PROPERTY_JWK, rawSubject)
                        .build();
                this.userIdentityService.createUserIdentity(createdUser.getUserId(), prototype);
                return createdUser;
            });
        } catch (RuntimeException ex) {
            // Almost certainly the unique (identity_type, subject) constraint: another request bound
            // this key while this one was creating its account. Fall in behind the winner by reading
            // its account back, which the provider then authenticates exactly as it would have had
            // this request won. Anything else is surfaced untouched rather than disguised as a
            // protocol error.
            UserIdentity winner = this.userIdentityService
                    .findUserIdentityByRawSubject(identityType(), rawSubject)
                    .orElse(null);
            if (winner == null) {
                throw ex;
            }
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Lost the race to provision this key; falling back to the account that won");
            }
            return this.userService.loadUserById(winner.getUserId());
        }
    }

    // ========== Shared helpers ==========

    /**
     * Refuse an assertion without saying anything about the account behind it, keeping the reason
     * for the log where an operator can act on it.
     *
     * @param reason what was actually wrong; never sent to the caller
     * @see JwtBearerErrors#NO_ACCOUNT_DETAIL
     */
    protected OAuth2AuthenticationException refuse(String reason) {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Refusing a jwt-bearer assertion: {}", reason);
        }
        return JwtBearerErrors.refuse();
    }

    /**
     * Load the account an identity belongs to and describe how it was authenticated. The one place
     * a result is built, which is what makes a first login and every later login of the same account
     * indistinguishable to whatever consumes the token.
     * <p>
     * The factor is stamped alongside the user's own authorities, mirroring the one-time-password
     * provider, so a token consumer can tell that this user proved possession of a private key
     * rather than something else.
     */
    private Authentication toAuthentication(UserIdentity userIdentity) {
        EulerUser user = this.userService.loadUserById(userIdentity.getUserId());
        EulerUserDetails userDetails = UserDetailsUtils.toEulerUserDetails(user);
        if (userDetails == null || CollectionUtils.isEmpty(userDetails.getAuthorities())) {
            throw JwtBearerErrors.invalidGrant("the account the assertion resolved to could not be loaded");
        }

        Set<GrantedAuthority> authorities = new HashSet<>(userDetails.getAuthorities());
        authorities.add(FactorGrantedAuthority.fromFactor(PublicKeyAuthentication.PUBLIC_KEY_FACTOR));

        return new PublicKeyAuthentication(userDetails, userIdentity, authorities);
    }
}
