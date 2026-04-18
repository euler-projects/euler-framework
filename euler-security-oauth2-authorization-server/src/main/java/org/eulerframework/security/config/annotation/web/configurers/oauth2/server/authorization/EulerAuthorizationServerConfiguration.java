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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eulerframework.security.authentication.ChallengeService;
import org.eulerframework.security.authentication.NonceService;
import org.eulerframework.security.authentication.apple.AppAttestRegistrationService;
import org.eulerframework.security.authentication.apple.AppleAppAttestValidationService;
import org.eulerframework.security.core.userdetails.EulerAppleAppAttestUserDetailsService;
import org.eulerframework.security.oauth2.core.EulerClientAuthenticationMethod;
import org.eulerframework.security.oauth2.server.authorization.authentication.EulerOAuth2ClientAttestationAuthenticationProvider;
import org.eulerframework.security.oauth2.server.authorization.authentication.EulerOAuth2ClientAttestationVerifier;
import org.eulerframework.security.oauth2.server.authorization.authentication.OAuth2AppleAppAttestAssertionAuthenticationProvider;
import org.eulerframework.security.oauth2.server.authorization.authentication.OAuth2PasswordAuthenticationProvider;
import org.eulerframework.security.oauth2.server.authorization.authentication.OAuth2WechatAuthorizationCodeAuthenticationProvider;
import org.eulerframework.security.oauth2.server.authorization.converter.EulerOAuth2ClientRegistrationRegisteredClientConverter;
import org.eulerframework.security.oauth2.server.authorization.converter.EulerRegisteredClientOAuth2ClientRegistrationConverter;
import org.eulerframework.security.oauth2.server.authorization.oidc.authentication.UserDetailsOidcUserInfoMapper;
import org.eulerframework.security.oauth2.server.authorization.web.EulerOAuth2AttestationBasedClientAuthenticationFilter;
import org.eulerframework.security.oauth2.server.authorization.web.authentication.EulerOAuth2ClientAttestationAuthenticationConverter;
import org.eulerframework.security.oauth2.server.authorization.web.authentication.OAuth2AppleAppAttestAssertionAuthenticationConverter;
import org.eulerframework.security.oauth2.server.authorization.web.authentication.OAuth2PasswordAuthenticationConverter;
import org.eulerframework.security.oauth2.server.authorization.web.authentication.OAuth2WechatAuthorizationCodeAuthenticationConverter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2ConfigurerUtilsAccessor;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientRegistrationAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.web.OAuth2ClientAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.eulerframework.security.web.authentication.ChallengeEndpointFilter;

import java.util.List;

public class EulerAuthorizationServerConfiguration {
    public static void configClientRegistrationEndpoint(HttpSecurity http, AuthenticationConfiguration authenticationConfiguration) {
        http.oauth2AuthorizationServer(oauth2AuthorizationServer -> oauth2AuthorizationServer
                .clientRegistrationEndpoint(configurer -> configurer
                        .openRegistrationAllowed(true)
                        .authenticationProviders(authenticationProviders -> {
                            for (AuthenticationProvider authenticationProvider : authenticationProviders) {
                                if (authenticationProvider instanceof OAuth2ClientRegistrationAuthenticationProvider oauth2ClientRegistrationAuthenticationProvider) {
                                    oauth2ClientRegistrationAuthenticationProvider.setRegisteredClientConverter(new EulerOAuth2ClientRegistrationRegisteredClientConverter());
                                    oauth2ClientRegistrationAuthenticationProvider.setClientRegistrationConverter(new EulerRegisteredClientOAuth2ClientRegistrationConverter());
                                }
                            }
                        })));
//        http.oauth2AuthorizationServer(oauth2AuthorizationServer -> oauth2AuthorizationServer
//                .clientRegistrationEndpoint(configurer ->
//                        http.authorizeHttpRequests((authorize) -> authorize
//                                .requestMatchers(ConfigurerAccessor.getDeferredRequestMatcher(configurer))
//                                .permitAll())));
    }


    public static void configPasswordAuthentication(HttpSecurity http, AuthenticationConfiguration authenticationConfiguration) {
        http.oauth2AuthorizationServer(oauth2AuthorizationServer -> oauth2AuthorizationServer
                .tokenEndpoint(configurer -> configurer
                        .authenticationProvider(getOAuth2PasswordAuthenticationProvider(http, authenticationConfiguration))
                        .accessTokenRequestConverter(getOAuth2PasswordAuthenticationConverter())));
    }

    private static OAuth2PasswordAuthenticationProvider getOAuth2PasswordAuthenticationProvider(HttpSecurity http, AuthenticationConfiguration authenticationConfiguration) {
        try {
            return new OAuth2PasswordAuthenticationProvider(
                    authenticationConfiguration.getAuthenticationManager(),
                    OAuth2ConfigurerUtilsAccessor.getAuthorizationService(http),
                    OAuth2ConfigurerUtilsAccessor.getTokenGenerator(http)
            );
        } catch (Exception e) {
            throw ExceptionUtils.asRuntimeException(e);
        }
    }

