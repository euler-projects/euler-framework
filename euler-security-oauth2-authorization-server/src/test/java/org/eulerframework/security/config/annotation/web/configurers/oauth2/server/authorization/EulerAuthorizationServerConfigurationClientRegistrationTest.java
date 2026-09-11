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

import org.eulerframework.security.authentication.appattest.AppAttestAttestationRegistration;
import org.eulerframework.security.authentication.appattest.AppAttestAttestationRegistrationService;
import org.eulerframework.security.authentication.appattest.InMemoryAppAttestAttestationRegistrationService;
import org.eulerframework.security.authentication.appattest.InMemoryRegisteredAppRepository;
import org.eulerframework.security.authentication.appattest.RegisteredAppRepository;
import org.eulerframework.security.authentication.appattest.apple.AppleAppAttestValidationService;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Assembly-level regression tests for
 * {@link EulerAuthorizationServerConfiguration#configClientRegistrationEndpoint}.
 * <p>
 * The App Attest converter, the App Attest provider and the {@code permitAll} rule that lets an
 * assertion-carrying request reach the endpoint are all registered under one condition, because a
 * {@code permitAll} without them would leave the endpoint unenforced and the components without it
 * would be unreachable behind the chain's catch-all {@code authenticated()} rule. These tests pin
 * that coupling: the authorization rule is added exactly when the App Attest beans are present.
 */
class EulerAuthorizationServerConfigurationClientRegistrationTest {

    @Test
    void permitsTheRegistrationEndpointWhenAppAttestIsAvailable() {
        HttpSecurity http = httpSecurity(appAttestEnabledContext());

        assertDoesNotThrow(() -> EulerAuthorizationServerConfiguration.configClientRegistrationEndpoint(http, null));

        // The permitAll rule is registered, so an assertion-carrying request that has no bearer
        // token can reach the converter and the provider that enforce the endpoint instead.
        assertNotNull(http.getConfigurer(AuthorizeHttpRequestsConfigurer.class));
    }

    @Test
    void leavesTheRegistrationEndpointAuthenticatedWhenAppAttestIsAbsent() {
        HttpSecurity http = httpSecurity(contextWithoutAppAttest());

        assertDoesNotThrow(() -> EulerAuthorizationServerConfiguration.configClientRegistrationEndpoint(http, null));

        // No permitAll rule: without the App Attest components there is nothing to enforce the
        // endpoint, so the chain's own authenticated() rule must stay in charge of it.
        assertNull(http.getConfigurer(AuthorizeHttpRequestsConfigurer.class));
    }

    // ---- helpers ----

    private static HttpSecurity httpSecurity(GenericApplicationContext context) {
        ObjectPostProcessor<Object> objectPostProcessor = ObjectPostProcessor.identity();
        Map<Class<?>, Object> sharedObjects = new HashMap<>();
        sharedObjects.put(ApplicationContext.class, context);
        return new HttpSecurity(objectPostProcessor, new AuthenticationManagerBuilder(objectPostProcessor),
                sharedObjects);
    }

    private static GenericApplicationContext appAttestEnabledContext() {
        GenericApplicationContext context = baseContext();
        context.registerBean(AppleAppAttestValidationService.class, () -> new StubValidationService());
        context.registerBean(AppAttestAttestationRegistrationService.class,
                () -> new InMemoryAppAttestAttestationRegistrationService());
        context.registerBean(RegisteredAppRepository.class, () -> new InMemoryRegisteredAppRepository());
        context.refresh();
        return context;
    }

    private static GenericApplicationContext contextWithoutAppAttest() {
        GenericApplicationContext context = baseContext();
        context.refresh();
        return context;
    }

    private static GenericApplicationContext baseContext() {
        GenericApplicationContext context = new GenericApplicationContext();
        // Provided by the authorization server auto-configuration in a real application;
        // Spring's OAuth2ConfigurerUtils resolves it as a required bean.
        context.registerBean(AuthorizationServerSettings.class,
                () -> AuthorizationServerSettings.builder().build());
        RegisteredClient client = RegisteredClient.withId("id-1")
                .clientId("client-1")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientSecret("secret")
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .build();
        context.registerBean(RegisteredClientRepository.class,
                () -> new InMemoryRegisteredClientRepository(client));
        return context;
    }

    // ---- fakes ----

    static class StubValidationService implements AppleAppAttestValidationService {

        @Override
        public AppAttestAttestationRegistration validateAttestation(String attestation, String challenge) {
            throw new UnsupportedOperationException();
        }

        @Override
        public AppAttestAttestationRegistration validateAssertion(String keyId, String assertion, String challenge) {
            throw new UnsupportedOperationException();
        }
    }
}
