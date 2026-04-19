/*
 * Copyright 2013-2024 the original author or authors.
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
package org.eulerframework.security.oauth2.server.authorization.web;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eulerframework.security.oauth2.core.EulerClientAuthenticationMethod;
import org.eulerframework.security.oauth2.core.endpoint.EulerOAuth2ParameterNames;
import org.eulerframework.security.oauth2.server.authorization.authentication.EulerOAuth2ClientAttestationVerifier;
import org.eulerframework.security.oauth2.server.authorization.authentication.EulerOAuth2ClientAttestationAuthenticationProvider;
import org.eulerframework.security.oauth2.server.authorization.web.authentication.EulerOAuth2ClientAttestationAuthenticationConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.http.converter.OAuth2ErrorHttpMessageConverter;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Post-authentication filter that handles Client Attestation on token endpoint requests,
 * as defined in
 * <a href="https://www.ietf.org/archive/id/draft-ietf-oauth-attestation-based-client-auth-08.html">
 * draft-ietf-oauth-attestation-based-client-auth-08</a>.
 * <p>
 * This filter runs <b>after</b> {@code OAuth2ClientAuthenticationFilter} and handles two cases:
 * <ul>
 *   <li><b>{@code attest_jwt_client_auth} clients</b>: Already fully authenticated by
 *       {@link EulerOAuth2ClientAttestationAuthenticationProvider
 *       ClientAttestationAuthenticationProvider}. This filter simply extracts the verified
 *       {@code key_id} from the authentication token and sets it as a request attribute
 *       for downstream components.</li>
 *   <li><b>Standard clients with attestation headers</b> (Scenario A): The client was
 *       authenticated via standard methods (e.g., {@code client_secret_basic}, PKCE).
 *       This filter verifies the attestation data as an additional security signal.</li>
 * </ul>
 * <p>
 * For Scenario A, PoP verification is dispatched by the
 * {@code OAuth-Client-Attestation-Type} header:
 * <ul>
 *   <li>{@code jwt} (default) — standard PoP JWT as defined in Section 5.2 of the draft.</li>
 *   <li>{@code apple_app_attest} — Apple App Attest Assertion used as PoP, with parameters
 *       ({@code kid}, {@code assertion}, {@code challenge}) in the request body.</li>
 * </ul>
 *
 * @see EulerOAuth2ClientAttestationAuthenticationProvider
 * @see EulerOAuth2ClientAttestationVerifier
 * @see EulerOAuth2ParameterNames
 */
public class EulerOAuth2AttestationBasedClientAuthenticationFilter extends OncePerRequestFilter {

    private final Logger logger = LoggerFactory.getLogger(EulerOAuth2AttestationBasedClientAuthenticationFilter.class);

    /**
     * Request attribute name for the verified key ID.
     * Set by this filter after successful attestation verification; read by downstream
     * converters (e.g., {@link org.eulerframework.security.oauth2.server.authorization.web.authentication.OAuth2AppAssertionAuthenticationConverter}).
     */
    public static final String ATTESTATION_VERIFIED_KEY_ID_ATTRIBUTE = "oauth2.client-attestation.verified.key_id";

    private final RequestMatcher tokenEndpointMatcher;


    private final HttpMessageConverter<OAuth2Error> errorHttpResponseConverter = new OAuth2ErrorHttpMessageConverter();

    private final EulerOAuth2ClientAttestationAuthenticationConverter clientAttestationAuthenticationConverter;
    private final EulerOAuth2ClientAttestationAuthenticationProvider clientAttestationAuthenticationProvider;

    private AuthenticationFailureHandler authenticationFailureHandler = this::onAuthenticationFailure;

    public EulerOAuth2AttestationBasedClientAuthenticationFilter(
            RequestMatcher tokenEndpointMatcher,
            EulerOAuth2ClientAttestationAuthenticationConverter clientAttestationAuthenticationConverter,
            EulerOAuth2ClientAttestationAuthenticationProvider clientAttestationAuthenticationProvider) {
        Assert.notNull(tokenEndpointMatcher, "tokenEndpointMatcher must not be null");
        Assert.notNull(clientAttestationAuthenticationConverter, "clientAttestationAuthenticationConverter must not be null");
        Assert.notNull(clientAttestationAuthenticationProvider, "clientAttestationAuthenticationProvider must not be null");
        this.tokenEndpointMatcher = tokenEndpointMatcher;
        this.clientAttestationAuthenticationConverter = clientAttestationAuthenticationConverter;
        this.clientAttestationAuthenticationProvider = clientAttestationAuthenticationProvider;
    }

