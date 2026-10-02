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

import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;

/**
 * Unauthenticated {@link org.springframework.security.core.Authentication
 * Authentication} request token for one-time-password login.
 * <p>
 * It carries the ticket id issued by the issue endpoint ({@link #getTicketId()})
 * and the submitted OTP value ({@link #getOtp()}, which is also the
 * credentials). The principal is {@code null} until verification resolves the
 * identity, mirroring Spring Security's {@code OneTimeTokenAuthenticationToken},
 * which keeps the token value in a dedicated field separate from the principal.
 * A successful verification yields a separate {@link OneTimePasswordAuthentication}
 * result token.
 *
 * @see OneTimePasswordAuthenticationProvider
 * @see OneTimePasswordAuthentication
 */
public class OneTimePasswordAuthenticationToken extends AbstractAuthenticationToken {

    private final String ticketId;
    private String otp;

    /**
     * Create an unauthenticated token from the submitted ticket id and OTP
     * value.
     *
     * @param ticketId the ticket id issued by the issue endpoint
     * @param otp      the one-time password value submitted by the user
     */
    public OneTimePasswordAuthenticationToken(String ticketId, String otp) {
        super(Collections.emptyList());
        this.ticketId = ticketId;
        this.otp = otp;
    }

    /**
     * Returns the ticket id issued by the issue endpoint - the handle of the
     * one-time password being presented. Analogous to Spring Security's
     * {@code OneTimeTokenAuthenticationToken#getTokenValue()}.
     */
    public String getTicketId() {
        return this.ticketId;
    }

    /**
     * Returns the submitted OTP value, or {@code null} once
     * {@link #eraseCredentials()} has been invoked.
     */
    public String getOtp() {
        return this.otp;
    }

    @Override
    public Object getCredentials() {
        return this.otp;
    }

    /**
     * Always {@code null}: the identity is not known until the ticket is
     * verified, at which point a {@link OneTimePasswordAuthentication} carries
     * the resolved principal.
     */
    @Override
    public Object getPrincipal() {
        return null;
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        this.otp = null;
    }
}
