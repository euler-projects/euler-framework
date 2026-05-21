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

import org.eulerframework.security.oauth2.core.EulerAuthorizationGrantType;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Unauthenticated grant token carrying the credentials submitted at the
 * {@code POST /oauth2/token} endpoint when {@code grant_type=otp}:
 * <ul>
 *     <li>{@code otp_ticket}  - the ticket id previously returned by
 *         {@code POST /otp/tickets}.</li>
 *     <li>{@code otp}         - the one-time password value the end-user typed
 *         back from the chosen delivery channel.</li>
 *     <li>{@code code_verifier} - PKCE code verifier (RFC 7636) whose S256
 *         hash must equal the {@code code_challenge} stored on the ticket.
 *         May be {@code null} when PKCE is disabled (see
 *         {@code euler.security.otp.pkce.enabled}).</li>
 * </ul>
 * <p>
 * The optional verified App Attest registration (carried in by
 * {@link org.eulerframework.security.oauth2.server.authorization.web.EulerOAuth2AttestationBasedClientAuthenticationFilter})
 * is propagated through the parent
 * {@link OAuth2AuthorizationGrantAuthenticationToken#getAdditionalParameters() additionalParameters}
 * map under the key
 * {@link org.eulerframework.security.oauth2.server.authorization.web.EulerOAuth2AttestationBasedClientAuthenticationFilter#VERIFIED_CLIENT_ATTESTATION_PARAMETER}.
 * When present, the provider enforces device-to-user consistency and
 * auto-binds the device to the OTP-resolved user on first use.
 * <p>
 * Verified by {@code OAuth2OtpAuthenticationProvider}.
 */
public class OAuth2OtpAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {

    private final String otpTicket;
    private final String otp;
    private final String codeVerifier;
    private final Set<String> scopes;

    public OAuth2OtpAuthenticationToken(
            String otpTicket,
            String otp,
            @Nullable String codeVerifier,
            Authentication clientPrincipal,
            @Nullable Set<String> scopes,
            @Nullable Map<String, Object> additionalParameters) {
        super(EulerAuthorizationGrantType.OTP, clientPrincipal, additionalParameters);
        Assert.hasText(otpTicket, "otpTicket must not be empty");
        Assert.hasText(otp, "otp must not be empty");
        this.otpTicket = otpTicket;
        this.otp = otp;
        this.codeVerifier = codeVerifier;
        this.scopes = Collections.unmodifiableSet(
                scopes != null ?
                        new HashSet<>(scopes) :
                        Collections.emptySet());
    }

    public String getOtpTicket() {
        return otpTicket;
    }

    public String getOtp() {
        return otp;
    }

    public String getCodeVerifier() {
        return codeVerifier;
    }

    public Set<String> getScopes() {
        return scopes;
    }
}
