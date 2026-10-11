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

import org.springframework.security.config.Customizer;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * An {@link AbstractHttpConfigurer} that registers this framework's authorization server endpoints. It
 * mirrors the shape of Spring's {@code OAuth2AuthorizationServerConfigurer}: one sub-configurer per
 * endpoint, held in a map, whose {@code init}/{@code configure} this delegates to and whose request
 * matchers it aggregates into {@link #getEndpointsMatcher()} and exempts from CSRF.
 * <p>
 * The endpoints are:
 * <ul>
 *   <li>the <b>challenge endpoint</b> ({@code POST /oauth2/challenge}) via {@link ChallengeEndpointConfigurer},
 *       always registered; and</li>
 *   <li>the <b>authorization challenge endpoint</b> ({@code POST /authorize-challenge}) via
 *       {@link AuthorizationChallengeEndpointConfigurer}, registered only when the caller invokes
 *       {@link #authorizationChallengeEndpoint(Customizer)} &mdash; so the decision to enable it stays
 *       with the caller rather than a flag inside this configurer.</li>
 * </ul>
 *
 * @see ChallengeEndpointConfigurer
 * @see AuthorizationChallengeEndpointConfigurer
 */
public class EulerOAuth2AuthorizationServerConfigurer
        extends AbstractHttpConfigurer<EulerOAuth2AuthorizationServerConfigurer, HttpSecurity> {

    public static final String DEFAULT_CHALLENGE_ENDPOINT_URI = "/oauth2/challenge";
    public static final String DEFAULT_AUTHORIZATION_CHALLENGE_ENDPOINT_URI = "/authorize-challenge";

    private final Map<Class<? extends AbstractEulerOAuth2Configurer>, AbstractEulerOAuth2Configurer> configurers =
            createConfigurers();

    private RequestMatcher endpointsMatcher;

    /**
     * Configure the challenge endpoint ({@code POST /oauth2/challenge}), which issues the fresh challenges
     * clients include in their attestation.
     *
     * @param challengeEndpointCustomizer the {@link Customizer} providing access to the sub-configurer
     * @return this configurer for chaining
     */
    public EulerOAuth2AuthorizationServerConfigurer challengeEndpoint(
            Customizer<ChallengeEndpointConfigurer> challengeEndpointCustomizer) {
        challengeEndpointCustomizer.customize(getConfigurer(ChallengeEndpointConfigurer.class));
        return this;
    }

    /**
     * Enable and configure the authorization challenge endpoint ({@code POST /authorize-challenge}), which
     * exchanges an {@code otp_ticket} + {@code otp} user authentication for an {@code authorization_code}.
     * Invoking this registers the endpoint's sub-configurer; not invoking it leaves the endpoint
     * unregistered, so enablement is the caller's decision.
     *
     * @param authorizationChallengeEndpointCustomizer the {@link Customizer} providing access to the sub-configurer
     * @return this configurer for chaining
     */
    public EulerOAuth2AuthorizationServerConfigurer authorizationChallengeEndpoint(
            Customizer<AuthorizationChallengeEndpointConfigurer> authorizationChallengeEndpointCustomizer) {
        AuthorizationChallengeEndpointConfigurer authorizationChallengeEndpointConfigurer = getOrCreateConfigurer(
                AuthorizationChallengeEndpointConfigurer.class, AuthorizationChallengeEndpointConfigurer::new);
        authorizationChallengeEndpointCustomizer.customize(authorizationChallengeEndpointConfigurer);
        return this;
    }

    /**
     * Configure OAuth 2.0 Client Authentication, mirroring Spring's
     * {@code OAuth2AuthorizationServerConfigurer#clientAuthentication}. Unlike Spring's, the
     * {@link EulerOAuth2ClientAuthenticationConfigurer} takes a caller-supplied request matcher, so the
     * same client authentication chain can guard an endpoint of this framework's own (e.g. the
     * authorization challenge endpoint). Invoking this registers the client authentication sub-configurer.
     *
     * @param clientAuthenticationCustomizer the {@link Customizer} providing access to the sub-configurer
     * @return this configurer for chaining
     */
    public EulerOAuth2AuthorizationServerConfigurer clientAuthentication(
            Customizer<EulerOAuth2ClientAuthenticationConfigurer> clientAuthenticationCustomizer) {
        EulerOAuth2ClientAuthenticationConfigurer clientAuthenticationConfigurer = getOrCreateConfigurer(
                EulerOAuth2ClientAuthenticationConfigurer.class, EulerOAuth2ClientAuthenticationConfigurer::new);
        clientAuthenticationCustomizer.customize(clientAuthenticationConfigurer);
        return this;
    }

    /**
     * Returns a {@link RequestMatcher} for the endpoints managed by this configurer. This can be used to
     * configure the security filter chain's security matcher.
     *
     * @return the endpoints request matcher
     */
    public RequestMatcher getEndpointsMatcher() {
        // Return a lazy matcher: endpointsMatcher is constructed in init(HttpSecurity), after the caller
        // has had its chance to register the optional authorization challenge endpoint.
        return (request) -> this.endpointsMatcher != null && this.endpointsMatcher.matches(request);
    }

    @Override
    public void init(HttpSecurity http) {
        List<RequestMatcher> requestMatchers = new ArrayList<>();
        for (AbstractEulerOAuth2Configurer configurer : this.configurers.values()) {
            configurer.init(http);
            requestMatchers.add(configurer.getRequestMatcher());
        }
        this.endpointsMatcher = new OrRequestMatcher(requestMatchers);

        // These endpoints are invoked by native apps with no browser session, so exempt them from CSRF.
        http.csrf(csrf -> csrf.ignoringRequestMatchers(this.endpointsMatcher));
    }

    @Override
    public void configure(HttpSecurity http) {
        for (AbstractEulerOAuth2Configurer configurer : this.configurers.values()) {
            configurer.configure(http);
        }
    }

    private Map<Class<? extends AbstractEulerOAuth2Configurer>, AbstractEulerOAuth2Configurer> createConfigurers() {
        Map<Class<? extends AbstractEulerOAuth2Configurer>, AbstractEulerOAuth2Configurer> configurers =
                new LinkedHashMap<>();
        configurers.put(ChallengeEndpointConfigurer.class, new ChallengeEndpointConfigurer(this::postProcess));
        return configurers;
    }

    @SuppressWarnings("unchecked")
    private <T extends AbstractEulerOAuth2Configurer> T getConfigurer(Class<T> type) {
        return (T) this.configurers.get(type);
    }

    @SuppressWarnings("unchecked")
    private <T extends AbstractEulerOAuth2Configurer> T getOrCreateConfigurer(
            Class<T> type, Function<ObjectPostProcessor<Object>, T> factory) {
        T configurer = (T) this.configurers.get(type);
        if (configurer == null) {
            configurer = factory.apply(this::postProcess);
            this.configurers.put(type, configurer);
        }
        return configurer;
    }
}
