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
import org.eulerframework.security.authentication.device.DeviceAttestationRegistrationService;
import org.eulerframework.security.authentication.apple.AppleAppAttestValidationService;
import org.eulerframework.security.oauth2.server.authorization.authentication.EulerOAuth2ClientAttestationAuthenticationProvider;
import org.eulerframework.security.oauth2.server.authorization.web.EulerOAuth2AttestationBasedClientAuthenticationFilter;
import org.eulerframework.security.oauth2.server.authorization.web.authentication.EulerOAuth2ClientAttestationAuthenticationConverter;
import org.eulerframework.security.web.authentication.ChallengeEndpointFilter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2ConfigurerUtilsAccessor;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.web.OAuth2ClientAuthenticationFilter;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * An {@link AbstractHttpConfigurer} for OAuth 2.0 Attestation-Based Client Authentication
 * as defined in
 * <a href="https://www.ietf.org/archive/id/draft-ietf-oauth-attestation-based-client-auth-08.html">
 * draft-ietf-oauth-attestation-based-client-auth-08</a>.
 * <p>
 * This configurer registers the following components into the authorization server
 * security filter chain:
 * <ul>
 *   <li><b>Challenge endpoint</b> ({@code POST /oauth2/challenge} by default) — generates
 *       fresh challenges for clients to include in the Client Attestation PoP JWT
 *       (Section 7 of the draft).</li>
 *   <li>{@link EulerOAuth2AttestationBasedClientAuthenticationFilter} — runs after
 *       {@code OAuth2ClientAuthenticationFilter} to verify attestation headers.</li>
 *   <li>{@link EulerOAuth2ClientAttestationAuthenticationProvider} — verifies the
 *       {@code attest_jwt_client_auth} client authentication method.</li>
 *   <li><b>Authorization Server Metadata</b> — advertises {@code challenge_endpoint},
 *       {@code attest_jwt_client_auth} method, and supported signing algorithms.</li>
 *   <li>Conditional <b>Apple App Attest</b> support — when the required beans are
 *       available, registers the App Attest assertion grant type provider.</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>
 * EulerOAuth2ClientAttestationConfigurer attestationConfigurer =
 *         new EulerOAuth2ClientAttestationConfigurer();
 * http.with(attestationConfigurer, Customizer.withDefaults());
 * http.securityMatcher(new OrRequestMatcher(
 *         authorizationServerConfigurer.getEndpointsMatcher(),
 *         attestationConfigurer.getEndpointsMatcher()));
 * </pre>
 *
 * @see EulerOAuth2AttestationBasedClientAuthenticationFilter
 * @see EulerOAuth2ClientAttestationAuthenticationProvider
 * @see ChallengeEndpointFilter
 */
public class EulerOAuth2AuthorizationServerConfigurer
        extends AbstractHttpConfigurer<EulerOAuth2AuthorizationServerConfigurer, HttpSecurity> {

    public static final String DEFAULT_CHALLENGE_ENDPOINT_URI = "/oauth2/challenge";

    private String challengeEndpointUri = DEFAULT_CHALLENGE_ENDPOINT_URI;

    private RequestMatcher endpointsMatcher;

    // Filters created in init(), installed in configure()
    private ChallengeEndpointFilter challengeFilter;
    private EulerOAuth2AttestationBasedClientAuthenticationFilter attestationFilter;
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
        AuthorizationServerSettings authorizationServerSettings = OAuth2ConfigurerUtilsAccessor
                .getAuthorizationServerSettings(http);
        String tokenEndpointUri = authorizationServerSettings.getTokenEndpoint();
        RequestMatcher tokenEndpointMatcher = PathPatternRequestMatcher
                .pathPattern(HttpMethod.POST, tokenEndpointUri);

        EulerOAuth2ClientAttestationAuthenticationConverter attestConverter = EulerOAuth2ConfigurerUtils
                .getEulerOAuth2ClientAttestationAuthenticationConverter(http);
        EulerOAuth2ClientAttestationAuthenticationProvider attestProvider = EulerOAuth2ConfigurerUtils
                .getEulerOAuth2ClientAttestationAuthenticationProvider(http);

        // Create post-auth filter (installed in configure())
        this.attestationFilter = new EulerOAuth2AttestationBasedClientAuthenticationFilter(
                tokenEndpointMatcher, attestConverter, attestProvider);

        // Create challenge endpoint filter (draft Section 7)
        ChallengeService challengeService = EulerOAuth2ConfigurerUtils.getChallengeService(http);
        this.challengeFilter = new ChallengeEndpointFilter(challengeService, this.challengeEndpointUri);
        this.challengeEndpointMatcher = this.challengeFilter.getRequestMatcher();
        this.endpointsMatcher = this.challengeEndpointMatcher;

        // Exempt challenge endpoint from CSRF protection
        http.csrf(csrf -> csrf.ignoringRequestMatchers(this.challengeEndpointMatcher));

        // Apple App Attest support for Attestation Based Client Authentication
        DeviceAttestationRegistrationService registrationService =
                EulerOAuth2ConfigurerUtils.getDeviceAttestRegistrationServiceIfAvailable(http);
        if (registrationService != null) {
            attestProvider.getOauth2ClientAttestationVerifier().setDeviceAttestRegistrationService(registrationService);
        }

        AppleAppAttestValidationService appleAppAttestValidationService =
                EulerOAuth2ConfigurerUtils.getAppleAppAttestValidationServiceIfAvailable(http);
        if (appleAppAttestValidationService != null) {
            attestProvider.setAppleAppAttestValidationService(appleAppAttestValidationService);
        }
    }

    @Override
    public void configure(HttpSecurity http) {
        http.addFilterBefore(postProcess(this.challengeFilter), AbstractPreAuthenticatedProcessingFilter.class);
        http.addFilterAfter(postProcess(this.attestationFilter), OAuth2ClientAuthenticationFilter.class);
    }
}
