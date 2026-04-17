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

import org.eulerframework.security.authentication.ChallengeService;
import org.eulerframework.security.authentication.InMemoryChallengeService;
import org.eulerframework.security.authentication.InMemoryNonceService;
import org.eulerframework.security.authentication.NonceService;
import org.eulerframework.security.authentication.apple.AppAttestRegistrationService;
import org.eulerframework.security.authentication.apple.AppleAppAttestValidationService;
import org.eulerframework.security.core.userdetails.EulerAppleAppAttestUserDetailsService;
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

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
     * Resolve an {@link AppAttestRegistrationService} bean from the {@link ApplicationContext}.
     *
     * @param http the {@link HttpSecurity} to resolve from
     * @return the resolved service
     * @throws IllegalStateException if no bean is found
     */
    static AppAttestRegistrationService getAppAttestRegistrationService(HttpSecurity http) {
        ApplicationContext ctx = http.getSharedObject(ApplicationContext.class);
        return ctx.getBean(AppAttestRegistrationService.class);
    }

    /**
     * Resolve an {@link AppleAppAttestValidationService} bean from the {@link ApplicationContext}.
     *
     * @param http the {@link HttpSecurity} to resolve from
     * @return the resolved service
     * @throws IllegalStateException if no bean is found
     */
    static AppleAppAttestValidationService getAppleAppAttestValidationService(HttpSecurity http) {
        ApplicationContext ctx = http.getSharedObject(ApplicationContext.class);
        return ctx.getBean(AppleAppAttestValidationService.class);
    }

    /**
     * Resolve an {@link EulerAppleAppAttestUserDetailsService} bean from the {@link ApplicationContext}.
     *
     * @param http the {@link HttpSecurity} to resolve from
     * @return the resolved service
     * @throws IllegalStateException if no bean is found
     */
    static EulerAppleAppAttestUserDetailsService getAppleAppAttestUserDetailsService(HttpSecurity http) {
        ApplicationContext ctx = http.getSharedObject(ApplicationContext.class);
        return ctx.getBean(EulerAppleAppAttestUserDetailsService.class);
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
}
