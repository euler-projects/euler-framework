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
import org.eulerframework.security.core.userdetails.EulerDeviceAttestationUserDetailsService;
import org.eulerframework.security.oauth2.core.EulerClientAuthenticationMethod;
import org.eulerframework.security.oauth2.server.authorization.authentication.*;
import org.eulerframework.security.oauth2.server.authorization.converter.EulerOAuth2ClientRegistrationRegisteredClientConverter;
import org.eulerframework.security.oauth2.server.authorization.converter.EulerRegisteredClientOAuth2ClientRegistrationConverter;
import org.eulerframework.security.oauth2.server.authorization.oidc.authentication.UserDetailsOidcUserInfoMapper;
import org.eulerframework.security.oauth2.server.authorization.web.authentication.EulerOAuth2ClientAttestationAuthenticationConverter;
import org.eulerframework.security.oauth2.server.authorization.web.authentication.OAuth2DeviceAssertionAuthenticationConverter;
import org.eulerframework.security.oauth2.server.authorization.web.authentication.OAuth2PasswordAuthenticationConverter;
import org.eulerframework.security.oauth2.server.authorization.web.authentication.OAuth2WechatAuthorizationCodeAuthenticationConverter;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2ConfigurerUtilsAccessor;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientRegistrationAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

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

    public static void configClientAttestationAuthentication(HttpSecurity http, AuthenticationConfiguration authenticationConfiguration) {
        EulerOAuth2ClientAttestationAuthenticationConverter attestConverter = EulerOAuth2ConfigurerUtils
                .getEulerOAuth2ClientAttestationAuthenticationConverter(http);
        EulerOAuth2ClientAttestationAuthenticationProvider attestProvider = EulerOAuth2ConfigurerUtils
                .getEulerOAuth2ClientAttestationAuthenticationProvider(http);

        // Register as standard Client Authentication
        http.oauth2AuthorizationServer(oauth2 -> oauth2
                .clientAuthentication(clientAuth -> clientAuth
                        .authenticationConverter(attestConverter)
                        .authenticationProvider(attestProvider)));

        // Add attestation metadata to OIDC provider configuration and AS metadata endpoints
        //   (draft-ietf-oauth-attestation-based-client-auth-08 Section 9)
        AuthorizationServerSettings authorizationServerSettings = OAuth2ConfigurerUtilsAccessor
                .getAuthorizationServerSettings(http);
        String issuer = authorizationServerSettings.getIssuer();
        String challengeEndpointFullUri = (issuer != null ? issuer : "") + EulerOAuth2AuthorizationServerConfigurer.DEFAULT_CHALLENGE_ENDPOINT_URI;
        List<String> supportedSigningAlgs = List.of("ES256");
        http.oauth2AuthorizationServer(oauth2 -> oauth2
                .oidc(oidc -> oidc
                        .providerConfigurationEndpoint(config -> config
                                .providerConfigurationCustomizer(builder -> {
                                    builder.tokenEndpointAuthenticationMethod(
                                            EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH.getValue());
                                    builder.claim("challenge_endpoint", challengeEndpointFullUri);
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
                            builder.claim("challenge_endpoint", challengeEndpointFullUri);
                            builder.claim("client_attestation_signing_alg_values_supported",
                                    supportedSigningAlgs);
                            builder.claim("client_attestation_pop_signing_alg_values_supported",
                                    supportedSigningAlgs);
                        })
                )
        );

        EulerDeviceAttestationUserDetailsService userDetailsService =
                EulerOAuth2ConfigurerUtils.getAppleAppAttestUserDetailsServiceIfAvailable(http);
        if (userDetailsService != null) {
            OAuth2DeviceAssertionAuthenticationProvider grantProvider =
                    new OAuth2DeviceAssertionAuthenticationProvider(
                            userDetailsService,
                            OAuth2ConfigurerUtilsAccessor.getAuthorizationService(http),
                            OAuth2ConfigurerUtilsAccessor.getTokenGenerator(http));

            http.oauth2AuthorizationServer(oauth2 -> oauth2
                    .tokenEndpoint(configurer -> configurer
                            .authenticationProvider(grantProvider)
                            .accessTokenRequestConverter(new OAuth2DeviceAssertionAuthenticationConverter())));
        }
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
