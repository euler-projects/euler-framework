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

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.eulerframework.security.authentication.ChallengeService;
import org.eulerframework.security.authentication.InMemoryChallengeService;
import org.eulerframework.security.authentication.InMemoryNonceService;
import org.eulerframework.security.authentication.NonceService;
import org.eulerframework.security.authentication.device.DeviceAttestationRegistrationService;
import org.eulerframework.security.authentication.apple.AppleAppAttestValidationService;
import org.eulerframework.security.core.userdetails.EulerDeviceAttestationUserDetailsService;
import org.eulerframework.security.oauth2.server.authorization.authentication.EulerOAuth2ClientAttestationAuthenticationProvider;
import org.eulerframework.security.oauth2.server.authorization.authentication.EulerOAuth2ClientAttestationVerifier;
import org.eulerframework.security.oauth2.server.authorization.web.authentication.EulerOAuth2ClientAttestationAuthenticationConverter;
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2ConfigurerUtilsAccessor;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

/**
 * Utility methods for resolving shared services used by Euler OAuth 2.0 configurers.
 * <p>
 * Resolved instances are cached in {@link HttpSecurity} shared objects to ensure
 * that all configurers and authentication providers within the same security filter chain
 * share the same service instance.
 */
final class EulerOAuth2ConfigurerUtils {

    private EulerOAuth2ConfigurerUtils() {
    }

    @Nonnull
    static EulerOAuth2ClientAttestationAuthenticationConverter getEulerOAuth2ClientAttestationAuthenticationConverter(HttpSecurity http) {
        EulerOAuth2ClientAttestationAuthenticationConverter converter = http.getSharedObject(EulerOAuth2ClientAttestationAuthenticationConverter.class);
        if (converter != null) {
            return converter;
        }
        ApplicationContext applicationContext = http.getSharedObject(ApplicationContext.class);
        if (applicationContext.getBeanNamesForType(EulerOAuth2ClientAttestationAuthenticationConverter.class).length > 0) {
            converter = applicationContext.getBean(EulerOAuth2ClientAttestationAuthenticationConverter.class);
        } else {
            converter = new EulerOAuth2ClientAttestationAuthenticationConverter();
        }
        http.setSharedObject(EulerOAuth2ClientAttestationAuthenticationConverter.class, converter);
        return converter;
    }

    @Nonnull
    public static EulerOAuth2ClientAttestationAuthenticationProvider getEulerOAuth2ClientAttestationAuthenticationProvider(HttpSecurity http) {
        EulerOAuth2ClientAttestationAuthenticationProvider provider = http.getSharedObject(EulerOAuth2ClientAttestationAuthenticationProvider.class);
        if (provider != null) {
            return provider;
        }
        ApplicationContext applicationContext = http.getSharedObject(ApplicationContext.class);
        if (applicationContext.getBeanNamesForType(EulerOAuth2ClientAttestationAuthenticationProvider.class).length > 0) {
            provider = applicationContext.getBean(EulerOAuth2ClientAttestationAuthenticationProvider.class);
        } else {
            ChallengeService challengeService = EulerOAuth2ConfigurerUtils.getChallengeService(http);
            NonceService nonceService = EulerOAuth2ConfigurerUtils.getNonceService(http);
            EulerOAuth2ClientAttestationVerifier oauth2ClientAttestationVerifier =
                    new EulerOAuth2ClientAttestationVerifier(challengeService, nonceService);
            RegisteredClientRepository registeredClientRepository =
                    OAuth2ConfigurerUtilsAccessor.getRegisteredClientRepository(http);
            provider = new EulerOAuth2ClientAttestationAuthenticationProvider(registeredClientRepository, oauth2ClientAttestationVerifier);
        }
        http.setSharedObject(EulerOAuth2ClientAttestationAuthenticationProvider.class, provider);
        return provider;
    }

