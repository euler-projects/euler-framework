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
package org.eulerframework.security.config.annotation.web.configurers.oauth2.server.authorization;

import org.eulerframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationChallengeAuthenticationProvider;
import org.eulerframework.security.oauth2.server.authorization.web.OAuth2AuthorizationChallengeEndpointFilter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2ConfigurerUtilsAccessor;
import org.springframework.security.oauth2.server.authorization.web.OAuth2ClientAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;

/**
 * Configurer for the <b>authorization challenge endpoint</b> ({@code POST /authorize-challenge} by
 * default) of <a href="https://www.ietf.org/archive/id/draft-ietf-oauth-first-party-apps-04.txt">
 * draft-ietf-oauth-first-party-apps-04</a>, which exchanges an {@code otp_ticket} + {@code otp} user
 * authentication for an {@code authorization_code} redeemed at the standard token endpoint.
 * <p>
 * Like Spring's endpoint configurers it owns only its own endpoint: {@link #init} registers the
 * code-minting {@link OAuth2AuthorizationChallengeAuthenticationProvider} into the shared
 * {@link AuthenticationManager}, and {@link #configure} installs the
 * {@link OAuth2AuthorizationChallengeEndpointFilter}. Client authentication for this endpoint is a
 * separate concern, configured through
 * {@link EulerOAuth2AuthorizationServerConfigurer#clientAuthentication(org.springframework.security.config.Customizer)}
 * (a peer {@link EulerOAuth2ClientAuthenticationConfigurer} bound to the same matcher), mirroring how
 * Spring configures client authentication for its token endpoint.
 * <p>
 * This configurer is created on demand when the caller invokes
 * {@link EulerOAuth2AuthorizationServerConfigurer#authorizationChallengeEndpoint(org.springframework.security.config.Customizer)};
 * omitting that call leaves the endpoint unregistered, so the decision to enable it stays with the caller.
 *
 * @see OAuth2AuthorizationChallengeEndpointFilter
 * @see EulerOAuth2ClientAuthenticationConfigurer
 */
public class AuthorizationChallengeEndpointConfigurer extends AbstractEulerOAuth2Configurer {

    private String endpointUri = EulerOAuth2AuthorizationServerConfigurer.DEFAULT_AUTHORIZATION_CHALLENGE_ENDPOINT_URI;
    private AuthenticationManager userAuthenticationManager;

    private RequestMatcher requestMatcher;

    AuthorizationChallengeEndpointConfigurer(ObjectPostProcessor<Object> objectPostProcessor) {
        super(objectPostProcessor);
    }

    /**
     * Set the URI for the authorization challenge endpoint. Defaults to {@code /authorize-challenge}.
     *
     * @param endpointUri the endpoint URI
     * @return this configurer for chaining
     */
    public AuthorizationChallengeEndpointConfigurer endpointUri(String endpointUri) {
        Assert.hasText(endpointUri, "endpointUri must not be empty");
        this.endpointUri = endpointUri;
        return this;
    }

    /**
     * Set the {@link AuthenticationManager} carrying the one-time-password authentication provider, used
     * to authenticate the submitted OTP (the same manager the {@code otp} grant provider uses).
     *
     * @param userAuthenticationManager the user authentication manager
     * @return this configurer for chaining
     */
    public AuthorizationChallengeEndpointConfigurer userAuthenticationManager(
            AuthenticationManager userAuthenticationManager) {
        Assert.notNull(userAuthenticationManager, "userAuthenticationManager must not be null");
        this.userAuthenticationManager = userAuthenticationManager;
        return this;
    }

    @Override
    void init(HttpSecurity httpSecurity) {
        Assert.notNull(this.userAuthenticationManager, "userAuthenticationManager must be configured");

        this.requestMatcher = PathPatternRequestMatcher.pathPattern(HttpMethod.POST, this.endpointUri);

        // Register the code-minting provider into the shared AuthenticationManager, mirroring how Spring
        // registers its token endpoint grant providers, so the endpoint filter consumes it through the
        // manager rather than invoking a provider directly.
        OAuth2AuthorizationChallengeAuthenticationProvider authorizationChallengeProvider =
                new OAuth2AuthorizationChallengeAuthenticationProvider(
                        this.userAuthenticationManager,
                        OAuth2ConfigurerUtilsAccessor.getAuthorizationService(httpSecurity));
        httpSecurity.authenticationProvider(postProcess(authorizationChallengeProvider));
    }

    @Override
    void configure(HttpSecurity httpSecurity) {
        // Runs after the client authentication filter (a peer EulerOAuth2ClientAuthenticationConfigurer
        // bound to this endpoint's matcher), which leaves the authenticated client on the SecurityContext
        // for the challenge converter to read.
        OAuth2AuthorizationChallengeEndpointFilter authorizationChallengeFilter =
                new OAuth2AuthorizationChallengeEndpointFilter(
                        httpSecurity.getSharedObject(AuthenticationManager.class), this.requestMatcher);
        httpSecurity.addFilterAfter(postProcess(authorizationChallengeFilter), OAuth2ClientAuthenticationFilter.class);
    }

    @Override
    RequestMatcher getRequestMatcher() {
        return this.requestMatcher;
    }
}
