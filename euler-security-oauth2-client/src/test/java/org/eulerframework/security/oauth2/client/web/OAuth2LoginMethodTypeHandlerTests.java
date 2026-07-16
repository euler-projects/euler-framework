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
package org.eulerframework.security.oauth2.client.web;

import org.eulerframework.security.web.endpoint.user.login.LoginMethodView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class OAuth2LoginMethodTypeHandlerTests {

    @Mock
    ClientRegistrationRepository repository;

    private OAuth2LoginMethodTypeHandler handler;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        this.mocks = MockitoAnnotations.openMocks(this);
        this.handler = new OAuth2LoginMethodTypeHandler(this.repository);
    }

    @AfterEach
    void tearDown() throws Exception {
        this.mocks.close();
    }

    @Test
    void resolvesRegistrationByExplicitProperty() {
        when(this.repository.findByRegistrationId("google"))
                .thenReturn(googleRegistration());

        LoginMethodView view = this.handler.toView("google-eu", Map.of(
                "oauth-client-registration-id", "google"));

        assertThat(view).isNotNull();
        assertThat(view.type()).isEqualTo("oauth2");
        assertThat(view.id()).isEqualTo("google");
        assertThat(view.href()).isEqualTo("/oauth2/authorization/google");
        assertThat(view.displayName()).isEqualTo("Google");
        assertThat(view.iconClass()).isEqualTo("btn-oauth2-google");
    }

    @Test
    void fallsBackToLoginMethodKeyWhenRegistrationIdOmitted() {
        when(this.repository.findByRegistrationId("google"))
                .thenReturn(googleRegistration());

        LoginMethodView view = this.handler.toView("google", Map.of());

        assertThat(view).isNotNull();
        assertThat(view.id()).isEqualTo("google");
        assertThat(view.href()).isEqualTo("/oauth2/authorization/google");
    }

    @Test
    void returnsNullWhenRegistrationNotFound() {
        when(this.repository.findByRegistrationId("missing")).thenReturn(null);

        LoginMethodView view = this.handler.toView("missing", Map.of());

        assertThat(view).isNull();
    }

    @Test
    void displayNameAndIconClassOverridesApply() {
        when(this.repository.findByRegistrationId("google"))
                .thenReturn(googleRegistration());

        LoginMethodView view = this.handler.toView("google", Map.of(
                "display-name", "Corp SSO",
                "icon-class", "btn-oauth2-corp"));

        assertThat(view).isNotNull();
        assertThat(view.displayName()).isEqualTo("Corp SSO");
        assertThat(view.iconClass()).isEqualTo("btn-oauth2-corp");
    }

    @Test
    void reportsCorrectType() {
        assertThat(this.handler.type()).isEqualTo("oauth2");
    }

    private static ClientRegistration googleRegistration() {
        return ClientRegistration.withRegistrationId("google")
                .clientId("client-id")
                .clientSecret("client-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid", "profile", "email")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://openidconnect.googleapis.com/v1/userinfo")
                .userNameAttributeName("sub")
                .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                .clientName("Google")
                .build();
    }
}
