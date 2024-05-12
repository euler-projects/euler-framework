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
