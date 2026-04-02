/*
 * Copyright 2013-2026 the original author or authors.
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
package org.eulerframework.security.oauth2.server.authorization.config.annotation.web.configurers;

import org.eulerframework.security.authentication.ChallengeService;
import org.eulerframework.security.authentication.InMemoryChallengeService;
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
}
