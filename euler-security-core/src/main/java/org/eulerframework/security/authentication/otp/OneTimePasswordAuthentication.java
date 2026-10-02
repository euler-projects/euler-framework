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
package org.eulerframework.security.authentication.otp;

import org.eulerframework.security.core.identity.UserIdentity;
import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import java.util.Collection;

/**
 * The result of a successful one-time-password authentication.
 * <p>
 * It carries the resolved {@link EulerUserDetails} as the principal together
 * with the {@link UserIdentity} the ticket was verified against; the
 * credentials are always {@code null}. Mirrors the shape of Spring Security's
 * {@code OneTimeTokenAuthentication}: an authenticated result token built on
 * the {@link org.springframework.security.core.Authentication.Builder}
 * infrastructure rather than the unauthenticated request token
 * {@link OneTimePasswordAuthenticationToken}.
 *
 * @see OneTimePasswordAuthenticationProvider
 */
public class OneTimePasswordAuthentication extends AbstractAuthenticationToken {

    /**
     * The authentication factor name stamped onto the result as a
     * {@code FactorGrantedAuthority} (authority value {@code FACTOR_OTP}).
     */
    public static final String OTP_FACTOR = "OTP";

    private final EulerUserDetails principal;
    private final UserIdentity userIdentity;

    public OneTimePasswordAuthentication(EulerUserDetails principal, UserIdentity userIdentity,
                                         Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        Assert.notNull(principal, "principal cannot be null");
        this.principal = principal;
        this.userIdentity = userIdentity;
        setAuthenticated(true);
    }

    protected OneTimePasswordAuthentication(Builder<?> builder) {
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
     * Returns the identity the ticket was verified against.
     */
    public UserIdentity getUserIdentity() {
        return this.userIdentity;
    }

    @Override
    public Builder<?> toBuilder() {
        return new Builder<>(this);
    }

    /**
     * A builder of {@link OneTimePasswordAuthentication} instances.
     */
    public static class Builder<B extends Builder<B>> extends AbstractAuthenticationBuilder<B> {

        private EulerUserDetails principal;
        private UserIdentity userIdentity;

        protected Builder(OneTimePasswordAuthentication token) {
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
        public OneTimePasswordAuthentication build() {
            return new OneTimePasswordAuthentication(this);
        }
    }
}
