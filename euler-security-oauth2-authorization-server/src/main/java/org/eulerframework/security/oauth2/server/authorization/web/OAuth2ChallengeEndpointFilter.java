/*
 * Copyright 2013-2026 the original author or authors.
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

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eulerframework.common.util.jackson.JacksonUtils;
import org.eulerframework.security.authentication.ChallengeService;
import org.eulerframework.security.authentication.GeneratedChallenge;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import tools.jackson.databind.ObjectMapper;

/**
 * A filter that exposes a {@code POST /oauth2/challenge} endpoint for generating
 * one-time challenges used in authentication flows that require a challenge-response
 * mechanism (e.g., Apple App Attest, WebAuthn).
 * <p>
 * This endpoint requires the request to be already authenticated (typically via
 * OAuth2 client credentials in the {@code Authorization} header).
 * <p>
 * If {@code authorizedGrantTypes} are configured, only clients that are authorized
 * for at least one of the specified grant types can obtain a challenge. If no grant
 * types are configured, any authenticated client can obtain a challenge.
 * <p>
 * Response format:
 * <pre>
 * {"challenge": "random-base64url-string", "format": "base64url"}
 * </pre>
 */
public class OAuth2ChallengeEndpointFilter extends OncePerRequestFilter {

    private static final String DEFAULT_CHALLENGE_ENDPOINT_URI = "/oauth2/challenge";

    private final ChallengeService challengeService;
    private final RequestMatcher requestMatcher;
    private final ObjectMapper objectMapper = JacksonUtils.getDefaultObjectMapper();

    private Set<AuthorizationGrantType> authorizedGrantTypes = Collections.emptySet();
    private AuthenticationFailureHandler errorResponseHandler = this::sendErrorResponse;
    private ChallengeGenerationSuccessHandler challengeResponseHandler = this::sendChallengeResponse;

    public OAuth2ChallengeEndpointFilter(ChallengeService challengeService) {
        this(challengeService, DEFAULT_CHALLENGE_ENDPOINT_URI);
    }

    public OAuth2ChallengeEndpointFilter(ChallengeService challengeService, String endpointUri) {
        Assert.notNull(challengeService, "challengeService must not be null");
        Assert.hasText(endpointUri, "endpointUri must not be empty");
        this.challengeService = challengeService;
        this.requestMatcher = PathPatternRequestMatcher.pathPattern(HttpMethod.POST, endpointUri);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!this.requestMatcher.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (!(authentication instanceof OAuth2ClientAuthenticationToken clientAuthenticationToken)
                    || !clientAuthenticationToken.isAuthenticated()) {
                throw new OAuth2AuthenticationException(new OAuth2Error(
                        OAuth2ErrorCodes.INVALID_CLIENT,
                        "Client authentication is required", null));
            }

            RegisteredClient registeredClient = clientAuthenticationToken.getRegisteredClient();
            if (registeredClient == null) {
                throw new OAuth2AuthenticationException(new OAuth2Error(
                        OAuth2ErrorCodes.INVALID_CLIENT,
                        "Client authentication is required", null));
            }

            // If authorized grant types are configured, verify the client is authorized for at least one
            if (!this.authorizedGrantTypes.isEmpty()) {
                boolean authorized = false;
                for (AuthorizationGrantType grantType : this.authorizedGrantTypes) {
                    if (registeredClient.getAuthorizationGrantTypes().contains(grantType)) {
                        authorized = true;
                        break;
                    }
                }
                if (!authorized) {
                    throw new OAuth2AuthenticationException(new OAuth2Error(
                            OAuth2ErrorCodes.ACCESS_DENIED,
                            "Client is not authorized for any challenge-required grant type", null));
                }
            }

            String clientId = registeredClient.getClientId();
            GeneratedChallenge challenge = this.challengeService.generateChallenge(clientId);

            this.challengeResponseHandler.onChallengeGenerated(request, response, clientAuthenticationToken, challenge);
        } catch (OAuth2AuthenticationException ex) {
            this.errorResponseHandler.onAuthenticationFailure(request, response, ex);
        }
    }

    /**
     * Set the {@link AuthorizationGrantType}s that authorize a client to obtain a challenge.
     * <p>
     * If the set is non-empty, only clients authorized for at least one of the specified
     * grant types can use this endpoint. If the set is empty (the default), any authenticated
     * client can obtain a challenge.
     *
     * @param authorizedGrantTypes the grant types that require challenges
     */
    public void setAuthorizedGrantTypes(Set<AuthorizationGrantType> authorizedGrantTypes) {
        Assert.notNull(authorizedGrantTypes, "authorizedGrantTypes must not be null");
        this.authorizedGrantTypes = authorizedGrantTypes;
    }

    /**
     * Set the {@link AuthenticationFailureHandler} used for handling an
     * {@link OAuth2AuthenticationException} and returning the error response.
     *
     * @param errorResponseHandler the handler for error responses
     */
    public void setErrorResponseHandler(AuthenticationFailureHandler errorResponseHandler) {
        Assert.notNull(errorResponseHandler, "errorResponseHandler cannot be null");
        this.errorResponseHandler = errorResponseHandler;
    }

    /**
     * Set the {@link ChallengeGenerationSuccessHandler} used for handling a
     * successfully generated challenge and returning the response.
     *
     * @param challengeResponseHandler the handler for successful challenge responses
     */
    public void setChallengeResponseHandler(ChallengeGenerationSuccessHandler challengeResponseHandler) {
        Assert.notNull(challengeResponseHandler, "challengeResponseHandler cannot be null");
        this.challengeResponseHandler = challengeResponseHandler;
    }

    /**
     * Return the {@link RequestMatcher} for this endpoint filter, so it can be
     * registered with the authorization server's security matcher.
     *
     * @return the request matcher
     */
    public RequestMatcher getRequestMatcher() {
        return this.requestMatcher;
    }

    private void sendChallengeResponse(HttpServletRequest request, HttpServletResponse response,
                                       Authentication clientAuthentication, GeneratedChallenge challenge) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        this.objectMapper.writeValue(response.getWriter(), challenge);
    }

    private void sendErrorResponse(HttpServletRequest request, HttpServletResponse response,
                                   org.springframework.security.core.AuthenticationException exception) throws IOException {
        OAuth2Error error = ((OAuth2AuthenticationException) exception).getError();

        int status;
        if (OAuth2ErrorCodes.INVALID_CLIENT.equals(error.getErrorCode())) {
            status = HttpStatus.UNAUTHORIZED.value();
        } else {
            status = HttpStatus.FORBIDDEN.value();
        }

        Map<String, String> body = new LinkedHashMap<>();
        body.put("error", error.getErrorCode());
        if (error.getDescription() != null) {
            body.put("error_description", error.getDescription());
        }

        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        this.objectMapper.writeValue(response.getWriter(), body);
    }
}
