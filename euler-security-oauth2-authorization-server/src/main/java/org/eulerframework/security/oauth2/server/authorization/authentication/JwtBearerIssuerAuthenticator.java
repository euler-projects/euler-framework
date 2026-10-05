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

import org.eulerframework.security.core.EulerUser;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;

/**
 * Authenticates a jwt-bearer assertion on behalf of the family of issuers it vouches for.
 * <p>
 * RFC 7523 requires an authorization server to reject an assertion whose signature does not
 * verify, and to know an {@code iss} well enough to find the key that should verify it &mdash; but
 * Section 5 puts the identifiers, the keys and the way they are exchanged explicitly out of scope,
 * to be agreed out of band. This interface is where that agreement is contributed: the grant
 * provider holds a list of authenticators and hands the assertion to the first one that claims its
 * issuer, so <b>supporting a new kind of issuer means contributing an authenticator</b>. The
 * provider, the identity model and the persisted accounts are untouched.
 * <p>
 * What an implementation does is everything that depends on who the issuer is: where the
 * verification key comes from (a registry this server maintains, the issuer's own published JWKS,
 * the key stored on an account's identity), which account the assertion authenticates, whether an
 * assertion from this issuer may open one, and what the successful result says about how the caller
 * proved themselves. What it does not do is anything RFC 6749 or RFC 7523 says the same for every
 * issuer &mdash; the grant and scope checks, the assertion envelope, replay protection and the
 * issuing of tokens all stay with {@link OAuth2JwtBearerAuthenticationProvider}.
 *
 * <h2>Two steps, in the provider's order</h2>
 * The interface is split so that the one ordering rule this grant has cannot be gotten wrong by an
 * implementation. The provider calls {@link #authenticate}, then makes the assertion single-use,
 * then calls {@link #provision} and only if no account was found:
 * <pre>
 *   authenticate(assertion)   reads only; finds the key, checks the signature, names the account
 *   &mdash; the provider spends the assertion's {@code jti} here &mdash;
 *   provision(assertion)      writes; called only when authenticate() found no account
 *   authenticate(assertion)   reads again, and is what produces the result
 * </pre>
 * Spending it earlier would let anyone who can reach the endpoint fill the nonce store with values
 * it chose, and deny a real assertion whose {@code jti} it had guessed or intercepted. Spending it
 * later would let a replay run ahead of the very check meant to stop it, and open an account twice.
 * Neither is reachable from an implementation, which is why there is no flag to set and no
 * bookkeeping to remember here.
 * <p>
 * {@code provision} returns the account rather than an authentication of it, so that
 * {@code authenticate} is the <em>only</em> thing that produces a result. A first login is therefore
 * resolved by the same code that resolves every later one, and cannot come out with a different
 * principal, different authorities or a different factor stamped on it. What that costs is one more
 * read-only pass over an assertion that has already been accepted &mdash; on a first login only.
 *
 * <h2>Assurance is the issuer's property, not the grant's</h2>
 * The same grant serves an anonymous trial account opened by a device-attested app and, should one
 * be contributed, a fully identified subject vouched for by an external identity provider. The
 * difference between those two is entirely inside their authenticators, which is why a low-assurance
 * issuer cannot be stopped from minting accounts by anything the provider does, and why a
 * high-assurance one needs no special case in it either.
 *
 * @see OAuth2JwtBearerAuthenticationProvider
 * @see JwtBearerAssertion
 * @see AbstractUserIdentityJwtBearerIssuerAuthenticator
 */
public interface JwtBearerIssuerAuthenticator {

    /**
     * Whether this authenticator is the one that vouches for the given issuer, presented by the
     * given client.
     * <p>
     * Implementations decide what binds an issuer to the request. An issuer that is a client of
     * this authorization server checks that the two agree; an external issuer checks it against its
     * own list of trusted issuers and ignores the client. Deciding on the {@code iss} string alone
     * would let any caller claim any issuer, so an implementation that has nothing better to go on
     * should not claim anything at all.
     * <p>
     * The first authenticator in the list that answers {@code true} gets the assertion; the rest are
     * not asked. Order therefore matters when two could claim the same issuer.
     *
     * @param issuer          the assertion's {@code iss}; never {@code null} or empty
     * @param clientPrincipal the client authentication this grant request was made under;
     *                        never {@code null}
     * @return {@code true} if this authenticator vouches for the issuer
     */
    boolean supports(String issuer, OAuth2ClientAuthenticationToken clientPrincipal);

    /**
     * Check the assertion's signature and authenticate the account it names, reading only.
     * <p>
     * Verifying and resolving are one step because for most issuers they are one lookup: the key
     * that makes the signature hold is the key that identifies the account. An implementation must
     * not write anything here &mdash; the provider has not yet made the assertion single-use, so
     * anything written now could be written twice by a replay.
     * <p>
     * This is the only method that produces a result, and it is called again after
     * {@link #provision} so that a first login is resolved by the same code as every later one.
     *
     * @param assertion the validated assertion; never {@code null}
     * @return the <em>authenticated</em> result whose principal is the account's user, or
     *         {@code null} if the assertion is genuine but no account carries it yet &mdash; which
     *         is the provider's cue to spend it and ask {@link #provision}. Never an
     *         unauthenticated result.
     * @throws OAuth2AuthenticationException {@code invalid_grant} if the assertion does not
     *                                       authenticate: a key the issuer does not vouch for, a
     *                                       signature that does not hold, an account this issuer
     *                                       may not speak for. {@code error_uri} should be
     *                                       {@code https://datatracker.ietf.org/doc/html/rfc7523#section-3.1}.
     *                                       Descriptions must not reveal whose account was named,
     *                                       or the endpoint becomes a way to probe which subjects
     *                                       exist.
     */
    @Nullable
    Authentication authenticate(JwtBearerAssertion assertion) throws OAuth2AuthenticationException;

    /**
     * Open an account for an assertion that verified but named no account.
     * <p>
     * Called only after the provider has spent the assertion, so this is where an implementation
     * writes. It is a command and not an authentication: it makes the account exist and hands it
     * back, and the provider then calls {@link #authenticate} again to resolve it. An issuer that
     * opens no accounts &mdash; because it always names its subject, or because what it vouches for
     * is not strong enough to mint an account on &mdash; returns {@code null} and the provider
     * refuses the request.
     * <p>
     * The provider passes the same assertion it verified with and nothing derived from it, so an
     * implementation re-reads whatever it needs. That is deliberate: an authenticator is a singleton
     * serving concurrent requests and has nowhere to put per-request state, and a value re-read here
     * is one that cannot have been smuggled in from somewhere the provider did not check.
     *
     * @param assertion the assertion {@link #authenticate} accepted; never {@code null}
     * @return the account opened, or {@code null} if this issuer opens none
     * @throws OAuth2AuthenticationException {@code invalid_grant} if this issuer could open an
     *                                       account but this deployment or this caller may not
     *                                       have one
     */
    @Nullable
    EulerUser provision(JwtBearerAssertion assertion) throws OAuth2AuthenticationException;
}
