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
import jakarta.servlet.http.HttpServletRequest;
import org.eulerframework.security.web.login.DefaultLoginMethodService;
import org.eulerframework.security.web.login.DefaultRegisteredLoginMethodRepository;
import org.eulerframework.security.web.login.LoginMethod;
import org.eulerframework.security.web.login.LoginMethodDispatch;
import org.eulerframework.security.web.login.LoginMethodHandler;
import org.eulerframework.security.web.login.RegisteredLoginMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

/**
 * Pins the dispatch endpoint's content negotiation: JSON clients receive
 * the incomplete-submission redirect as a {@code redirect_url} envelope
 * and unknown methods as an error envelope, while the 307 replay and the
 * plain-form redirects stay untouched.
 */
class LoginMethodDispatchFilterTests {

    private static final String DISPATCH_URL = "/login";
    private static final String LOGIN_PAGE_URL = "/signin";
    private static final String METHOD_PARAMETER = "_m";

    private static final class StubRegistration extends RegisteredLoginMethod {
        StubRegistration(String type, String id, String name) {
            super(type, id, name, null, false);
        }
    }

    private static final class FixedDispatchHandler implements LoginMethodHandler {
        private final String type;
        private final LoginMethodDispatch dispatch;

        FixedDispatchHandler(String type, LoginMethodDispatch dispatch) {
            this.type = type;
            this.dispatch = dispatch;
        }

        @Override
        public String type() {
            return this.type;
        }

        @Override
        public LoginMethod describe(RegisteredLoginMethod method) {
            return LoginMethod.withType(this.type).name(method.getName()).build();
        }

        @Override
        public LoginMethodDispatch dispatch(RegisteredLoginMethod method, HttpServletRequest request) {
            return this.dispatch;
        }
    }

    private static final class RecordingFilterChain implements FilterChain {
        private boolean called;

        @Override
        public void doFilter(ServletRequest request, ServletResponse response) {
            this.called = true;
        }
    }

    private static LoginMethodDispatchFilter filter(LoginMethodDispatch dispatch) {
        FixedDispatchHandler handler = new FixedDispatchHandler("password", dispatch);
        DefaultLoginMethodService service = new DefaultLoginMethodService(
                List.of(handler),
                new DefaultRegisteredLoginMethodRepository(new StubRegistration("password", "uuid-1", "password")));
        return new LoginMethodDispatchFilter(DISPATCH_URL, LOGIN_PAGE_URL, METHOD_PARAMETER, service);
    }

    private static MockHttpServletRequest post(String method, String accept) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", DISPATCH_URL);
        if (method != null) {
            request.setParameter(METHOD_PARAMETER, method);
        }
        if (accept != null) {
            request.addHeader(HttpHeaders.ACCEPT, accept);
        }
        return request;
    }

    @Test
    void jsonClientGetsRedirectEnvelopeForIncompleteSubmission() throws Exception {
        LoginMethodDispatchFilter filter = filter(LoginMethodDispatch.redirect(LOGIN_PAGE_URL + "?_m=password"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(post("password", MediaType.APPLICATION_JSON_VALUE), response, new RecordingFilterChain());

        Assertions.assertEquals(200, response.getStatus());
        Assertions.assertTrue(response.getContentAsString().contains(
                "\"redirect_url\":\"" + LOGIN_PAGE_URL + "?_m=password\""), response.getContentAsString());
    }

    @Test
    void formClientStillGetsThe302() throws Exception {
        LoginMethodDispatchFilter filter = filter(LoginMethodDispatch.redirect(LOGIN_PAGE_URL + "?_m=password"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(post("password", null), response, new RecordingFilterChain());

        Assertions.assertEquals(302, response.getStatus());
        Assertions.assertEquals(LOGIN_PAGE_URL + "?_m=password", response.getHeader("Location"));
    }

    @Test
    void jsonClientStillGetsThe307Replay() throws Exception {
        LoginMethodDispatchFilter filter = filter(LoginMethodDispatch.redirectPreservingMethod("/doLogin"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(post("password", MediaType.APPLICATION_JSON_VALUE), response, new RecordingFilterChain());

        Assertions.assertEquals(307, response.getStatus());
        Assertions.assertEquals("/doLogin", response.getHeader("Location"));
        Assertions.assertEquals("", response.getContentAsString());
    }

    @Test
    void jsonClientGetsErrorEnvelopeForUnknownMethod() throws Exception {
        LoginMethodDispatchFilter filter = filter(LoginMethodDispatch.redirect("/unused"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(post("nope", MediaType.APPLICATION_JSON_VALUE), response, new RecordingFilterChain());

        Assertions.assertEquals(400, response.getStatus());
        Assertions.assertTrue(response.getContentAsString().contains("\"error\":\"invalid_request\""),
                response.getContentAsString());
    }

    @Test
    void formClientStillRedirectsForUnknownMethod() throws Exception {
        LoginMethodDispatchFilter filter = filter(LoginMethodDispatch.redirect("/unused"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(post("nope", null), response, new RecordingFilterChain());

        Assertions.assertEquals(302, response.getStatus());
        Assertions.assertEquals(LOGIN_PAGE_URL + "?error", response.getHeader("Location"));
    }

    @Test
    void missingMethodParameterPassesThrough() throws Exception {
        LoginMethodDispatchFilter filter = filter(LoginMethodDispatch.redirect("/unused"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        RecordingFilterChain chain = new RecordingFilterChain();

        filter.doFilter(post(null, MediaType.APPLICATION_JSON_VALUE), response, chain);

        Assertions.assertTrue(chain.called);
        Assertions.assertEquals("", response.getContentAsString());
    }
}