    private static OAuth2PasswordAuthenticationConverter getOAuth2PasswordAuthenticationConverter() {
        return new OAuth2PasswordAuthenticationConverter();
    }

    public static void configWechatAuthentication(HttpSecurity http, AuthenticationConfiguration authenticationConfiguration) {
        http.oauth2AuthorizationServer(oauth2AuthorizationServer -> oauth2AuthorizationServer
                .tokenEndpoint(configurer -> configurer
                        .authenticationProvider(getOAuth2WechatAuthenticationProvider(http, authenticationConfiguration))
                        .accessTokenRequestConverter(getOAuth2WechatAuthenticationConverter())));
    }

    private static OAuth2WechatAuthorizationCodeAuthenticationProvider getOAuth2WechatAuthenticationProvider(HttpSecurity http, AuthenticationConfiguration authenticationConfiguration) {
        try {
            return new OAuth2WechatAuthorizationCodeAuthenticationProvider(
                    authenticationConfiguration.getAuthenticationManager(),
                    OAuth2ConfigurerUtilsAccessor.getAuthorizationService(http),
                    OAuth2ConfigurerUtilsAccessor.getTokenGenerator(http)
            );
        } catch (Exception e) {
            throw ExceptionUtils.asRuntimeException(e);
        }
    }

    private static OAuth2WechatAuthorizationCodeAuthenticationConverter getOAuth2WechatAuthenticationConverter() {
        return new OAuth2WechatAuthorizationCodeAuthenticationConverter();
    }

    /**
     * Configure OAuth 2.0 Attestation-Based Client Authentication for the authorization server.
     * <p>
     * This method implements the server-side support for
     * <a href="https://www.ietf.org/archive/id/draft-ietf-oauth-attestation-based-client-auth-08.html">
     * draft-ietf-oauth-attestation-based-client-auth-08</a>, including:
     * <ul>
     *   <li><b>Challenge endpoint</b> ({@code POST /oauth2/challenge}) — generates fresh
     *       challenges for clients to include in the Client Attestation PoP JWT
     *       (Section 7 of the draft). The {@link ChallengeService} is shared with other
     *       security filter chains (e.g., App Attest registration) via the ApplicationContext.
     *       Uses a shared {@link ChallengeEndpointFilter} instance.</li>
     *   <li>{@link EulerOAuth2AttestationBasedClientAuthenticationFilter} — runs after
     *       {@code OAuth2ClientAuthenticationFilter} to verify attestation headers
     *       ({@code OAuth-Client-Attestation} and/or {@code OAuth-Client-Attestation-PoP}).</li>
     *   <li>{@link EulerOAuth2ClientAttestationAuthenticationProvider} — verifies the
     *       {@code attest_jwt_client_auth} client authentication method (Section 6.3 / 13.4).</li>
     *   <li>When {@code appAttestEnabled} is {@code true}:
     *       {@link OAuth2AppleAppAttestAssertionAuthenticationProvider} — a thin grant type
     *       provider for anonymous user resolution and token issuance via App Attest.</li>
     *   <li><b>Authorization Server Metadata</b> — advertises {@code challenge_endpoint},
     *       {@code attest_jwt_client_auth} method, and supported signing algorithms
     *       (Section 9 of the draft).</li>
     * </ul>
     *
     * @param http                        the {@link HttpSecurity} to configure
     * @param authenticationConfiguration the authentication configuration
     * @return a {@link RequestMatcher} for the challenge endpoint; the caller should include
     * this in the security filter chain's security matcher via
     * {@link org.springframework.security.web.util.matcher.OrRequestMatcher}
     */
    public static RequestMatcher configAttestationBasedClientAuthentication(
            HttpSecurity http, AuthenticationConfiguration authenticationConfiguration) {
        AuthorizationServerSettings authorizationServerSettings = OAuth2ConfigurerUtilsAccessor
                .getAuthorizationServerSettings(http);
        String tokenEndpointUri = authorizationServerSettings.getTokenEndpoint();
        RequestMatcher tokenEndpointMatcher = PathPatternRequestMatcher
                .pathPattern(HttpMethod.POST, tokenEndpointUri);

        ChallengeService challengeService = EulerOAuth2ConfigurerUtils.getChallengeService(http);
        NonceService nonceService = EulerOAuth2ConfigurerUtils.getNonceService(http);
        EulerOAuth2ClientAttestationVerifier oauth2ClientAttestationVerifier =
                new EulerOAuth2ClientAttestationVerifier(challengeService, nonceService);

        RegisteredClientRepository registeredClientRepository =
                OAuth2ConfigurerUtilsAccessor.getRegisteredClientRepository(http);

        EulerOAuth2ClientAttestationAuthenticationConverter attestConverter =
                new EulerOAuth2ClientAttestationAuthenticationConverter();
        EulerOAuth2ClientAttestationAuthenticationProvider attestProvider =
                new EulerOAuth2ClientAttestationAuthenticationProvider(
                        registeredClientRepository, oauth2ClientAttestationVerifier);

        // Config Attestation Based Client Authentication as standard Client Authentication.
        http.oauth2AuthorizationServer(oauth2 -> oauth2
                .clientAuthentication(clientAuth -> clientAuth
                        .authenticationConverter(attestConverter)
                        .authenticationProvider(attestProvider)));

        // Config Attestation Based Client Authentication as an additional security signal.
        EulerOAuth2AttestationBasedClientAuthenticationFilter attestationFilter = new EulerOAuth2AttestationBasedClientAuthenticationFilter(
                tokenEndpointMatcher, attestConverter, attestProvider);
        http.addFilterAfter(attestationFilter, OAuth2ClientAuthenticationFilter.class);


        // Register the challenge endpoint (draft Section 7)
        String challengeEndpointPath = "/oauth2/challenge";
        ChallengeEndpointFilter challengeFilter =
                new ChallengeEndpointFilter(challengeService, challengeEndpointPath);
        RequestMatcher challengeEndpointMatcher = challengeFilter.getRequestMatcher();

        http.addFilterBefore(challengeFilter, OAuth2ClientAuthenticationFilter.class);
        http.csrf(csrf -> csrf.ignoringRequestMatchers(challengeEndpointMatcher));
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers(challengeEndpointMatcher).permitAll());

