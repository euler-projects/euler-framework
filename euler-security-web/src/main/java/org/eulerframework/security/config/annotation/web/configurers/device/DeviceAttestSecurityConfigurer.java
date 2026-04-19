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

package org.eulerframework.security.config.annotation.web.configurers.device;

import org.eulerframework.security.authentication.ChallengeService;
import org.eulerframework.security.authentication.device.apple.AppleAppAttestValidationService;
import org.eulerframework.security.authentication.device.DeviceAttestationRegistrationAuthenticationProvider;
import org.eulerframework.security.authentication.device.DeviceAttestationRegistrationService;
import org.eulerframework.security.authentication.device.DeviceRepository;
import org.eulerframework.security.core.userdetails.EulerDeviceUserDetailsService;
import org.eulerframework.security.web.authentication.ChallengeEndpointFilter;
import org.eulerframework.security.web.authentication.device.DeviceAttestRegistrationAuthenticationConverter;
import org.eulerframework.security.web.authentication.device.DeviceAttestRegistrationEndpointFilter;
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
 *     <li>{@code POST /device/challenge} - generates a one-time challenge</li>
 *     <li>{@code POST /device/attest} - validates attestation and registers the device</li>
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
 * @see DeviceAttestRegistrationEndpointFilter
 */
public class DeviceAttestSecurityConfigurer
        extends AbstractHttpConfigurer<DeviceAttestSecurityConfigurer, HttpSecurity> {

    private ChallengeService challengeService;
    private AppleAppAttestValidationService validationService;
    private DeviceRepository deviceRepository;
    private DeviceAttestationRegistrationService registrationService;
    private EulerDeviceUserDetailsService userDetailsService;

    private static final String DEFAULT_CHALLENGE_ENDPOINT_URI = "/device/challenge";

    private String challengeEndpointUri = DEFAULT_CHALLENGE_ENDPOINT_URI;
    private String registrationEndpointUri = DeviceAttestRegistrationEndpointFilter.DEFAULT_REGISTRATION_ENDPOINT_URI;

    private RequestMatcher endpointsMatcher;

    // ---- Fluent API ----

    public DeviceAttestSecurityConfigurer challengeService(ChallengeService challengeService) {
        this.challengeService = challengeService;
        return this;
    }

    public DeviceAttestSecurityConfigurer validationService(AppleAppAttestValidationService validationService) {
        this.validationService = validationService;
        return this;
    }

    public DeviceAttestSecurityConfigurer appleDeviceRepository(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
        return this;
    }

    public DeviceAttestSecurityConfigurer registrationService(DeviceAttestationRegistrationService registrationService) {
        this.registrationService = registrationService;
        return this;
    }

    public DeviceAttestSecurityConfigurer userDetailsService(EulerDeviceUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
        return this;
    }

    public DeviceAttestSecurityConfigurer challengeEndpointUri(String challengeEndpointUri) {
        this.challengeEndpointUri = challengeEndpointUri;
        return this;
    }

    public DeviceAttestSecurityConfigurer registrationEndpointUri(String registrationEndpointUri) {
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
        DeviceAttestRegistrationEndpointFilter registrationFilter =
                new DeviceAttestRegistrationEndpointFilter(
                        new DeviceAttestRegistrationAuthenticationConverter(),
                        createRegistrationProvider(http),
                        this.registrationEndpointUri);

        this.endpointsMatcher = new OrRequestMatcher(
                challengeFilter.getRequestMatcher(),
                registrationFilter.getRequestMatcher());

        // Exempt device attest endpoints from CSRF protection
        http.csrf(csrf -> csrf.ignoringRequestMatchers(this.endpointsMatcher));

        // Store filters as shared objects for configure() to retrieve
        http.setSharedObject(ChallengeEndpointFilter.class, challengeFilter);
        http.setSharedObject(DeviceAttestRegistrationEndpointFilter.class, registrationFilter);
    }

    @Override
    public void configure(HttpSecurity http) {
        ChallengeEndpointFilter challengeFilter =
                http.getSharedObject(ChallengeEndpointFilter.class);
        DeviceAttestRegistrationEndpointFilter registrationFilter =
                http.getSharedObject(DeviceAttestRegistrationEndpointFilter.class);

        http.addFilterBefore(postProcess(challengeFilter), AuthorizationFilter.class);
        http.addFilterBefore(postProcess(registrationFilter), AuthorizationFilter.class);
    }

    // ---- Dependency resolution ----

    private DeviceAttestationRegistrationAuthenticationProvider createRegistrationProvider(HttpSecurity http) {
        return new DeviceAttestationRegistrationAuthenticationProvider(
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

    private DeviceRepository resolveAppleAppRepository(HttpSecurity http) {
        if (this.deviceRepository != null) {
            return this.deviceRepository;
        }
        ApplicationContext context = http.getSharedObject(ApplicationContext.class);
        return context.getBean(DeviceRepository.class);
    }

    private DeviceAttestationRegistrationService resolveRegistrationService(HttpSecurity http) {
        if (this.registrationService != null) {
            return this.registrationService;
        }
        ApplicationContext context = http.getSharedObject(ApplicationContext.class);
        return context.getBean(DeviceAttestationRegistrationService.class);
    }

    private EulerDeviceUserDetailsService resolveUserDetailsService(HttpSecurity http) {
        if (this.userDetailsService != null) {
            return this.userDetailsService;
        }
        ApplicationContext context = http.getSharedObject(ApplicationContext.class);
        return context.getBean(EulerDeviceUserDetailsService.class);
    }
}