    /**
     * Resolve a {@link ChallengeService} instance using the following precedence:
     * <ol>
     *     <li>An instance previously cached in {@link HttpSecurity} shared objects</li>
     *     <li>A bean from the {@link ApplicationContext}</li>
     *     <li>A default {@link InMemoryChallengeService} instance</li>
     * </ol>
     * The resolved instance is cached in shared objects for subsequent calls.
     *
     * @param http the {@link HttpSecurity} to resolve from
     * @return the resolved {@link ChallengeService}, never {@code null}
     */
    @Nonnull
    static ChallengeService getChallengeService(HttpSecurity http) {
        ChallengeService service = http.getSharedObject(ChallengeService.class);
        if (service != null) {
            return service;
        }

        ApplicationContext applicationContext = http.getSharedObject(ApplicationContext.class);
        if (applicationContext.getBeanNamesForType(ChallengeService.class).length > 0) {
            service = applicationContext.getBean(ChallengeService.class);
        } else {
            service = new InMemoryChallengeService();
        }
        http.setSharedObject(ChallengeService.class, service);
        return service;
    }

    /**
     * Resolve a {@link NonceService} instance using the following precedence:
     * <ol>
     *     <li>An instance previously cached in {@link HttpSecurity} shared objects</li>
     *     <li>A bean from the {@link ApplicationContext}</li>
     *     <li>A default {@link InMemoryNonceService} instance</li>
     * </ol>
     * The resolved instance is cached in shared objects for subsequent calls.
     *
     * @param http the {@link HttpSecurity} to resolve from
     * @return the resolved {@link NonceService}, never {@code null}
     */
    @Nonnull
    static NonceService getNonceService(HttpSecurity http) {
        NonceService service = http.getSharedObject(NonceService.class);
        if (service != null) {
            return service;
        }

        ApplicationContext applicationContext = http.getSharedObject(ApplicationContext.class);
        if (applicationContext.getBeanNamesForType(NonceService.class).length > 0) {
            service = applicationContext.getBean(NonceService.class);
        } else {
            service = new InMemoryNonceService();
        }
        http.setSharedObject(NonceService.class, service);
        return service;
    }

    /**
     * Resolve an {@link DeviceAttestationRegistrationService} bean from the {@link ApplicationContext}.
     *
     * @param http the {@link HttpSecurity} to resolve from
     * @return the resolved service
     * @throws IllegalStateException if no bean is found
     */
    @Nullable
    static DeviceAttestationRegistrationService getDeviceAttestRegistrationServiceIfAvailable(HttpSecurity http) {
        ApplicationContext ctx = http.getSharedObject(ApplicationContext.class);
        if (ctx.getBeanNamesForType(AppleAppAttestValidationService.class).length > 0) {
            return ctx.getBean(DeviceAttestationRegistrationService.class);
        }
        return null;
    }

    /**
     * Resolve an {@link AppleAppAttestValidationService} bean from the {@link ApplicationContext},
     * or return {@code null} if no such bean is available.
     *
     * @param http the {@link HttpSecurity} to resolve from
     * @return the resolved service, or {@code null} if not available
     */
    @Nullable
    static AppleAppAttestValidationService getAppleAppAttestValidationServiceIfAvailable(HttpSecurity http) {
        ApplicationContext ctx = http.getSharedObject(ApplicationContext.class);
        if (ctx.getBeanNamesForType(AppleAppAttestValidationService.class).length > 0) {
            return ctx.getBean(AppleAppAttestValidationService.class);
        }
        return null;
    }

    /**
     * Resolve an {@link EulerDeviceAttestationUserDetailsService} bean from the {@link ApplicationContext},
     * or return {@code null} if no such bean is available.
     *
     * @param http the {@link HttpSecurity} to resolve from
     * @return the resolved service, or {@code null} if not available
     */
    @Nullable
    static EulerDeviceAttestationUserDetailsService getAppleAppAttestUserDetailsServiceIfAvailable(HttpSecurity http) {
        ApplicationContext ctx = http.getSharedObject(ApplicationContext.class);
        if (ctx.getBeanNamesForType(EulerDeviceAttestationUserDetailsService.class).length > 0) {
            return ctx.getBean(EulerDeviceAttestationUserDetailsService.class);
        }
        return null;
    }
}
