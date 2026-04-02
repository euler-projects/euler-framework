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

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.authorization.web.OAuth2ClientAuthenticationFilter;
import org.springframework.security.oauth2.server.authorization.web.authentication.ClientSecretBasicAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.ClientSecretPostAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.JwtClientAssertionAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.PublicClientAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.X509ClientCertificateAuthenticationConverter;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.DelegatingAuthenticationConverter;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link AbstractHttpConfigurer} for Euler OAuth 2.0 Authorization Server extensions.
 * <p>
 * This configurer acts as an umbrella for all Euler-specific OAuth 2.0 endpoints that extend
 * the standard Spring Authorization Server. It manages cross-cutting concerns such as
 * client authentication filter registration and CSRF exemption for all enabled endpoints.
 * <p>
 * Each endpoint is managed by a dedicated sub-configurer. Currently supported endpoints:
 * <ul>
 * <li><b>Challenge Endpoint</b> ({@link OAuth2ChallengeEndpointConfigurer}) - generates one-time
 *     challenges for authentication flows that require a challenge-response mechanism
 *     (e.g., Apple App Attest, WebAuthn)</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 *
 * <pre>
 * EulerOAuth2AuthorizationServerConfigurer eulerConfigurer =
 *     new EulerOAuth2AuthorizationServerConfigurer();
 *
 * http.with(eulerConfigurer, euler -&gt; euler
 *     .challengeEndpoint(challenge -&gt; challenge
 *         .challengeService(challengeService)
 *         .authorizedGrantTypes(MyGrantType.CUSTOM_ATTEST)
 *     )
 * );
 * </pre>
 *
 * @see OAuth2ChallengeEndpointConfigurer
 */
public class EulerOAuth2AuthorizationServerConfigurer
        extends AbstractHttpConfigurer<EulerOAuth2AuthorizationServerConfigurer, HttpSecurity> {

    private OAuth2ChallengeEndpointConfigurer challengeEndpointConfigurer;
    private RequestMatcher endpointsMatcher;

    /**
     * Configures the OAuth 2.0 Challenge Endpoint.
     * <p>
     * Calling this method enables the challenge endpoint. If not called, the challenge
     * endpoint will not be registered.
     *
     * @param challengeEndpointCustomizer the {@link Customizer} providing access to the
     *                                    {@link OAuth2ChallengeEndpointConfigurer}
     * @return the {@link EulerOAuth2AuthorizationServerConfigurer} for further configuration
     */
    public EulerOAuth2AuthorizationServerConfigurer challengeEndpoint(
            Customizer<OAuth2ChallengeEndpointConfigurer> challengeEndpointCustomizer) {
        if (this.challengeEndpointConfigurer == null) {
            this.challengeEndpointConfigurer = new OAuth2ChallengeEndpointConfigurer();
        }
        challengeEndpointCustomizer.customize(this.challengeEndpointConfigurer);
        return this;
    }

    /**
     * Returns a {@link RequestMatcher} for all enabled Euler authorization server endpoints.
     * <p>
     * This method returns a delegating matcher whose actual matching logic is populated
     * during the {@link #init(HttpSecurity)} phase. This allows it to be called early
     * (e.g., for {@code securityMatcher} setup) before sub-configurers are registered.
     *
     * @return the composite request matcher for all enabled endpoints
     */
    public RequestMatcher getEndpointsMatcher() {
        return (request) -> this.endpointsMatcher.matches(request);
    }

    @Override
    public void init(HttpSecurity http) {
        List<RequestMatcher> matchers = new ArrayList<>();

        if (this.challengeEndpointConfigurer != null) {
            this.challengeEndpointConfigurer.init(http);
            matchers.add(this.challengeEndpointConfigurer.getRequestMatcher());
        }
        // Future: add more endpoint matchers here

        this.endpointsMatcher = matchers.isEmpty()
                ? (request) -> false
                : new OrRequestMatcher(matchers);

        // Ignore CSRF for enabled Euler endpoints, since OAuth2AuthorizationServerConfigurer
        // only ignores CSRF for its built-in endpoints (token, introspection, revocation, etc.)
        if (!matchers.isEmpty()) {
            http.csrf(csrf -> csrf.ignoringRequestMatchers(this.endpointsMatcher));
        }
    }

    @Override
    public void configure(HttpSecurity http) {
        // Delegate to each enabled sub-configurer to register their endpoint filters
        if (this.challengeEndpointConfigurer != null) {
            this.challengeEndpointConfigurer.configure(http, this::postProcess);
        }

        // Register a single dedicated client authentication filter for ALL enabled Euler endpoints,
        // using the endpointsMatcher already built during init().
        // The authorization server's OAuth2ClientAuthenticationFilter only matches standard
        // endpoints (token, introspection, revocation, device authorization, PAR), so without this,
        // no client authentication is performed for Euler endpoint requests, causing AuthorizationFilter
        // to reject them before they reach the endpoint filters.
        if (this.challengeEndpointConfigurer != null) {
            AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
            OAuth2ClientAuthenticationFilter clientAuthFilter = new OAuth2ClientAuthenticationFilter(
                    authenticationManager, this.endpointsMatcher);
            clientAuthFilter.setAuthenticationConverter(new DelegatingAuthenticationConverter(
                    createDefaultAuthenticationConverters()
            ));
            http.addFilterAfter(postProcess(clientAuthFilter), AbstractPreAuthenticatedProcessingFilter.class);
        }
    }

    private static List<AuthenticationConverter> createDefaultAuthenticationConverters() {
        List<AuthenticationConverter> authenticationConverters = new ArrayList<>();
        authenticationConverters.add(new JwtClientAssertionAuthenticationConverter());
        authenticationConverters.add(new ClientSecretBasicAuthenticationConverter());
        authenticationConverters.add(new ClientSecretPostAuthenticationConverter());
        authenticationConverters.add(new PublicClientAuthenticationConverter());
        authenticationConverters.add(new X509ClientCertificateAuthenticationConverter());
        return authenticationConverters;
    }
}
