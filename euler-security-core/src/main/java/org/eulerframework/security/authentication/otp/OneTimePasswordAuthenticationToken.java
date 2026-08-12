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

import java.util.Collection;
import java.util.Collections;

/**
 * {@link org.springframework.security.core.Authentication Authentication}
 * token for one-time-password login.
 * <p>
 * The unauthenticated form carries the ticket id issued by the issue
 * endpoint as the principal and the submitted OTP value as the
 * credentials; the authenticated form carries the resolved
 * {@link EulerUserDetails} as the principal together with the
 * {@link UserIdentity} the ticket was verified against.
 *
 * @see OneTimePasswordAuthenticationProvider
 */
public class OneTimePasswordAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    private final UserIdentity userIdentity;
    private String otp;

    private OneTimePasswordAuthenticationToken(String otpTicket, String otp) {
        super(Collections.emptyList());
        this.principal = otpTicket;
        this.otp = otp;
        this.userIdentity = null;
        setAuthenticated(false);
    }

    private OneTimePasswordAuthenticationToken(EulerUserDetails principal, UserIdentity userIdentity,
                                               Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.userIdentity = userIdentity;
        super.setAuthenticated(true);
    }

    /**
     * Create an unauthenticated token from the submitted ticket id and OTP
     * value.
     *
     * @param otpTicket the ticket id issued by the issue endpoint
     * @param otp       the one-time password value submitted by the user
     */
    public static OneTimePasswordAuthenticationToken unauthenticated(String otpTicket, String otp) {
        return new OneTimePasswordAuthenticationToken(otpTicket, otp);
    }

    /**
     * Create an authenticated token for a successfully verified one-time
     * password.
     *
     * @param principal    the resolved user details
     * @param userIdentity the identity the ticket was verified against
     * @param authorities  the authorities granted to the principal
     */
    public static OneTimePasswordAuthenticationToken authenticated(EulerUserDetails principal, UserIdentity userIdentity,
                                                                   Collection<? extends GrantedAuthority> authorities) {
        return new OneTimePasswordAuthenticationToken(principal, userIdentity, authorities);
    }

    /**
     * Returns the submitted OTP value, or {@code null} once
     * {@link #eraseCredentials()} has been invoked. Always {@code null} on
     * the authenticated form.
     */
    public String getOtp() {
        return this.otp;
    }

    /**
     * Returns the identity the ticket was verified against; {@code null} on
     * the unauthenticated form.
     */
    public UserIdentity getUserIdentity() {
        return this.userIdentity;
    }

    @Override
    public Object getCredentials() {
        return this.otp;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        this.otp = null;
    }
}
