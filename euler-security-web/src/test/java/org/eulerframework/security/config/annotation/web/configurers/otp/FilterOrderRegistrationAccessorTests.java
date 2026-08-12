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
package org.eulerframework.security.config.annotation.web.configurers.otp;

import org.eulerframework.security.web.authentication.otp.OneTimePasswordAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.FilterOrderRegistrationAccessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.ott.OneTimeTokenAuthenticationFilter;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilterOrderRegistrationAccessorTests {

    @Test
    void registeredFilterCanBeAddedThroughAddFilter() {
        HttpSecurity http = newHttpSecurity();

        FilterOrderRegistrationAccessor.register(http,
                OneTimePasswordAuthenticationFilter.class,
                OneTimeTokenAuthenticationFilter.class, 1);

        assertDoesNotThrow(() -> http.addFilter(new OneTimePasswordAuthenticationFilter()));
    }

    @Test
    void unregisteredFilterIsStillRejectedByAddFilter() {
        HttpSecurity http = newHttpSecurity();

        assertThrows(IllegalArgumentException.class,
                () -> http.addFilter(new OneTimePasswordAuthenticationFilter()));
    }

    private static HttpSecurity newHttpSecurity() {
        ObjectPostProcessor<Object> postProcessor = new ObjectPostProcessor<>() {
            @Override
            public <T> T postProcess(T object) {
                return object;
            }
        };
        return new HttpSecurity(postProcessor,
                new AuthenticationManagerBuilder(postProcessor), new HashMap<>());
    }
}