    public void setAuthenticationFailureHandler(AuthenticationFailureHandler authenticationFailureHandler) {
        this.authenticationFailureHandler = authenticationFailureHandler;
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) throws ServletException, IOException {
        if (!this.tokenEndpointMatcher.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof OAuth2ClientAuthenticationToken clientAuthentication) || !clientAuthentication.isAuthenticated()) {
            // Not authenticated via standard client authentication; pass through to downstream filters
            filterChain.doFilter(request, response);
            return;
        }

        String kid = null;
        // Standard authentication used a traditional OAuth client authentication method.
        // If the request also carries attestation data, enter enhanced verification logic
        // by reusing the standard attest_jwt_client_auth AuthenticationConverter and AuthenticationProvider.
        if (!EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH
                .equals(clientAuthentication.getClientAuthenticationMethod())) {

            try {
                OAuth2ClientAuthenticationToken convertedAuthentication =
                        (OAuth2ClientAuthenticationToken) this.clientAttestationAuthenticationConverter.convert(request);
                if (convertedAuthentication != null) { // null means the request carries no attestation data
                    OAuth2ClientAuthenticationToken securitySignalToken = convertToSecuritySignalToken(convertedAuthentication);
                    OAuth2ClientAuthenticationToken clientAttestationAuthentication =
                            (OAuth2ClientAuthenticationToken) this.clientAttestationAuthenticationProvider.authenticate(securitySignalToken);
                    if (clientAttestationAuthentication == null || !clientAttestationAuthentication.isAuthenticated()) {
                        throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT));
                    }

                    // Verify that the client_id resolved from attestation data matches the
                    // client already authenticated via the standard method (Section 6.4 consistency).
                    RegisteredClient standardMethodAuthenticatedRegisteredClient = clientAuthentication.getRegisteredClient();
                    RegisteredClient securitySignalRegisteredClient = clientAttestationAuthentication.getRegisteredClient();
                    if (standardMethodAuthenticatedRegisteredClient != null &&
                            securitySignalRegisteredClient != null &&
                            !standardMethodAuthenticatedRegisteredClient.getClientId().equals(securitySignalRegisteredClient.getClientId())) {
                        throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT, "client_id mismatch", null));
                    }

                    kid = getKid(clientAttestationAuthentication);
                }
            } catch (OAuth2AuthenticationException ex) {
                if (this.logger.isTraceEnabled()) {
                    this.logger.trace("Attestation-Based Client Authentication failed: {}", ex.getError(), ex);
                }
                this.authenticationFailureHandler.onAuthenticationFailure(request, response, ex);
                return;
            }
        } else {
            kid = getKid(clientAuthentication);
        }

        if (kid != null) {
            request.setAttribute(ATTESTATION_VERIFIED_KEY_ID_ATTRIBUTE, kid);
        }
        filterChain.doFilter(request, response);
    }

    @Nonnull
    private static OAuth2ClientAuthenticationToken convertToSecuritySignalToken(OAuth2ClientAuthenticationToken convertedAuthentication) {
        Map<String, Object> additionalParameters = new HashMap<>(convertedAuthentication.getAdditionalParameters());
        additionalParameters.put(EulerOAuth2ParameterNames.ADDITIONAL_SECURITY_SIGNAL, true);
        return new OAuth2ClientAuthenticationToken(
                (String) convertedAuthentication.getPrincipal(),
                convertedAuthentication.getClientAuthenticationMethod(),
                convertedAuthentication.getCredentials(),
                additionalParameters
        );
    }

    public static String getKid(Authentication authentication) {
        return authentication.getCredentials() instanceof String k ? k : null;
    }

    private void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                         AuthenticationException exception) throws IOException {

        SecurityContextHolder.clearContext();

        OAuth2Error error = ((OAuth2AuthenticationException) exception).getError();
        ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
        if (OAuth2ErrorCodes.INVALID_CLIENT.equals(error.getErrorCode())) {
            httpResponse.setStatusCode(HttpStatus.UNAUTHORIZED);
        } else {
            httpResponse.setStatusCode(HttpStatus.BAD_REQUEST);
        }
        OAuth2Error errorResponse = new OAuth2Error(error.getErrorCode());
        this.errorHttpResponseConverter.write(errorResponse, null, httpResponse);
    }
}
