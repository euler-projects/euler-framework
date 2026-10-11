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
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;

/**
 * Configurer for the <b>challenge endpoint</b> ({@code POST /oauth2/challenge} by default), which issues
 * the fresh challenges clients include in their attestation, as defined in Section 6.3 of
 * <a href="https://www.ietf.org/archive/id/draft-ietf-oauth-attestation-based-client-auth-11.html">
 * draft-ietf-oauth-attestation-based-client-auth-11</a>.
 *
 * @see ChallengeEndpointFilter
 * @see EulerOAuth2AuthorizationServerConfigurer#challengeEndpoint(org.springframework.security.config.Customizer)
 */
public class ChallengeEndpointConfigurer extends AbstractEulerOAuth2Configurer {

    private String endpointUri = EulerOAuth2AuthorizationServerConfigurer.DEFAULT_CHALLENGE_ENDPOINT_URI;

    private ChallengeEndpointFilter challengeFilter;
    private RequestMatcher requestMatcher;

    ChallengeEndpointConfigurer(ObjectPostProcessor<Object> objectPostProcessor) {
        super(objectPostProcessor);
    }

    /**
     * Set the URI for the challenge endpoint. Defaults to {@code /oauth2/challenge}.
     *
     * @param endpointUri the challenge endpoint URI
     * @return this configurer for chaining
     */
    public ChallengeEndpointConfigurer endpointUri(String endpointUri) {
        Assert.hasText(endpointUri, "endpointUri must not be empty");
        this.endpointUri = endpointUri;
        return this;
    }

    @Override
    void init(HttpSecurity httpSecurity) {
        ChallengeService challengeService = EulerOAuth2ConfigurerUtils.getChallengeService(httpSecurity);
        this.challengeFilter = new ChallengeEndpointFilter(challengeService, this.endpointUri);
        this.requestMatcher = this.challengeFilter.getRequestMatcher();
    }

    @Override
    void configure(HttpSecurity httpSecurity) {
        httpSecurity.addFilterBefore(postProcess(this.challengeFilter), AbstractPreAuthenticatedProcessingFilter.class);
    }

    @Override
    RequestMatcher getRequestMatcher() {
        return this.requestMatcher;
    }
}
