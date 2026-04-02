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
package org.eulerframework.security.oauth2.server.authorization.config.annotation.web.configurers;

import org.eulerframework.security.authentication.ChallengeService;
import org.eulerframework.security.oauth2.server.authorization.web.ChallengeGenerationSuccessHandler;
import org.eulerframework.security.oauth2.server.authorization.web.OAuth2ChallengeEndpointFilter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * Configurer for the OAuth 2.0 Challenge Endpoint.
 * <p>
 * This is a sub-configurer managed by {@link EulerOAuth2AuthorizationServerConfigurer},
 * responsible for registering and configuring the {@link OAuth2ChallengeEndpointFilter}
 * that exposes a {@code POST /oauth2/challenge} endpoint (configurable) for generating
 * one-time challenges used in authentication flows that require a challenge-response
 * mechanism (e.g., Apple App Attest, WebAuthn).
 *
 * @see EulerOAuth2AuthorizationServerConfigurer#challengeEndpoint
 * @see OAuth2ChallengeEndpointFilter
 * @see ChallengeService
 */
public class OAuth2ChallengeEndpointConfigurer {

    private static final String DEFAULT_CHALLENGE_ENDPOINT_URI = "/oauth2/challenge";

    private String challengeEndpointUri = DEFAULT_CHALLENGE_ENDPOINT_URI;
    private ChallengeService challengeService;
    private final Set<AuthorizationGrantType> authorizedGrantTypes = new LinkedHashSet<>();
    private AuthenticationFailureHandler errorResponseHandler;
    private ChallengeGenerationSuccessHandler challengeResponseHandler;
    private RequestMatcher requestMatcher;

    /**
     * Set the URI for the challenge endpoint.
     *
     * @param uri the endpoint URI (e.g., {@code "/oauth2/challenge"})
     * @return this configurer for further customization
     */
    public OAuth2ChallengeEndpointConfigurer challengeEndpoint(String uri) {
        Assert.hasText(uri, "challengeEndpointUri must not be empty");
        this.challengeEndpointUri = uri;
        this.requestMatcher = null; // invalidate cached matcher
        return this;
    }

    /**
     * Set the {@link ChallengeService} to use.
     * <p>
     * If not set, the service will be resolved from the {@link ApplicationContext}.
     *
     * @param challengeService the challenge service
     * @return this configurer for further customization
     */
    public OAuth2ChallengeEndpointConfigurer challengeService(ChallengeService challengeService) {
        Assert.notNull(challengeService, "challengeService must not be null");
        this.challengeService = challengeService;
        return this;
    }

    /**
     * Set the {@link AuthorizationGrantType}s that authorize a client to obtain a challenge.
     * <p>
     * If configured, only clients authorized for at least one of the specified grant types
     * can use the challenge endpoint. If not configured (the default), any authenticated
     * client can obtain a challenge.
     *
     * @param grantTypes the grant types that require challenges
     * @return this configurer for further customization
     */
    public OAuth2ChallengeEndpointConfigurer authorizedGrantTypes(AuthorizationGrantType... grantTypes) {
        Assert.notEmpty(grantTypes, "grantTypes must not be empty");
        this.authorizedGrantTypes.addAll(Arrays.asList(grantTypes));
        return this;
    }

    /**
     * Set the {@link AuthenticationFailureHandler} used for handling an
     * {@code OAuth2AuthenticationException} and returning the error response.
     *
     * @param errorResponseHandler the error response handler
     * @return this configurer for further customization
     */
    public OAuth2ChallengeEndpointConfigurer errorResponseHandler(AuthenticationFailureHandler errorResponseHandler) {
        Assert.notNull(errorResponseHandler, "errorResponseHandler must not be null");
        this.errorResponseHandler = errorResponseHandler;
        return this;
    }

    /**
     * Set the {@link ChallengeGenerationSuccessHandler} used for handling a
     * successfully generated challenge and returning the response.
     *
     * @param challengeResponseHandler the challenge response handler
     * @return this configurer for further customization
     */
    public OAuth2ChallengeEndpointConfigurer challengeResponseHandler(ChallengeGenerationSuccessHandler challengeResponseHandler) {
        Assert.notNull(challengeResponseHandler, "challengeResponseHandler must not be null");
        this.challengeResponseHandler = challengeResponseHandler;
        return this;
    }

    /**
     * Return the {@link RequestMatcher} for the challenge endpoint.
     *
     * @return the request matcher for the challenge endpoint
     */
    public RequestMatcher getRequestMatcher() {
        if (this.requestMatcher == null) {
            this.requestMatcher = PathPatternRequestMatcher.pathPattern(HttpMethod.POST, this.challengeEndpointUri);
        }
        return this.requestMatcher;
    }

    /**
     * Initialize this sub-configurer. Called by the parent
     * {@link EulerOAuth2AuthorizationServerConfigurer} during its {@code init()} phase.
     *
     * @param http the {@link HttpSecurity} to initialize
     */
    void init(HttpSecurity http) {
        // Ensure the request matcher is built
        getRequestMatcher();
    }

    /**
     * Configure this sub-configurer. Called by the parent
     * {@link EulerOAuth2AuthorizationServerConfigurer} during its {@code configure()} phase.
     *
     * @param http        the {@link HttpSecurity} to configure
     * @param postProcess the post-processor function from the parent configurer
     */
    void configure(HttpSecurity http, Function<Object, Object> postProcess) {
        ChallengeService resolvedChallengeService = getChallengeService(http);

        OAuth2ChallengeEndpointFilter oauth2ChallengeEndpointFilter =
                new OAuth2ChallengeEndpointFilter(resolvedChallengeService, this.challengeEndpointUri);

        if (!this.authorizedGrantTypes.isEmpty()) {
            oauth2ChallengeEndpointFilter.setAuthorizedGrantTypes(this.authorizedGrantTypes);
        }
        if (this.errorResponseHandler != null) {
            oauth2ChallengeEndpointFilter.setErrorResponseHandler(this.errorResponseHandler);
        }
        if (this.challengeResponseHandler != null) {
            oauth2ChallengeEndpointFilter.setChallengeResponseHandler(this.challengeResponseHandler);
        }

        http.addFilterAfter((OAuth2ChallengeEndpointFilter) postProcess.apply(oauth2ChallengeEndpointFilter),
                AuthorizationFilter.class);
    }

    private ChallengeService getChallengeService(HttpSecurity http) {
        if (this.challengeService != null) {
            return this.challengeService;
        }
        return EulerOAuth2ConfigurerUtils.getChallengeService(http);
    }
}