        //  Add attestation metadata to OIDC provider configuration and AS metadata endpoints
        //    (draft-ietf-oauth-attestation-based-client-auth-08 Section 9)
        String issuer = authorizationServerSettings.getIssuer();
        String challengeEndpointUri = (issuer != null ? issuer : "") + challengeEndpointPath;
        List<String> supportedSigningAlgs = List.of("ES256");

        http.oauth2AuthorizationServer(oauth2 -> oauth2
                .oidc(oidc -> oidc
                        .providerConfigurationEndpoint(config -> config
                                .providerConfigurationCustomizer(builder -> {
                                    builder.tokenEndpointAuthenticationMethod(
                                            EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH.getValue());
                                    builder.claim("challenge_endpoint", challengeEndpointUri);
                                    builder.claim("client_attestation_signing_alg_values_supported",
                                            supportedSigningAlgs);
                                    builder.claim("client_attestation_pop_signing_alg_values_supported",
                                            supportedSigningAlgs);
                                })
                        )
                )
                .authorizationServerMetadataEndpoint(metadata -> metadata
                        .authorizationServerMetadataCustomizer(builder -> {
                            builder.tokenEndpointAuthenticationMethod(
                                    EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH.getValue());
                            builder.claim("challenge_endpoint", challengeEndpointUri);
                            builder.claim("client_attestation_signing_alg_values_supported",
                                    supportedSigningAlgs);
                            builder.claim("client_attestation_pop_signing_alg_values_supported",
                                    supportedSigningAlgs);
                        })
                )
        );

        // Apple App Attest support for Attestation Based Client Authentication
        AppAttestRegistrationService registrationService =
                EulerOAuth2ConfigurerUtils.getAppAttestRegistrationServiceIfAvailable(http);
        if (registrationService != null) {
            oauth2ClientAttestationVerifier.setAppAttestRegistrationService(registrationService);
        }

        AppleAppAttestValidationService appleAppAttestValidationService =
                EulerOAuth2ConfigurerUtils.getAppleAppAttestValidationServiceIfAvailable(http);
        if (appleAppAttestValidationService != null) {
            attestProvider.setAppleAppAttestValidationService(appleAppAttestValidationService);
        }

        EulerAppleAppAttestUserDetailsService userDetailsService =
                EulerOAuth2ConfigurerUtils.getAppleAppAttestUserDetailsServiceIfAvailable(http);
        if (userDetailsService != null) {
            OAuth2AppleAppAttestAssertionAuthenticationProvider grantProvider =
                    new OAuth2AppleAppAttestAssertionAuthenticationProvider(
                            userDetailsService,
                            OAuth2ConfigurerUtilsAccessor.getAuthorizationService(http),
                            OAuth2ConfigurerUtilsAccessor.getTokenGenerator(http));

            http.oauth2AuthorizationServer(oauth2 -> oauth2
                    .tokenEndpoint(configurer -> configurer
                            .authenticationProvider(grantProvider)
                            .accessTokenRequestConverter(new OAuth2AppleAppAttestAssertionAuthenticationConverter())));
        }

        return challengeEndpointMatcher;
    }

    /**
     * Enable extended claims support for the OIDC {@code userinfo} endpoint.
     * <p>
     * To provide the actual claim values, an {@link OAuth2TokenCustomizer} bean must be defined
     * to enrich token claims at issuance time
     * (automatically injected via {@link JwtGenerator#setJwtCustomizer(OAuth2TokenCustomizer)}).
     */
    public static void enableExtendedClaims(HttpSecurity http) {
        http.oauth2AuthorizationServer(oauth2AuthorizationServer -> oauth2AuthorizationServer
                .oidc(oidc -> oidc
                        .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint
                                .userInfoMapper(new UserDetailsOidcUserInfoMapper()))));
    }
}
