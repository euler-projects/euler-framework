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
import org.eulerframework.security.oauth2.server.authorization.authentication.ClientAttestationVerifier;
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
     * Configure Apple App Attest authentication for the token endpoint.
     * <p>
     * This method registers:
     * <ul>
     *   <li>{@link EulerOAuth2AttestationBasedClientAuthenticationFilter} — runs after {@code OAuth2ClientAuthenticationFilter}
     *       to verify attestation headers (Client Attestation JWT and/or PoP).</li>
     *   <li>{@link OAuth2AppleAppAttestAssertionAuthenticationProvider} — a thin grant type
     *       provider for anonymous user resolution and token issuance.</li>
     *   <li>{@link OAuth2AppleAppAttestAssertionAuthenticationConverter} — reads the verified
     *       {@code keyId} from request attributes set by the filter.</li>
     * </ul>
     */
    public static void configAppleAppAttestAuthentication(HttpSecurity http, AuthenticationConfiguration authenticationConfiguration) {
        ChallengeService challengeService = EulerOAuth2ConfigurerUtils.getChallengeService(http);
        AppAttestRegistrationService registrationService =
                EulerOAuth2ConfigurerUtils.getAppAttestRegistrationService(http);
        AppleAppAttestValidationService validationService =
                EulerOAuth2ConfigurerUtils.getAppleAppAttestValidationService(http);
        RegisteredClientRepository registeredClientRepository =
                OAuth2ConfigurerUtilsAccessor.getRegisteredClientRepository(http);
        EulerAppleAppAttestUserDetailsService userDetailsService =
                EulerOAuth2ConfigurerUtils.getAppleAppAttestUserDetailsService(http);
        NonceService nonceService = EulerOAuth2ConfigurerUtils.getNonceService(http);

        // 1. Resolve the token endpoint RequestMatcher for the attestation filter
        AuthorizationServerSettings authorizationServerSettings = OAuth2ConfigurerUtilsAccessor
                .getAuthorizationServerSettings(http);
        String tokenEndpointUri = authorizationServerSettings.getTokenEndpoint();
        RequestMatcher tokenEndpointMatcher = PathPatternRequestMatcher
                .pathPattern(HttpMethod.POST, tokenEndpointUri);

        // 2. Create shared ClientAttestationVerifier for PoP JWT verification
        ClientAttestationVerifier attestationVerifier = new ClientAttestationVerifier(registrationService);
        attestationVerifier.setChallengeService(challengeService);
        attestationVerifier.setNonceService(nonceService);

        // 3. Register Converter + Provider for attest_jwt_client_auth client authentication
        EulerOAuth2ClientAttestationAuthenticationConverter attestConverter =
                new EulerOAuth2ClientAttestationAuthenticationConverter();
        EulerOAuth2ClientAttestationAuthenticationProvider attestProvider =
                new EulerOAuth2ClientAttestationAuthenticationProvider(
                        registeredClientRepository, validationService, attestationVerifier);

        http.oauth2AuthorizationServer(oauth2 -> oauth2
                .clientAuthentication(clientAuth -> clientAuth
                        .authenticationConverter(attestConverter)
                        .authenticationProvider(attestProvider)));

        // 4. Register attestation filter for Scenario A enhancement and keyId extraction
        EulerOAuth2AttestationBasedClientAuthenticationFilter attestationFilter = new EulerOAuth2AttestationBasedClientAuthenticationFilter(
                tokenEndpointMatcher, attestConverter, attestProvider);
        http.addFilterAfter(attestationFilter, OAuth2ClientAuthenticationFilter.class);

        // 5. Register the slimmed-down grant type (anonymous user mode)
        OAuth2AppleAppAttestAssertionAuthenticationProvider provider =
                new OAuth2AppleAppAttestAssertionAuthenticationProvider(
                        userDetailsService,
                        OAuth2ConfigurerUtilsAccessor.getAuthorizationService(http),
                        OAuth2ConfigurerUtilsAccessor.getTokenGenerator(http));

        http.oauth2AuthorizationServer(oauth2 -> oauth2
                .tokenEndpoint(configurer -> configurer
                        .authenticationProvider(provider)
                        .accessTokenRequestConverter(new OAuth2AppleAppAttestAssertionAuthenticationConverter())));

        // 6. Add attestation metadata to OIDC provider configuration and AS metadata endpoints
        //    (draft-ietf-oauth-attestation-based-client-auth-08 Section 9)
        String issuer = authorizationServerSettings.getIssuer();
        String challengeEndpoint = (issuer != null ? issuer : "") + "/app/attest/challenge";
        List<String> supportedSigningAlgs = List.of("ES256");

        http.oauth2AuthorizationServer(oauth2 -> oauth2
                .oidc(oidc -> oidc
                        .providerConfigurationEndpoint(config -> config
                                .providerConfigurationCustomizer(builder -> {
                                    builder.tokenEndpointAuthenticationMethod(
                                            EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH.getValue());
                                    builder.claim("attestation_challenge_endpoint", challengeEndpoint);
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
                            builder.claim("attestation_challenge_endpoint", challengeEndpoint);
                            builder.claim("client_attestation_signing_alg_values_supported",
                                    supportedSigningAlgs);
                            builder.claim("client_attestation_pop_signing_alg_values_supported",
                                    supportedSigningAlgs);
                        })
                )
        );
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
