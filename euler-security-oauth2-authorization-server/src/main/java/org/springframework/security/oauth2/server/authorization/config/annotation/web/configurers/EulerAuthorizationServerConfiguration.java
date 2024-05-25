/*
 * Copyright 2013-2024 the original author or authors.
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
package org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2PrincipalSupportTokenIntrospectionAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2PasswordAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2PasswordAuthenticationProvider;

public class EulerAuthorizationServerConfiguration {
    public static void configPasswordAuthentication(HttpSecurity http, AuthenticationConfiguration authenticationConfiguration) {
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class).tokenEndpoint(configurer -> configurer
                .authenticationProvider(getOAuth2PasswordAuthenticationProvider(http, authenticationConfiguration))
                .accessTokenRequestConverter(getOAuth2PasswordAuthenticationConverter()));
    }

    public static void configPrincipalSupportTokenIntrospectionAuthentication(HttpSecurity http) {
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class).tokenIntrospectionEndpoint(configurer -> configurer
                .authenticationProvider(getOAuth2PasswordAuthenticationProvider(http)));
    }

    private static OAuth2PasswordAuthenticationProvider getOAuth2PasswordAuthenticationProvider(HttpSecurity http, AuthenticationConfiguration authenticationConfiguration) {
        try {
            return new OAuth2PasswordAuthenticationProvider(
                    authenticationConfiguration.getAuthenticationManager(),
                    OAuth2ConfigurerUtils.getAuthorizationService(http),
                    OAuth2ConfigurerUtils.getTokenGenerator(http)
            );
        } catch (Exception e) {
            throw ExceptionUtils.asRuntimeException(e);
        }
    }

    private static OAuth2PrincipalSupportTokenIntrospectionAuthenticationProvider getOAuth2PasswordAuthenticationProvider(HttpSecurity http) {
        return new OAuth2PrincipalSupportTokenIntrospectionAuthenticationProvider(
                OAuth2ConfigurerUtils.getRegisteredClientRepository(http),
                OAuth2ConfigurerUtils.getAuthorizationService(http));
    }

    private static OAuth2PasswordAuthenticationConverter getOAuth2PasswordAuthenticationConverter() {
        return new OAuth2PasswordAuthenticationConverter();
    }
}
