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

import org.eulerframework.security.core.identity.UserIdentity;
import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import java.util.Collection;

/**
 * The result of a successful {@code public_key} authentication, in which the caller proved
 * possession of the private key matching a registered public key.
 * <p>
 * It carries the resolved {@link EulerUserDetails} as the principal together with the
 * {@link UserIdentity} the proof was verified against; the credentials are always
 * {@code null}, since a signature over a challenge is not a secret that could be replayed
 * as a credential. Mirrors {@link org.eulerframework.security.authentication.otp.OneTimePasswordAuthentication}:
 * an authenticated result token built on the
 * {@link org.springframework.security.core.Authentication.Builder} infrastructure, so that
 * whatever stored it can tell how the user was authenticated.
 * <p>
 * Lives in this module because the jwt-bearer issuer authenticator is the only thing that
 * constructs one, and it is what that grant stores: the {@code Principal} attribute of the
 * {@code OAuth2Authorization} it writes, and the principal in the {@code SecurityContext} of
 * anything downstream. It holds no OAuth2 state itself. Being written into a persisted
 * authorization is also why it has to be registered with the polymorphic type validator, which
 * {@link org.eulerframework.security.oauth2.server.authorization.jackson.EulerAuthorizationServerJacksonModule}
 * does; an authorization store reads the whole attribute map back in one pass, so a type the
 * validator does not admit fails the read outright rather than degrading one field.
 *
 * @see org.eulerframework.security.core.identity.UserIdentityService
 * @see OAuth2JwtBearerAuthenticationProvider
 */
public class PublicKeyAuthentication extends AbstractAuthenticationToken {

    /**
     * The authentication factor name stamped onto the result as a
     * {@code FactorGrantedAuthority} (authority value {@code FACTOR_PUBLIC_KEY}).
     */
    public static final String PUBLIC_KEY_FACTOR = "PUBLIC_KEY";

    private final EulerUserDetails principal;
    private final UserIdentity userIdentity;

    public PublicKeyAuthentication(EulerUserDetails principal, UserIdentity userIdentity,
                                   Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        Assert.notNull(principal, "principal cannot be null");
        this.principal = principal;
        this.userIdentity = userIdentity;
        setAuthenticated(true);
    }

    protected PublicKeyAuthentication(Builder<?> builder) {
        super(builder);
        this.principal = builder.principal;
        this.userIdentity = builder.userIdentity;
    }

    @Override
    public EulerUserDetails getPrincipal() {
        return this.principal;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    /**
     * Returns the {@code public_key} identity the proof was verified against.
     */
    public UserIdentity getUserIdentity() {
        return this.userIdentity;
    }

    @Override
    public Builder<?> toBuilder() {
        return new Builder<>(this);
    }

    /**
     * A builder of {@link PublicKeyAuthentication} instances.
     */
    public static class Builder<B extends Builder<B>> extends AbstractAuthenticationBuilder<B> {

        private EulerUserDetails principal;
        private UserIdentity userIdentity;

        protected Builder(PublicKeyAuthentication token) {
            super(token);
            this.principal = token.principal;
            this.userIdentity = token.userIdentity;
        }

        /**
         * Use this principal.
         *
         * @return the {@link Builder} for further configuration
         */
        @Override
        public B principal(Object principal) {
            Assert.notNull(principal, "principal cannot be null");
            Assert.isInstanceOf(EulerUserDetails.class, principal, "principal must be an EulerUserDetails");
            this.principal = (EulerUserDetails) principal;
            return (B) this;
        }

        /**
         * Use this user identity.
         *
         * @return the {@link Builder} for further configuration
         */
        public B userIdentity(UserIdentity userIdentity) {
            this.userIdentity = userIdentity;
            return (B) this;
        }

        @Override
        public PublicKeyAuthentication build() {
            return new PublicKeyAuthentication(this);
        }
    }
}
