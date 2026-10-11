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
package org.eulerframework.security.oauth2.server.authorization.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eulerframework.common.util.jackson.JacksonUtils;
import org.eulerframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationChallengeAuthenticationToken;
import org.eulerframework.security.oauth2.server.authorization.web.authentication.OAuth2AuthorizationChallengeAuthenticationConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The Authorization Challenge Endpoint of
 * <a href="https://www.ietf.org/archive/id/draft-ietf-oauth-first-party-apps-04.txt">
 * draft-ietf-oauth-first-party-apps-04</a>: a browserless {@code POST} that a first-party native app
 * uses to turn a user authentication into an {@code authorization_code}, which it then redeems at the
 * standard token endpoint with {@code grant_type=authorization_code} and PKCE.
 * <p>
 * The filter follows the same shape as Spring's {@code OAuth2TokenEndpointFilter}: an
 * {@link AuthenticationConverter} builds the request token, the injected {@link AuthenticationManager}
 * (the authorization server's shared manager, into which
 * {@link org.eulerframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationChallengeAuthenticationProvider}
 * is registered) performs the work, and success / failure handlers write the response. Client
 * authentication is not done here: a preceding {@code OAuth2ClientAuthenticationFilter} bound to this
 * endpoint authenticates the client and leaves it on the {@code SecurityContext} for the converter to
 * read (Section 4.1 requires the same client authentication as the token endpoint).
 * <p>
 * The success body is {@code {"authorization_code": "..."}} (Section 5.2.1); errors are the standard
 * OAuth 2.0 error JSON (Section 5.2.2). The endpoint is exempt from CSRF and responds without
 * continuing the chain, so it never reaches the chain's authorization filter.
 *
 * @see OAuth2AuthorizationChallengeAuthenticationConverter
 */
public class OAuth2AuthorizationChallengeEndpointFilter extends OncePerRequestFilter {

    /**
     * The authorization code success response parameter (Section 5.2.1).
     */
    public static final String AUTHORIZATION_CODE_PARAMETER = "authorization_code";

    private static final String SERVER_ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2";

    private final Logger logger = LoggerFactory.getLogger(OAuth2AuthorizationChallengeEndpointFilter.class);

    private final RequestMatcher requestMatcher;
    private final AuthenticationManager authenticationManager;
    private AuthenticationConverter authenticationConverter = new OAuth2AuthorizationChallengeAuthenticationConverter();
    private AuthenticationSuccessHandler authenticationSuccessHandler = this::onAuthenticationSuccess;
    private AuthenticationFailureHandler authenticationFailureHandler = this::onAuthenticationFailure;

    public OAuth2AuthorizationChallengeEndpointFilter(AuthenticationManager authenticationManager,
                                                      RequestMatcher requestMatcher) {
        Assert.notNull(authenticationManager, "authenticationManager must not be null");
        Assert.notNull(requestMatcher, "requestMatcher must not be null");
        this.authenticationManager = authenticationManager;
        this.requestMatcher = requestMatcher;
    }

    /**
     * Returns the {@link RequestMatcher} for this endpoint, used to configure CSRF exemption and the
     * security filter chain's matcher.
     */
    public RequestMatcher getRequestMatcher() {
        return this.requestMatcher;
    }

    public void setAuthenticationConverter(AuthenticationConverter authenticationConverter) {
        Assert.notNull(authenticationConverter, "authenticationConverter must not be null");
        this.authenticationConverter = authenticationConverter;
    }

    public void setAuthenticationSuccessHandler(AuthenticationSuccessHandler authenticationSuccessHandler) {
        Assert.notNull(authenticationSuccessHandler, "authenticationSuccessHandler must not be null");
        this.authenticationSuccessHandler = authenticationSuccessHandler;
    }

    public void setAuthenticationFailureHandler(AuthenticationFailureHandler authenticationFailureHandler) {
        Assert.notNull(authenticationFailureHandler, "authenticationFailureHandler must not be null");
        this.authenticationFailureHandler = authenticationFailureHandler;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!this.requestMatcher.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Authentication authorizationChallengeAuthentication = this.authenticationConverter.convert(request);
            Authentication authorizationChallengeResult =
                    this.authenticationManager.authenticate(authorizationChallengeAuthentication);
            this.authenticationSuccessHandler.onAuthenticationSuccess(request, response, authorizationChallengeResult);
        } catch (OAuth2AuthenticationException ex) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Authorization challenge request failed: {}", ex.getError().getErrorCode());
            }
            this.authenticationFailureHandler.onAuthenticationFailure(request, response, ex);
        }
    }

    private void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException {
        OAuth2AuthorizationChallengeAuthenticationToken result =
                (OAuth2AuthorizationChallengeAuthenticationToken) authentication;

        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.addHeader("Cache-Control", "no-store");
        response.addHeader("Pragma", "no-cache");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put(AUTHORIZATION_CODE_PARAMETER, result.getAuthorizationCode());
        response.getWriter().write(JacksonUtils.writeValueAsString(body));
    }

    private void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                         AuthenticationException exception) throws IOException {
        OAuth2Error error = (exception instanceof OAuth2AuthenticationException oauth2Exception)
                ? oauth2Exception.getError()
                : new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR, "Authorization challenge request failed.",
                        SERVER_ERROR_URI);

        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.addHeader("Cache-Control", "no-store");
        response.addHeader("Pragma", "no-cache");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put(OAuth2ParameterNames.ERROR, error.getErrorCode());
        if (StringUtils.hasText(error.getDescription())) {
            body.put(OAuth2ParameterNames.ERROR_DESCRIPTION, error.getDescription());
        }
        if (StringUtils.hasText(error.getUri())) {
            body.put(OAuth2ParameterNames.ERROR_URI, error.getUri());
        }
        response.getWriter().write(JacksonUtils.writeValueAsString(body));
    }
}
