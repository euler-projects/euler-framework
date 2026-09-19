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

package org.eulerframework.security.config.annotation.web.configurers.appattest;

import org.eulerframework.security.web.authentication.appattest.AppAttestProviderConfigurationEndpointFilter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Configurer for the App Attest provider configuration (discovery) endpoint, mirroring Spring
 * Authorization Server's {@code OidcProviderConfigurationEndpointConfigurer}.
 * <p>
 * This is a sub-configurer driven by {@link AppAttestConfigurer}: it is not registered with
 * {@code HttpSecurity} directly. {@link AppAttestConfigurer} creates it, injects the endpoint
 * {@code URI}s to advertise, and calls {@link #init(HttpSecurity)} / {@link #configure(HttpSecurity)}
 * at the matching lifecycle phases.
 *
 * @see AppAttestConfigurer#providerConfigurationEndpoint
 * @see AppAttestProviderConfigurationEndpointFilter
 */
public final class AppAttestProviderConfigurationEndpointConfigurer {

    private final ObjectPostProcessor<Object> objectPostProcessor;

    private String challengeEndpointUri;
    private String registrationEndpointUri;
    private Consumer<Map<String, Object>> providerConfigurationCustomizer;

    private RequestMatcher requestMatcher;

    AppAttestProviderConfigurationEndpointConfigurer(ObjectPostProcessor<Object> objectPostProcessor) {
        Assert.notNull(objectPostProcessor, "objectPostProcessor must not be null");
        this.objectPostProcessor = objectPostProcessor;
    }

    /**
     * Sets the {@code Consumer} providing access to the claims of the provider configuration
     * document, allowing additional claims to be added or the advertised endpoints to be overridden.
     *
     * @param providerConfigurationCustomizer the claims customizer
     * @return this configurer for chaining
     */
    public AppAttestProviderConfigurationEndpointConfigurer providerConfigurationCustomizer(
            Consumer<Map<String, Object>> providerConfigurationCustomizer) {
        Assert.notNull(providerConfigurationCustomizer, "providerConfigurationCustomizer must not be null");
        this.providerConfigurationCustomizer = providerConfigurationCustomizer;
        return this;
    }

    void setChallengeEndpointUri(String challengeEndpointUri) {
        this.challengeEndpointUri = challengeEndpointUri;
    }

    void setRegistrationEndpointUri(String registrationEndpointUri) {
        this.registrationEndpointUri = registrationEndpointUri;
    }

    void init(HttpSecurity http) {
        this.requestMatcher = PathPatternRequestMatcher.pathPattern(HttpMethod.GET,
                AppAttestProviderConfigurationEndpointFilter.DEFAULT_PROVIDER_CONFIGURATION_ENDPOINT_URI);
    }

    void configure(HttpSecurity http) {
        AppAttestProviderConfigurationEndpointFilter filter = new AppAttestProviderConfigurationEndpointFilter(
                this.challengeEndpointUri, this.registrationEndpointUri);
        if (this.providerConfigurationCustomizer != null) {
            filter.setProviderConfigurationCustomizer(this.providerConfigurationCustomizer);
        }
        http.addFilterBefore(this.objectPostProcessor.postProcess(filter), AuthorizationFilter.class);
    }

    RequestMatcher getRequestMatcher() {
        return this.requestMatcher;
    }
}
