package org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2PasswordAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2PasswordAuthenticationProvider;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.ArrayList;
import java.util.List;

public class EulerAuthorizationServerConfig {
    private static final Log logger = LogFactory.getLog(EulerAuthorizationServerConfig.class);

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        applyDefaultSecurity(http);
        return http.build();
    }

    public static void applyDefaultSecurity(HttpSecurity http) throws Exception {

        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer();
        RequestMatcher endpointsMatcher = authorizationServerConfigurer
                .getEndpointsMatcher();


        List<AuthenticationProvider> a = new ArrayList<>();

        http
                .securityMatcher(endpointsMatcher)
                .authorizeHttpRequests(authorize ->
                        authorize.anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers(endpointsMatcher))
                .with(authorizationServerConfigurer, configurer ->
                                configurer
//                                    .clientAuthentication(clientAuthenticationCustomizer ->
//                                            clientAuthenticationCustomizer
//                                                    .authenticationProvider(authenticationProvider)
//                                                    .authenticationConverter(new OAuth2ResourceOwnerPasswordAuthenticationConverter()))
                                        .tokenEndpoint(oAuth2TokenEndpointConfigurer ->
                                                oAuth2TokenEndpointConfigurer
                                                        .authenticationProviders(authenticationProviders -> {
                                                            authenticationProviders.add(new OAuth2PasswordAuthenticationProvider(
                                                                    OAuth2ConfigurerUtils.getAuthorizationService(http),
                                                                    OAuth2ConfigurerUtils.getTokenGenerator(http)
                                                            ));
                                                            a.addAll(authenticationProviders);
                                                        })
                                                        .accessTokenRequestConverter(new OAuth2PasswordAuthenticationConverter())
                                                        .accessTokenRequestConverters(authenticationConverters -> {
                                                            for (AuthenticationProvider provider : a) {
                                                                if (provider instanceof OAuth2PasswordAuthenticationProvider) {
                                                                    ((OAuth2PasswordAuthenticationProvider) provider).setUserDetailsAuthenticationManager(http.getSharedObject(AuthenticationManager.class));
                                                                }
                                                            }
                                                        }))
                );
    }
}
