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

import org.eulerframework.security.web.login.LoginMethod;
import org.eulerframework.security.web.login.LoginMethodDispatch;
import org.eulerframework.security.web.login.RegisteredOAuth2LoginMethod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

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

        RegisteredOAuth2LoginMethod method = new RegisteredOAuth2LoginMethod(
                "test-registration", "google-eu", "google", false, null, "google");
        LoginMethod described = this.handler.describe(method);

        assertThat(described).isNotNull();
        assertThat(described.getType()).isEqualTo("oauth2");
        assertThat(described.getName()).isEqualTo("google-eu");
        assertThat(described.getAttributes().get("provider")).isEqualTo("google");
    }

    @Test
    void fallsBackToProviderWhenRegistrationIdOmitted() {
        when(this.repository.findByRegistrationId("google"))
                .thenReturn(googleRegistration());

        RegisteredOAuth2LoginMethod method = method("corp-sso", "google");
        LoginMethod described = this.handler.describe(method);

        assertThat(described).isNotNull();
        assertThat(described.getName()).isEqualTo("corp-sso");
        assertThat(described.getAttributes().get("provider")).isEqualTo("google");
    }

    @Test
    void explicitProviderOverridesIdentityType() {
        when(this.repository.findByRegistrationId("google"))
                .thenReturn(googleRegistration());

        RegisteredOAuth2LoginMethod method = new RegisteredOAuth2LoginMethod(
                "corp-sso", "corp-sso", "corp-account", false, "google", null);
        LoginMethod described = this.handler.describe(method);

        assertThat(described).isNotNull();
        assertThat(described.getAttributes().get("provider")).isEqualTo("google");
    }

    @Test
    void returnsNullWhenIdentityTypeMissing() {
        RegisteredOAuth2LoginMethod method = new RegisteredOAuth2LoginMethod(
                "missing", "missing", null, false, null, null);

        assertThat(this.handler.describe(method)).isNull();
    }

    @Test
    void returnsNullWhenRegistrationNotFound() {
        when(this.repository.findByRegistrationId("google")).thenReturn(null);

        RegisteredOAuth2LoginMethod method = method("google", "google");

        assertThat(this.handler.describe(method)).isNull();
    }

    @Test
    void dispatchReturnsRedirectToAuthorizationEndpoint() {
        RegisteredOAuth2LoginMethod method = method("google", "google");
        MockHttpServletRequest request = new MockHttpServletRequest();

        LoginMethodDispatch dispatch = this.handler.dispatch(method, request);

        assertThat(dispatch.getAction()).isEqualTo(LoginMethodDispatch.Action.REDIRECT_302);
        assertThat(dispatch.getLocation()).isEqualTo("/oauth2/authorization/google");
    }

    /**
     * The provider is what the option is branded by, so it falls back on
     * the identity type when not declared.
     */
    @Test
    void providerFallsBackToIdentityType() {
        assertThat(OAuth2LoginMethodHandler.resolveProvider(method("google", "google")))
                .isEqualTo("google");
        assertThat(OAuth2LoginMethodHandler.resolveProvider(new RegisteredOAuth2LoginMethod(
                "corp-sso", "corp-sso", "corp-account", false, "google", null)))
                .isEqualTo("google");
    }

    @Test
    void reportsCorrectType() {
        assertThat(this.handler.type()).isEqualTo("oauth2");
    }

    /** A registration as the contributor hands it to the handler. */
    private static RegisteredOAuth2LoginMethod method(String name, String identityType) {
        return new RegisteredOAuth2LoginMethod(
                "test-registration", name, identityType, false, null, null);
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
