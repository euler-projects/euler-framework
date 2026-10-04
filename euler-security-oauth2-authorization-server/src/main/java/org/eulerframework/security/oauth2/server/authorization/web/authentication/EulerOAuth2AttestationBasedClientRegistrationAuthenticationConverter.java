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
package org.eulerframework.security.oauth2.server.authorization.web.authentication;

import jakarta.servlet.http.HttpServletRequest;
import org.eulerframework.security.oauth2.core.EulerOAuth2ErrorCodes;
import org.eulerframework.security.oauth2.server.authorization.authentication.EulerOAuth2AttestationBasedClientRegistrationAuthenticationToken;
import org.eulerframework.security.web.authentication.appattest.AppAttestCredential;
import org.eulerframework.security.web.authentication.appattest.AppAttestCredentialResolver;
import org.eulerframework.security.web.authentication.appattest.AppAttestParameterNames;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.OAuth2ClientRegistration;
import org.springframework.security.oauth2.server.authorization.http.converter.OAuth2ClientRegistrationHttpMessageConverter;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.StringUtils;

/**
 * An {@link AuthenticationConverter} that extracts an Apple App Attest client registration
 * request from {@link HttpServletRequest} and converts it to an
 * {@link EulerOAuth2AttestationBasedClientRegistrationAuthenticationToken}.
 * <p>
 * Registered ahead of Spring's {@code OAuth2ClientRegistrationAuthenticationConverter}, which
 * the {@code DelegatingAuthenticationConverter} reaches only when this one returns {@code null}.
 * The two converters split the RFC 7591 registration endpoint by credential:
 * <ul>
 *   <li>an App Attest credential present &rarr; this converter, taking precedence even when a bearer
 *       token is also present, since the assertion is the more specific credential</li>
 *   <li>an authenticated initial access token &rarr; {@code null}, falling through to Spring's
 *       converter and its {@code OAuth2ClientRegistrationAuthenticationProvider}</li>
 *   <li>neither &rarr; rejected here, before the request body is read</li>
 * </ul>
 * <p>
 * The third branch exists because the endpoint is configured {@code permitAll} so that App Attest
 * requests, which carry no bearer token, can reach it at all; enforcement therefore moves from the
 * filter chain into this converter and the providers. Rejecting here is what keeps an anonymous
 * request from having its body parsed: returning {@code null} instead would delegate to Spring's
 * converter, which reads the body unconditionally.
 * <p>
 * The credential test mirrors {@code OAuth2ClientRegistrationAuthenticationProvider} exactly, so
 * the two never disagree about which requests are admissible. It assumes that provider's
 * {@code openRegistrationAllowed} stays {@code false}; enabling open registration would make this
 * converter reject requests that provider would accept.
 *
 * @see EulerOAuth2AttestationBasedClientRegistrationAuthenticationToken
 */
public final class EulerOAuth2AttestationBasedClientRegistrationAuthenticationConverter
        implements AuthenticationConverter {

    private final HttpMessageConverter<OAuth2ClientRegistration> clientRegistrationHttpMessageConverter =
            new OAuth2ClientRegistrationHttpMessageConverter();

    @Override
    public Authentication convert(HttpServletRequest request) {
        AppAttestCredential credential = AppAttestCredentialResolver.resolve(request);
        if (credential.isPresent()) {
            return convertClientAttestation(request, credential);
        }

        if (!isInitialAccessTokenAuthenticated()) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN,
                    "Client registration requires either an RFC 7591 initial access token "
                            + "or an App Attest assertion", null));
        }

        return null;
    }

    /**
     * Validate the App Attest credential structurally, then read the request body. The checks are
     * deliberately limited to field presence and shape so that an unauthenticated caller cannot get
     * the body parsed by sending fields that are merely well-formed; consuming the challenge and
     * verifying the assertion stay in the provider, where side effects and crypto belong.
     * <p>
     * Unlike the token endpoint, this endpoint knows no deprecated carriage: released clients never
     * registered dynamically, so the credential is read from the App Attest carriage alone.
     */
    private Authentication convertClientAttestation(HttpServletRequest request, AppAttestCredential credential) {
        // The credential rides the App Attest carriage shared with /app_attest/**: the App-Attest-*
        // headers or the app_attest_* form parameters, never a mix of the two.
        if (!StringUtils.hasText(credential.kid())) {
            throw invalidClientAttestation(AppAttestParameterNames.HEADER_KID);
        }

        if (!StringUtils.hasText(credential.challenge())) {
            throw invalidClientAttestation(AppAttestParameterNames.HEADER_CHALLENGE);
        }

        if (!StringUtils.hasText(credential.assertion())) {
            throw invalidClientAttestation(AppAttestParameterNames.HEADER_ASSERTION);
        }

        return new EulerOAuth2AttestationBasedClientRegistrationAuthenticationToken(
                readClientRegistration(request), credential.kid(), credential.challenge(), credential.assertion());
    }

    /**
     * Whether an initial access token has already been authenticated into the security context by
     * the resource server support that enabling the registration endpoint turns on. Mirrors
     * {@code OAuth2ClientRegistrationAuthenticationProvider}, which requires the principal to be an
     * {@link AbstractOAuth2TokenAuthenticationToken} and to be authenticated.
     */
    private static boolean isInitialAccessTokenAuthenticated() {
        Authentication principal = SecurityContextHolder.getContext().getAuthentication();
        return principal instanceof AbstractOAuth2TokenAuthenticationToken<?> tokenAuthentication
                && tokenAuthentication.isAuthenticated();
    }

    private OAuth2ClientRegistration readClientRegistration(HttpServletRequest request) {
        OAuth2ClientRegistration clientRegistration;
        try {
            clientRegistration = this.clientRegistrationHttpMessageConverter.read(
                    OAuth2ClientRegistration.class, new ServletServerHttpRequest(request));
        } catch (Exception ex) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST,
                    "Invalid client registration request body", null), ex);
        }
        if (clientRegistration == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST,
                    "Missing client registration request body", null));
        }
        return clientRegistration;
    }

    private static OAuth2AuthenticationException invalidClientAttestation(String parameterName) {
        OAuth2Error error = new OAuth2Error(EulerOAuth2ErrorCodes.INVALID_CLIENT_ATTESTATION,
                "Client attestation failed: " + parameterName, null);
        return new OAuth2AuthenticationException(error);
    }

}
