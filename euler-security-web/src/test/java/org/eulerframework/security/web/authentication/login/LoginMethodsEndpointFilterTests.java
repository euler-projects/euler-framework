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
package org.eulerframework.security.web.authentication.login;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.eulerframework.security.web.login.LoginMethod;
import org.eulerframework.security.web.login.LoginMethodService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

/**
 * Pins the endpoint contract of {@link LoginMethodsEndpointFilter}:
 * only the configured GET path is answered, the body is the bare array of
 * offered methods, and assembly failures surface as the shared error
 * envelope rather than a chain continuation.
 */
class LoginMethodsEndpointFilterTests {

    private static final String ENDPOINT = "/login-methods";

    private static LoginMethodService service(List<LoginMethod> methods) {
        return () -> methods;
    }

    private static final class RecordingFilterChain implements FilterChain {
        private boolean called;

        @Override
        public void doFilter(ServletRequest request, ServletResponse response) {
            this.called = true;
        }
    }

    @Test
    void nonMatchingPathPassesThrough() throws Exception {
        LoginMethodsEndpointFilter filter =
                new LoginMethodsEndpointFilter(ENDPOINT, service(List.of()));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/other");
        MockHttpServletResponse response = new MockHttpServletResponse();
        RecordingFilterChain chain = new RecordingFilterChain();

        filter.doFilter(request, response, chain);

        Assertions.assertTrue(chain.called);
        Assertions.assertEquals("", response.getContentAsString());
    }

    @Test
    void nonGetPassesThrough() throws Exception {
        LoginMethodsEndpointFilter filter =
                new LoginMethodsEndpointFilter(ENDPOINT, service(List.of()));
        MockHttpServletRequest request = new MockHttpServletRequest("POST", ENDPOINT);
        MockHttpServletResponse response = new MockHttpServletResponse();
        RecordingFilterChain chain = new RecordingFilterChain();

        filter.doFilter(request, response, chain);

        Assertions.assertTrue(chain.called);
        Assertions.assertEquals("", response.getContentAsString());
    }

    @Test
    void matchingGetWritesTheOfferedMethods() throws Exception {
        List<LoginMethod> methods = List.of(
                LoginMethod.withType("password").name("password").primary(true).build(),
                LoginMethod.withType("oauth2").name("google").attribute("provider", "google").build());
        LoginMethodsEndpointFilter filter =
                new LoginMethodsEndpointFilter(ENDPOINT, service(methods));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", ENDPOINT);
        MockHttpServletResponse response = new MockHttpServletResponse();
        RecordingFilterChain chain = new RecordingFilterChain();

        filter.doFilter(request, response, chain);

        Assertions.assertFalse(chain.called);
        Assertions.assertEquals(200, response.getStatus());
        Assertions.assertEquals("application/json;charset=UTF-8", response.getContentType());
        String body = response.getContentAsString();
        Assertions.assertTrue(body.startsWith("["), body);
        Assertions.assertTrue(body.contains("\"name\":\"password\""), body);
        Assertions.assertTrue(body.contains("\"type\":\"password\""), body);
        Assertions.assertTrue(body.contains("\"primary\":true"), body);
        Assertions.assertTrue(body.contains("\"attributes\":{\"provider\":\"google\"}"), body);
    }

    @Test
    void serviceFailureWritesServerErrorEnvelope() throws Exception {
        LoginMethodService failing = () -> {
            throw new IllegalStateException("boom");
        };
        LoginMethodsEndpointFilter filter =
                new LoginMethodsEndpointFilter(ENDPOINT, failing);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", ENDPOINT);
        MockHttpServletResponse response = new MockHttpServletResponse();
        RecordingFilterChain chain = new RecordingFilterChain();

        filter.doFilter(request, response, chain);

        Assertions.assertFalse(chain.called);
        Assertions.assertEquals(500, response.getStatus());
        String body = response.getContentAsString();
        Assertions.assertTrue(body.contains("\"error\":\"server_error\""), body);
        Assertions.assertTrue(body.contains("boom"), body);
    }
}
