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

import org.eulerframework.security.authentication.ChallengeService;
import org.eulerframework.security.web.authentication.ChallengeEndpointFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * An {@link AbstractHttpConfigurer} that registers the <b>challenge endpoint</b>
 * ({@code POST /oauth2/challenge} by default), which issues the fresh challenges clients include in
 * their attestation, as defined in Section 6.3 of
 * <a href="https://www.ietf.org/archive/id/draft-ietf-oauth-attestation-based-client-auth-11.html">
 * draft-ietf-oauth-attestation-based-client-auth-11</a>.
 * <p>
 * The attestation-based client authentication that consumes those challenges needs no configurer of
 * its own: it composes with Spring's client authentication infrastructure and is wired in
 * {@link EulerAuthorizationServerConfiguration#configClientAttestationAuthentication}.
 *
 * @see ChallengeEndpointFilter
 */
public class EulerOAuth2AuthorizationServerConfigurer
        extends AbstractHttpConfigurer<EulerOAuth2AuthorizationServerConfigurer, HttpSecurity> {

    public static final String DEFAULT_CHALLENGE_ENDPOINT_URI = "/oauth2/challenge";

    private String challengeEndpointUri = DEFAULT_CHALLENGE_ENDPOINT_URI;

    private RequestMatcher endpointsMatcher;

    // Filter created in init(), installed in configure()
    private ChallengeEndpointFilter challengeFilter;
    private RequestMatcher challengeEndpointMatcher;

    // ---- Fluent API ----

    /**
     * Set the URI for the challenge endpoint. Defaults to {@code /oauth2/challenge}.
     *
     * @param challengeEndpointUri the challenge endpoint URI
     * @return this configurer for chaining
     */
    public EulerOAuth2AuthorizationServerConfigurer challengeEndpointUri(String challengeEndpointUri) {
        this.challengeEndpointUri = challengeEndpointUri;
        return this;
    }

    /**
     * Returns a {@link RequestMatcher} for the endpoints managed by this configurer
     * (currently only the challenge endpoint). This can be used to configure the
     * security filter chain's security matcher.
     *
     * @return the endpoints request matcher
     */
    public RequestMatcher getEndpointsMatcher() {
        return (request) -> this.endpointsMatcher != null && this.endpointsMatcher.matches(request);
    }

    @Override
    public void init(HttpSecurity http) {
        // Create challenge endpoint filter (draft Section 7)
        ChallengeService challengeService = EulerOAuth2ConfigurerUtils.getChallengeService(http);
        this.challengeFilter = new ChallengeEndpointFilter(challengeService, this.challengeEndpointUri);
        this.challengeEndpointMatcher = this.challengeFilter.getRequestMatcher();
        this.endpointsMatcher = this.challengeEndpointMatcher;

        // Exempt challenge endpoint from CSRF protection
        http.csrf(csrf -> csrf.ignoringRequestMatchers(this.challengeEndpointMatcher));
    }

    @Override
    public void configure(HttpSecurity http) {
        http.addFilterBefore(postProcess(this.challengeFilter), AbstractPreAuthenticatedProcessingFilter.class);
    }
}
