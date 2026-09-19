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

import org.eulerframework.security.authentication.ChallengeService;
import org.eulerframework.security.authentication.appattest.apple.AppleAppAttestValidationService;
import org.eulerframework.security.authentication.appattest.AppAttestAttestationRegistrationAuthenticationProvider;
import org.eulerframework.security.authentication.appattest.AppAttestAttestationRegistrationService;
import org.eulerframework.security.authentication.appattest.RegisteredAppRepository;
import org.eulerframework.security.core.userdetails.EulerDeviceUserDetailsService;
import org.eulerframework.security.provisioning.jit.JitProvisioningPolicy;
import org.eulerframework.security.web.authentication.ChallengeEndpointFilter;
import org.eulerframework.security.web.authentication.appattest.AppAttestProviderConfigurationEndpointFilter;
import org.eulerframework.security.web.authentication.appattest.AppAttestRegistrationAuthenticationConverter;
import org.eulerframework.security.web.authentication.appattest.AppAttestRegistrationEndpointFilter;
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;

/**
 * An {@link AbstractHttpConfigurer} for Apple App Attest instance registration and its
 * discovery document.
 * <p>
 * This configurer registers the challenge, registration and provider configuration endpoint
 * filters into the default security filter chain. All three are exempt from CSRF protection and
 * reachable without a prior session or OAuth credential; the registration endpoint authenticates
 * the App instance via its attestation, while the challenge and discovery endpoints are anonymous.
 *
 * <h2>Endpoints</h2>
 * <ul>
 *     <li>{@code POST /app_attest/challenge} - generates a one-time challenge</li>
 *     <li>{@code POST /app_attest/register} - validates attestation and registers the App Attest KEY</li>
 *     <li>{@code GET /.well-known/app-attest-configuration} - the discovery document advertising
 *     the two endpoints above</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>
 * http.with(new AppAttestConfigurer(), appAttest -&gt; appAttest
 *     .challengeService(challengeService)
 *     .validationService(validationService)
 *     .providerConfigurationEndpoint(config -&gt; config
 *         .providerConfigurationCustomizer(claims -&gt; claims.put("issuer", issuer)))
 * );
 * </pre>
 *
 * @see ChallengeEndpointFilter
 * @see AppAttestRegistrationEndpointFilter
 * @see AppAttestProviderConfigurationEndpointFilter
 */
