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

package org.eulerframework.security.config.annotation.web.configurers.apple;

import org.eulerframework.security.authentication.ChallengeService;
import org.eulerframework.security.authentication.apple.AppAttestRegistrationAuthenticationProvider;
import org.eulerframework.security.authentication.apple.AppleAppAttestValidationService;
import org.eulerframework.security.authentication.apple.AppleAppRepository;
import org.eulerframework.security.authentication.apple.AppAttestRegistrationService;
import org.eulerframework.security.core.userdetails.EulerAppleAppAttestUserDetailsService;
import org.eulerframework.security.web.authentication.apple.AppAttestChallengeEndpointFilter;
import org.eulerframework.security.web.authentication.apple.AppAttestRegistrationAuthenticationConverter;
import org.eulerframework.security.web.authentication.apple.AppAttestRegistrationEndpointFilter;
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * An {@link AbstractHttpConfigurer} for Apple App Attest device registration.
 * <p>
 * This configurer registers the challenge and registration endpoint filters into
 * the default security filter chain. Both endpoints are anonymous (no authentication
 * required) and exempt from CSRF protection.
 *
 * <h2>Endpoints</h2>
 * <ul>
 *     <li>{@code POST /app/attest/challenge} - generates a one-time challenge</li>
 *     <li>{@code POST /app/attest/register} - validates attestation and registers the device</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>
 * http.with(new AppAttestSecurityConfigurer(), appAttest -&gt; appAttest
 *     .challengeService(challengeService)
 *     .appleAppRepository(appleAppRepository)
 *     .registrationService(registrationService)
 *     .userDetailsService(userDetailsService)
 * );
 * </pre>
 *
 * @see AppAttestChallengeEndpointFilter
 * @see AppAttestRegistrationEndpointFilter
 */
public class AppAttestSecurityConfigurer
        extends AbstractHttpConfigurer<AppAttestSecurityConfigurer, HttpSecurity> {

    private ChallengeService challengeService;
    private AppleAppAttestValidationService validationService;
    private AppleAppRepository appleAppRepository;
    private AppAttestRegistrationService registrationService;
    private EulerAppleAppAttestUserDetailsService userDetailsService;

    private String challengeEndpointUri = AppAttestChallengeEndpointFilter.DEFAULT_CHALLENGE_ENDPOINT_URI;
    private String registrationEndpointUri = AppAttestRegistrationEndpointFilter.DEFAULT_REGISTRATION_ENDPOINT_URI;

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

    public AppAttestSecurityConfigurer appleAppRepository(AppleAppRepository appleAppRepository) {
        this.appleAppRepository = appleAppRepository;
        return this;
    }

    public AppAttestSecurityConfigurer registrationService(AppAttestRegistrationService registrationService) {
        this.registrationService = registrationService;
        return this;
    }

    public AppAttestSecurityConfigurer userDetailsService(EulerAppleAppAttestUserDetailsService userDetailsService) {
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
     * Returns a {@link RequestMatcher} that matches all App Attest endpoints.
     * This can be used externally to configure additional security rules.
     */
    public RequestMatcher getEndpointsMatcher() {
        return (request) -> this.endpointsMatcher != null && this.endpointsMatcher.matches(request);
    }

    @Override
    public void init(HttpSecurity http) {
        // Build endpoint filters to obtain their request matchers
        AppAttestChallengeEndpointFilter challengeFilter =
                new AppAttestChallengeEndpointFilter(resolveChallengeService(http), this.challengeEndpointUri);
        AppAttestRegistrationEndpointFilter registrationFilter =
                new AppAttestRegistrationEndpointFilter(
                        new AppAttestRegistrationAuthenticationConverter(),
                        createRegistrationProvider(http),
                        this.registrationEndpointUri);

        this.endpointsMatcher = new OrRequestMatcher(
                challengeFilter.getRequestMatcher(),
                registrationFilter.getRequestMatcher());

        // Exempt App Attest endpoints from CSRF protection
        http.csrf(csrf -> csrf.ignoringRequestMatchers(this.endpointsMatcher));

        // Store filters as shared objects for configure() to retrieve
        http.setSharedObject(AppAttestChallengeEndpointFilter.class, challengeFilter);
        http.setSharedObject(AppAttestRegistrationEndpointFilter.class, registrationFilter);
    }

    @Override
    public void configure(HttpSecurity http) {
        AppAttestChallengeEndpointFilter challengeFilter =
                http.getSharedObject(AppAttestChallengeEndpointFilter.class);
        AppAttestRegistrationEndpointFilter registrationFilter =
                http.getSharedObject(AppAttestRegistrationEndpointFilter.class);

        http.addFilterBefore(postProcess(challengeFilter), AuthorizationFilter.class);
        http.addFilterBefore(postProcess(registrationFilter), AuthorizationFilter.class);
    }

    // ---- Dependency resolution ----

    private AppAttestRegistrationAuthenticationProvider createRegistrationProvider(HttpSecurity http) {
        return new AppAttestRegistrationAuthenticationProvider(
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

    private AppleAppRepository resolveAppleAppRepository(HttpSecurity http) {
        if (this.appleAppRepository != null) {
            return this.appleAppRepository;
        }
        ApplicationContext context = http.getSharedObject(ApplicationContext.class);
        return context.getBean(AppleAppRepository.class);
    }

    private AppAttestRegistrationService resolveRegistrationService(HttpSecurity http) {
        if (this.registrationService != null) {
            return this.registrationService;
        }
        ApplicationContext context = http.getSharedObject(ApplicationContext.class);
        return context.getBean(AppAttestRegistrationService.class);
    }

    private EulerAppleAppAttestUserDetailsService resolveUserDetailsService(HttpSecurity http) {
        if (this.userDetailsService != null) {
            return this.userDetailsService;
        }
        ApplicationContext context = http.getSharedObject(ApplicationContext.class);
        return context.getBean(EulerAppleAppAttestUserDetailsService.class);
    }
}
