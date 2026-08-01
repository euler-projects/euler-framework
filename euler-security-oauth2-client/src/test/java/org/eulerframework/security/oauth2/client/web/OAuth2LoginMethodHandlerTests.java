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

import org.eulerframework.security.web.endpoint.user.login.LoginMethod;
import org.eulerframework.security.web.endpoint.user.login.LoginMethodDispatch;
import org.eulerframework.security.web.endpoint.user.login.RegisteredLoginMethod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class OAuth2LoginMethodHandlerTests {

    @Mock
    ClientRegistrationRepository repository;

    private OAuth2LoginMethodHandler handler;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        this.mocks = MockitoAnnotations.openMocks(this);
        this.handler = new OAuth2LoginMethodHandler(this.repository);
    }

    @AfterEach
    void tearDown() throws Exception {
        this.mocks.close();
    }

    @Test
    void resolvesRegistrationByExplicitProperty() {
        when(this.repository.findByRegistrationId("google"))
                .thenReturn(googleRegistration());

        RegisteredLoginMethod method = method("google", Map.of(
                "oauth-client-registration-id", "google"));
        LoginMethod described = this.handler.describe("google-eu", method);

        assertThat(described).isNotNull();
        assertThat(described.getType()).isEqualTo("oauth2");
        assertThat(described.getName()).isEqualTo("google-eu");
        assertThat(described.getAttributes().get("provider")).isEqualTo("google");
    }

    @Test
    void fallsBackToProviderWhenRegistrationIdOmitted() {
        when(this.repository.findByRegistrationId("google"))
                .thenReturn(googleRegistration());

        RegisteredLoginMethod method = method("google", Map.of());
        LoginMethod described = this.handler.describe("corp-sso", method);

        assertThat(described).isNotNull();
        assertThat(described.getName()).isEqualTo("corp-sso");
        assertThat(described.getAttributes().get("provider")).isEqualTo("google");
    }

    @Test
    void explicitProviderOverridesIdentityType() {
        when(this.repository.findByRegistrationId("google"))
                .thenReturn(googleRegistration());

        RegisteredLoginMethod method = RegisteredLoginMethod.withId("corp-sso")
                .type("oauth2")
                .identityType("corp-account")
                .property("provider", "google")
                .build();
        LoginMethod described = this.handler.describe("corp-sso", method);

        assertThat(described).isNotNull();
        assertThat(described.getAttributes().get("provider")).isEqualTo("google");
    }

    @Test
    void returnsNullWhenIdentityTypeMissing() {
        RegisteredLoginMethod method = RegisteredLoginMethod.withId("missing").type("oauth2").build();

        assertThat(this.handler.describe("missing", method)).isNull();
    }

    @Test
    void returnsNullWhenRegistrationNotFound() {
        when(this.repository.findByRegistrationId("google")).thenReturn(null);

        RegisteredLoginMethod method = method("google", Map.of());

        assertThat(this.handler.describe("google", method)).isNull();
    }

    @Test
    void dispatchReturnsRedirectToAuthorizationEndpoint() {
        RegisteredLoginMethod method = method("google", Map.of());
        MockHttpServletRequest request = new MockHttpServletRequest();

        LoginMethodDispatch dispatch = this.handler.dispatch("google", method, request);

        assertThat(dispatch.getAction()).isEqualTo(LoginMethodDispatch.Action.REDIRECT_302);
        assertThat(dispatch.getLocation()).isEqualTo("/oauth2/authorization/google");
    }

    @Test
    void resolveNameDerivesFromProvider() {
        assertThat(this.handler.resolveName(method("google", Map.of()))).isEqualTo("google");
        assertThat(this.handler.resolveName(RegisteredLoginMethod.withId("corp-sso")
                .type("oauth2")
                .identityType("corp-account")
                .property("provider", "google")
                .build())).isEqualTo("google");
    }

    @Test
    void reportsCorrectType() {
        assertThat(this.handler.type()).isEqualTo("oauth2");
    }

    private static RegisteredLoginMethod method(String identityType, Map<String, Object> properties) {
        return RegisteredLoginMethod.withId("test-registration")
                .type("oauth2")
                .identityType(identityType)
                .properties(properties)
                .build();
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