public class AppAttestConfigurer
        extends AbstractHttpConfigurer<AppAttestConfigurer, HttpSecurity> {

    private ChallengeService challengeService;
    private AppleAppAttestValidationService validationService;
    private RegisteredAppRepository registeredAppRepository;
    private AppAttestAttestationRegistrationService registrationService;

    private static final String DEFAULT_CHALLENGE_ENDPOINT_URI = "/app_attest/challenge";
    public static final String DEFAULT_REGISTRATION_ENDPOINT_URI = "/app_attest/register";

    private String challengeEndpointUri = DEFAULT_CHALLENGE_ENDPOINT_URI;
    private String registrationEndpointUri = DEFAULT_REGISTRATION_ENDPOINT_URI;

    private final AppAttestProviderConfigurationEndpointConfigurer providerConfigurationEndpointConfigurer;

    private RequestMatcher endpointsMatcher;

    public AppAttestConfigurer() {
        this.providerConfigurationEndpointConfigurer =
                new AppAttestProviderConfigurationEndpointConfigurer(this::postProcess);
    }

    // ---- Fluent API ----

    public AppAttestConfigurer challengeService(ChallengeService challengeService) {
        this.challengeService = challengeService;
        return this;
    }

    public AppAttestConfigurer validationService(AppleAppAttestValidationService validationService) {
        this.validationService = validationService;
        return this;
    }

    public AppAttestConfigurer registeredAppRepository(RegisteredAppRepository registeredAppRepository) {
        this.registeredAppRepository = registeredAppRepository;
        return this;
    }

    public AppAttestConfigurer registrationService(AppAttestAttestationRegistrationService registrationService) {
        this.registrationService = registrationService;
        return this;
    }

    /**
     * No-op. App instance registration no longer resolves or creates users, so a user
     * details service is not used. Retained for API compatibility.
     *
     * @deprecated the registration endpoint registers the App Attest KEY only and
     * creates no user
     */
    @Deprecated
    public AppAttestConfigurer userDetailsService(EulerDeviceUserDetailsService userDetailsService) {
        return this;
    }

    /**
     * No-op. App instance registration no longer provisions users, so a JIT provisioning
     * policy is not used. Retained for API compatibility.
     *
     * @deprecated the registration endpoint registers the App Attest KEY only and
     * creates no user
     */
    @Deprecated
    public AppAttestConfigurer jitProvisioning(JitProvisioningPolicy jitProvisioning) {
        Assert.notNull(jitProvisioning, "jitProvisioning must not be null");
        return this;
    }

    public AppAttestConfigurer challengeEndpointUri(String challengeEndpointUri) {
        this.challengeEndpointUri = challengeEndpointUri;
        return this;
    }

    public AppAttestConfigurer registrationEndpointUri(String registrationEndpointUri) {
        this.registrationEndpointUri = registrationEndpointUri;
        return this;
    }

    /**
     * Configures the App Attest provider configuration (discovery) endpoint.
     *
     * @param providerConfigurationEndpointCustomizer the {@link Customizer} providing access to the
     *                                                {@link AppAttestProviderConfigurationEndpointConfigurer}
     * @return this configurer for chaining
     */
    public AppAttestConfigurer providerConfigurationEndpoint(
            Customizer<AppAttestProviderConfigurationEndpointConfigurer> providerConfigurationEndpointCustomizer) {
        providerConfigurationEndpointCustomizer.customize(this.providerConfigurationEndpointConfigurer);
        return this;
    }

    /**
     * Returns a {@link RequestMatcher} that matches all App Attest endpoints.
     * This can be used externally to configure additional security rules.
     */
    public RequestMatcher getEndpointsMatcher() {
        return (request) -> this.endpointsMatcher != null && this.endpointsMatcher.matches(request);
    }

    @Override
    public void init(HttpSecurity http) {
        // Build endpoint filters to obtain their request matchers
        ChallengeEndpointFilter challengeFilter =
                new ChallengeEndpointFilter(resolveChallengeService(http), this.challengeEndpointUri);
        AppAttestRegistrationEndpointFilter registrationFilter =
                new AppAttestRegistrationEndpointFilter(
                        new AppAttestRegistrationAuthenticationConverter(),
                        createRegistrationProvider(http),
                        this.registrationEndpointUri);

        // The discovery document advertises the two endpoints above, so it needs their final URIs.
        this.providerConfigurationEndpointConfigurer.setChallengeEndpointUri(this.challengeEndpointUri);
        this.providerConfigurationEndpointConfigurer.setRegistrationEndpointUri(this.registrationEndpointUri);
        this.providerConfigurationEndpointConfigurer.init(http);

        this.endpointsMatcher = new OrRequestMatcher(
                challengeFilter.getRequestMatcher(),
                registrationFilter.getRequestMatcher(),
                this.providerConfigurationEndpointConfigurer.getRequestMatcher());

        // Exempt App Attest endpoints from CSRF protection
        http.csrf(csrf -> csrf.ignoringRequestMatchers(this.endpointsMatcher));

        // Store filters as shared objects for configure() to retrieve
        http.setSharedObject(ChallengeEndpointFilter.class, challengeFilter);
        http.setSharedObject(AppAttestRegistrationEndpointFilter.class, registrationFilter);
    }

    @Override
    public void configure(HttpSecurity http) {
        ChallengeEndpointFilter challengeFilter =
                http.getSharedObject(ChallengeEndpointFilter.class);
        AppAttestRegistrationEndpointFilter registrationFilter =
                http.getSharedObject(AppAttestRegistrationEndpointFilter.class);

        http.addFilterBefore(postProcess(challengeFilter), AuthorizationFilter.class);
        http.addFilterBefore(postProcess(registrationFilter), AuthorizationFilter.class);
        this.providerConfigurationEndpointConfigurer.configure(http);
    }

    // ---- Dependency resolution ----

    private AppAttestAttestationRegistrationAuthenticationProvider createRegistrationProvider(HttpSecurity http) {
        return new AppAttestAttestationRegistrationAuthenticationProvider(
                resolveChallengeService(http),
                resolveValidationService(http));
    }

    private ChallengeService resolveChallengeService(HttpSecurity http) {
        if (this.challengeService != null) {
            return this.challengeService;
        }
        ApplicationContext context = http.getSharedObject(ApplicationContext.class);
        return context.getBean(ChallengeService.class);
    }

    private AppleAppAttestValidationService resolveValidationService(HttpSecurity http) {
        if (this.validationService != null) {
            return this.validationService;
        }
        ApplicationContext context = http.getSharedObject(ApplicationContext.class);
        return context.getBean(AppleAppAttestValidationService.class);
    }

    private RegisteredAppRepository resolveAppleAppRepository(HttpSecurity http) {
        if (this.registeredAppRepository != null) {
            return this.registeredAppRepository;
        }
        ApplicationContext context = http.getSharedObject(ApplicationContext.class);
        return context.getBean(RegisteredAppRepository.class);
    }

    private AppAttestAttestationRegistrationService resolveRegistrationService(HttpSecurity http) {
        if (this.registrationService != null) {
            return this.registrationService;
        }
        ApplicationContext context = http.getSharedObject(ApplicationContext.class);
        return context.getBean(AppAttestAttestationRegistrationService.class);
    }
}
