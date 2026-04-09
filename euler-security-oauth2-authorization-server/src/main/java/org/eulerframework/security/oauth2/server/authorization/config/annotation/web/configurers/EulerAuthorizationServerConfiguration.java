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
package org.eulerframework.security.oauth2.server.authorization.config.annotation.web.configurers;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eulerframework.security.oauth2.server.authorization.authentication.OAuth2AppleAppAttestAssertionAuthenticationProvider;
import org.eulerframework.security.oauth2.server.authorization.authentication.OAuth2AppleAppAttestAttestationAuthenticationProvider;
import org.eulerframework.security.oauth2.server.authorization.authentication.OAuth2PasswordAuthenticationProvider;
import org.eulerframework.security.oauth2.server.authorization.authentication.OAuth2WechatAuthorizationCodeAuthenticationProvider;
import org.eulerframework.security.authentication.ChallengeService;
import org.eulerframework.security.oauth2.core.EulerAuthorizationGrantType;
import org.eulerframework.security.oauth2.server.authorization.converter.EulerOAuth2ClientRegistrationRegisteredClientConverter;
import org.eulerframework.security.oauth2.server.authorization.converter.EulerRegisteredClientOAuth2ClientRegistrationConverter;
import org.eulerframework.security.oauth2.server.authorization.oidc.authentication.UserDetailsOidcUserInfoMapper;
import org.eulerframework.security.oauth2.server.authorization.web.authentication.EulerPublicClientAuthenticationConverter;
import org.eulerframework.security.oauth2.server.authorization.web.authentication.OAuth2AppleAppAttestAssertionAuthenticationConverter;
import org.eulerframework.security.oauth2.server.authorization.web.authentication.OAuth2AppleAppAttestAttestationAuthenticationConverter;
import org.eulerframework.security.oauth2.server.authorization.web.authentication.OAuth2PasswordAuthenticationConverter;
import org.eulerframework.security.oauth2.server.authorization.web.authentication.OAuth2WechatAuthorizationCodeAuthenticationConverter;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.ConfigurerAccessor;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2ConfigurerUtilsAccessor;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientRegistrationAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

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
        http.oauth2AuthorizationServer(oauth2AuthorizationServer -> oauth2AuthorizationServer
                .clientRegistrationEndpoint(configurer ->
                        http.authorizeHttpRequests((authorize) -> authorize
                                .requestMatchers(ConfigurerAccessor.getDeferredRequestMatcher(configurer))
                                .permitAll())));
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
     * Configure Apple App Attest authentication for both attestation and assertion grant types.
     * <p>
     * This method registers {@link EulerPublicClientAuthenticationConverter} with the token endpoint's
     * client authentication filter, enabling public clients
     * ({@link org.springframework.security.oauth2.core.ClientAuthenticationMethod#NONE NONE})
     * to use Apple App Attest grant types.
     * <p>
     * Note: The corresponding {@link org.eulerframework.security.oauth2.server.authorization.authentication.EulerPublicClientAuthenticationProvider
     * EulerPublicClientAuthenticationProvider} is registered by {@link EulerOAuth2AuthorizationServerConfigurer}
     * into the shared {@link org.springframework.security.authentication.AuthenticationManager AuthenticationManager},
     * which is shared by both the token endpoint and the Euler endpoint client authentication filters.
     * Only the converter needs to be added here since converters are per-filter.
     */
    public static void configAppleAppAttestAuthentication(HttpSecurity http, AuthenticationConfiguration authenticationConfiguration) {
        ChallengeService challengeService = EulerOAuth2ConfigurerUtils.getChallengeService(http);

        // Register the public client authentication converter for the token endpoint.
        // The provider is already in the shared AuthenticationManager, registered by
        // EulerOAuth2AuthorizationServerConfigurer.init() — only the converter is per-filter.
        http.oauth2AuthorizationServer(oauth2AuthorizationServer -> oauth2AuthorizationServer
                .clientAuthentication(clientAuth -> clientAuth
                        .authenticationConverter(new EulerPublicClientAuthenticationConverter())
                ));

        // Register attestation and assertion grant types
        http.oauth2AuthorizationServer(oauth2AuthorizationServer -> oauth2AuthorizationServer
                .tokenEndpoint(configurer -> configurer
                        .authenticationProvider(getOAuth2AppleAppAttestAttestationAuthenticationProvider(http, authenticationConfiguration, challengeService))
                        .accessTokenRequestConverter(getOAuth2AppleAppAttestAttestationAuthenticationConverter())
                        .authenticationProvider(getOAuth2AppleAppAttestAssertionAuthenticationProvider(http, authenticationConfiguration, challengeService))
                        .accessTokenRequestConverter(getOAuth2AppleAppAttestAssertionAuthenticationConverter())));

        // Auto-register grant types with challenge endpoint
        EulerOAuth2AuthorizationServerConfigurer eulerConfigurer =
                http.getConfigurer(EulerOAuth2AuthorizationServerConfigurer.class);
        if (eulerConfigurer != null) {
            eulerConfigurer.challengeEndpoint(challenge ->
                    challenge.authorizedGrantTypes(
                            EulerAuthorizationGrantType.APPLE_APP_ATTEST_ATTESTATION,
                            EulerAuthorizationGrantType.APPLE_APP_ATTEST_ASSERTION));
        }
    }

    private static OAuth2AppleAppAttestAttestationAuthenticationProvider getOAuth2AppleAppAttestAttestationAuthenticationProvider(
            HttpSecurity http, AuthenticationConfiguration authenticationConfiguration,
            ChallengeService challengeService) {
        try {
            return new OAuth2AppleAppAttestAttestationAuthenticationProvider(
                    authenticationConfiguration.getAuthenticationManager(),
                    OAuth2ConfigurerUtilsAccessor.getAuthorizationService(http),
                    OAuth2ConfigurerUtilsAccessor.getTokenGenerator(http),
                    challengeService
            );
        } catch (Exception e) {
            throw ExceptionUtils.asRuntimeException(e);
        }
    }

    private static OAuth2AppleAppAttestAttestationAuthenticationConverter getOAuth2AppleAppAttestAttestationAuthenticationConverter() {
        return new OAuth2AppleAppAttestAttestationAuthenticationConverter();
    }

    private static OAuth2AppleAppAttestAssertionAuthenticationProvider getOAuth2AppleAppAttestAssertionAuthenticationProvider(
            HttpSecurity http, AuthenticationConfiguration authenticationConfiguration,
            ChallengeService challengeService) {
        try {
            return new OAuth2AppleAppAttestAssertionAuthenticationProvider(
                    authenticationConfiguration.getAuthenticationManager(),
                    OAuth2ConfigurerUtilsAccessor.getAuthorizationService(http),
                    OAuth2ConfigurerUtilsAccessor.getTokenGenerator(http),
                    challengeService
            );
        } catch (Exception e) {
            throw ExceptionUtils.asRuntimeException(e);
        }
    }

    private static OAuth2AppleAppAttestAssertionAuthenticationConverter getOAuth2AppleAppAttestAssertionAuthenticationConverter() {
        return new OAuth2AppleAppAttestAssertionAuthenticationConverter();
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
