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
import org.eulerframework.security.web.authentication.ChallengeEndpointFilter;
import org.eulerframework.security.web.authentication.appattest.AppAttestRegistrationAuthenticationConverter;
import org.eulerframework.security.web.authentication.appattest.AppAttestRegistrationEndpointFilter;
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * An {@link AbstractHttpConfigurer} for device attestation registration.
 * <p>
 * This configurer registers the challenge and registration endpoint filters into
 * the default security filter chain. Both endpoints are anonymous (no authentication
 * required) and exempt from CSRF protection.
 *
 * <h2>Endpoints</h2>
 * <ul>
 *     <li>{@code POST /app_attest/challenge} - generates a one-time challenge</li>
 *     <li>{@code POST /app_attest/register} - validates attestation and registers the device</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>
 * http.with(new DeviceAttestSecurityConfigurer(), deviceAttest -&gt; deviceAttest
 *     .challengeService(challengeService)
 *     .appleAppRepository(appleAppRepository)
 *     .registrationService(registrationService)
 *     .userDetailsService(userDetailsService)
 * );
 * </pre>
 *
 * @see ChallengeEndpointFilter
 * @see AppAttestRegistrationEndpointFilter
 */
public class AppAttestSecurityConfigurer
        extends AbstractHttpConfigurer<AppAttestSecurityConfigurer, HttpSecurity> {

    private ChallengeService challengeService;
    private AppleAppAttestValidationService validationService;
    private RegisteredAppRepository registeredAppRepository;
    private AppAttestAttestationRegistrationService registrationService;
    private EulerDeviceUserDetailsService userDetailsService;

    private static final String DEFAULT_CHALLENGE_ENDPOINT_URI = "/app_attest/challenge";
    public static final String DEFAULT_REGISTRATION_ENDPOINT_URI = "/app_attest/register";

    private String challengeEndpointUri = DEFAULT_CHALLENGE_ENDPOINT_URI;
    private String registrationEndpointUri = DEFAULT_REGISTRATION_ENDPOINT_URI;

    private RequestMatcher endpointsMatcher;

    // ---- Fluent API ----

    public AppAttestSecurityConfigurer challengeService(ChallengeService challengeService) {
        this.challengeService = challengeService;
        return this;
    }

    public AppAttestSecurityConfigurer validationService(AppleAppAttestValidationService validationService) {
        this.validationService = validationService;
        return this;
    }

    public AppAttestSecurityConfigurer appleDeviceRepository(RegisteredAppRepository registeredAppRepository) {
        this.registeredAppRepository = registeredAppRepository;
        return this;
    }

    public AppAttestSecurityConfigurer registrationService(AppAttestAttestationRegistrationService registrationService) {
        this.registrationService = registrationService;
        return this;
    }

    public AppAttestSecurityConfigurer userDetailsService(EulerDeviceUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
        return this;
    }

    public AppAttestSecurityConfigurer challengeEndpointUri(String challengeEndpointUri) {
        this.challengeEndpointUri = challengeEndpointUri;
        return this;
    }

    public AppAttestSecurityConfigurer registrationEndpointUri(String registrationEndpointUri) {
        this.registrationEndpointUri = registrationEndpointUri;
        return this;
    }

    /**
     * Returns a {@link RequestMatcher} that matches all device attest endpoints.
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

        this.endpointsMatcher = new OrRequestMatcher(
                challengeFilter.getRequestMatcher(),
                registrationFilter.getRequestMatcher());

        // Exempt device attest endpoints from CSRF protection
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
    }

    // ---- Dependency resolution ----

    private AppAttestAttestationRegistrationAuthenticationProvider createRegistrationProvider(HttpSecurity http) {
        return new AppAttestAttestationRegistrationAuthenticationProvider(
                resolveChallengeService(http),
                resolveValidationService(http),
                resolveUserDetailsService(http));
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

    private EulerDeviceUserDetailsService resolveUserDetailsService(HttpSecurity http) {
        if (this.userDetailsService != null) {
            return this.userDetailsService;
        }
        ApplicationContext context = http.getSharedObject(ApplicationContext.class);
        return context.getBean(EulerDeviceUserDetailsService.class);
    }
}
