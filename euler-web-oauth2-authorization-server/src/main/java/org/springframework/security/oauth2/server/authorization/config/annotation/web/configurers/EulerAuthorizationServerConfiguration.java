package org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2PasswordAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2PasswordAuthenticationProvider;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration(proxyBeanMethods = false)
public class EulerAuthorizationServerConfiguration {
    private static final Log logger = LogFactory.getLog(EulerAuthorizationServerConfiguration.class);

    private final AuthenticationConfiguration authenticationConfiguration;

    /**
     * Only for build authorizationServerSecurityFilterChain
     */
    private final HttpSecurity http;

    public EulerAuthorizationServerConfiguration(AuthenticationConfiguration authenticationConfiguration, HttpSecurity http) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.http = http;
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain() throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();
        RequestMatcher endpointsMatcher = authorizationServerConfigurer.getEndpointsMatcher();
        http
                .securityMatcher(endpointsMatcher)
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .csrf(csrf -> csrf.ignoringRequestMatchers(endpointsMatcher))
                .with(authorizationServerConfigurer, this::configAuthorizationServer);

        return http.build();
    }

    private void configAuthorizationServer(OAuth2AuthorizationServerConfigurer configurer) {
        configurer
                .tokenEndpoint(this::configTokenEndpoint)
                .tokenIntrospectionEndpoint(this::configTokenIntrospectionEndpoint);
    }


    private void configTokenEndpoint(OAuth2TokenEndpointConfigurer configurer) {
        configurer
                .authenticationProvider(this.getOAuth2PasswordAuthenticationProvider())
                .accessTokenRequestConverter(this.getOAuth2PasswordAuthenticationConverter());
    }

    private void configTokenIntrospectionEndpoint(OAuth2TokenIntrospectionEndpointConfigurer configurer) {
    }

    private OAuth2PasswordAuthenticationProvider getOAuth2PasswordAuthenticationProvider() {
        try {
            return new OAuth2PasswordAuthenticationProvider(
                    this.authenticationConfiguration.getAuthenticationManager(),
                    OAuth2ConfigurerUtils.getAuthorizationService(http),
                    OAuth2ConfigurerUtils.getTokenGenerator(http)
            );
        } catch (Exception e) {
            throw ExceptionUtils.asRuntimeException(e);
        }
    }


    private OAuth2PasswordAuthenticationConverter getOAuth2PasswordAuthenticationConverter() {
        return new OAuth2PasswordAuthenticationConverter();
    }
}
