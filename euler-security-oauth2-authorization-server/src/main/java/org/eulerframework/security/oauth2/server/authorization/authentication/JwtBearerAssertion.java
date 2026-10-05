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

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.eulerframework.security.util.JwkUtils;
import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * A jwt-bearer assertion that has passed the checks which do not depend on who issued it, handed
 * to the {@link JwtBearerIssuerAuthenticator} that vouches for the issuer it names.
 * <p>
 * Built by {@link OAuth2JwtBearerAuthenticationProvider} once the assertion is known to be a
 * well-formed JWS with a well-formed payload, naming an issuer, addressed to this authorization
 * server and inside its lifetime. What is left is issuer-specific.
 * <p>
 * Deliberately immutable and deliberately ignorant of replay protection. Making the assertion
 * single-use is the provider's job and it does it <em>between</em> the two calls an issuer
 * authenticator answers &mdash; after {@link JwtBearerIssuerAuthenticator#verify} and before
 * {@link JwtBearerIssuerAuthenticator#provision} &mdash; so the ordering cannot be gotten wrong by
 * an implementation, only by the provider. Nothing here holds state an implementation could
 * corrupt, forget to set, or set without having done the work.
 *
 * @see JwtBearerIssuerAuthenticator
 * @see OAuth2JwtBearerAuthenticationProvider
 */
public final class JwtBearerAssertion {

    private final SignedJWT jwt;
    private final JWTClaimsSet claims;
    private final OAuth2ClientAuthenticationToken clientPrincipal;

    /**
     * @param jwt             the parsed assertion; its envelope has already been validated
     * @param claims          {@code jwt}'s payload, read out once for the caller
     * @param clientPrincipal the client authentication this grant request was made under
     */
    public JwtBearerAssertion(SignedJWT jwt,
                              JWTClaimsSet claims,
                              OAuth2ClientAuthenticationToken clientPrincipal) {
        Assert.notNull(jwt, "jwt must not be null");
        Assert.notNull(claims, "claims must not be null");
        Assert.notNull(clientPrincipal, "clientPrincipal must not be null");
        this.jwt = jwt;
        this.claims = claims;
        this.clientPrincipal = clientPrincipal;
    }

    /**
     * The assertion itself, for an authenticator that needs a part of it this class does not
     * expose &mdash; a header member other than {@code kid}, or the serialised form it has to hand
     * to a decoder of its own.
     */
    public SignedJWT getJwt() {
        return this.jwt;
    }

    /**
     * The assertion's payload. Its {@code iss}, {@code aud}, {@code exp} and {@code iat} have been
     * checked; anything else is for the authenticator to make of what it will.
     */
    public JWTClaimsSet getClaims() {
        return this.claims;
    }

    /**
     * The assertion's {@code iss}, which the grant provider has already found an authenticator
     * for. Never empty.
     */
    public String getIssuer() {
        return this.claims.getIssuer();
    }

    /**
     * The assertion's {@code sub}, or {@code null} when it names no account &mdash; which for an
     * issuer allowed to open accounts is a first login.
     */
    @Nullable
    public String getSubject() {
        String subject = this.claims.getSubject();
        return StringUtils.hasText(subject) ? subject : null;
    }

    /**
     * The assertion header's {@code kid}, or {@code null} when it carries none &mdash; an empty
     * value counts as none, so that a caller that set the member to nothing is treated as one that
     * left it out. A hint rather than an instruction (RFC 7515 Section 4.1.4): what makes a key
     * acceptable is that the issuer vouches for it, not that this names it.
     */
    @Nullable
    public String getKeyId() {
        String keyId = this.jwt.getHeader().getKeyID();
        return StringUtils.hasText(keyId) ? keyId : null;
    }

    /**
     * The client authentication this grant request was made under. An authenticator reads the
     * mechanism off it when a deployment admits only some mechanisms, and the registered client off
     * it when an issuer is itself a client of this server.
     */
    public OAuth2ClientAuthenticationToken getClientPrincipal() {
        return this.clientPrincipal;
    }

    /**
     * Check the assertion's signature against the given public key.
     * <p>
     * The verifier is chosen from the key's own type rather than from the header's algorithm, so a
     * header naming an algorithm the key cannot make is refused instead of being reinterpreted
     * &mdash; the confusion that would let a public key be used as an HMAC secret.
     * <p>
     * Offered here so that every authenticator reports a bad signature the same way and none of
     * them has to know which algorithms this server accepts. It does nothing but check: spending
     * the assertion is the provider's job, and an authenticator that verifies against several
     * candidate keys in turn is free to stop at the first that holds.
     *
     * @param publicKey the public JWK the issuer vouches for; never {@code null}
     * @throws OAuth2AuthenticationException with {@code invalid_grant} if the signature does not
     *                                       verify or the key cannot verify it
     */
    public void verifySignature(JWK publicKey) {
        Assert.notNull(publicKey, "publicKey must not be null");
        JWSVerifier verifier;
        try {
            verifier = JwkUtils.createJwsVerifier(this.jwt.getHeader(), publicKey);
        } catch (JOSEException | RuntimeException e) {
            throw JwtBearerErrors.invalidGrant("the assertion cannot be verified with this key: " + e.getMessage());
        }
        try {
            if (!this.jwt.verify(verifier)) {
                throw JwtBearerErrors.invalidGrant("the assertion signature does not match this key");
            }
        } catch (JOSEException e) {
            throw JwtBearerErrors.invalidGrant("the assertion signature could not be verified: " + e.getMessage());
        }
    }
}
