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
package org.eulerframework.security.web.endpoint.user;

import jakarta.servlet.http.HttpServletRequest;
import org.eulerframework.security.web.endpoint.EulerSecurityEndpoints;
import org.eulerframework.security.web.login.LoginMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import java.lang.reflect.Proxy;
import java.util.List;

/**
 * The built-in login page's display list: which methods it shows, in
 * which order, and how loudly it rejects a list the declarations cannot
 * back. The list is a presentation concern of the page alone, so these
 * tests also pin that the service's own offering is left untouched.
 */
public class EulerSecurityUserPageControllerTests {

    /** The dispatch method parameter, as the controller's default has it. */
    private static final String METHOD_PARAMETER =
            EulerSecurityEndpoints.LOGIN_METHODS_DISPATCH_METHOD_PARAMETER;

    /**
     * Deliberately not the order any configuration file would suggest:
     * whatever reaches the controller here is what the registry produced.
     */
    private static final List<LoginMethod> OFFERED = List.of(
            LoginMethod.withType("password").name("password").build(),
            LoginMethod.withType("oauth2").name("google").attribute("provider", "google").build(),
            LoginMethod.withType("otp").name("email").attribute("channel", "email").build());

    private static EulerSecurityUserPageController controller(List<String> loginPageMethods) {
        return controller(loginPageMethods, OFFERED);
    }

    private static EulerSecurityUserPageController controller(List<String> loginPageMethods,
                                                              List<LoginMethod> offered) {
        EulerSecurityUserPageController controller = new EulerSecurityUserPageController(
                (view, theme, attributes) -> new ModelAndView(view),
                () -> offered);
        controller.setLoginMethodParameter(METHOD_PARAMETER);
        if (loginPageMethods != null) {
            controller.setLoginPageMethods(loginPageMethods);
        }
        return controller;
    }

    /**
     * The controller reads one thing off the request - the selected method
     * parameter - so a proxy answering just that is enough, and spares the
     * module a test-only servlet mock dependency.
     */
    private static HttpServletRequest requestSelecting(String method) {
        return (HttpServletRequest) Proxy.newProxyInstance(
                EulerSecurityUserPageControllerTests.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, invoked, args) -> {
                    if ("getParameter".equals(invoked.getName())) {
                        return METHOD_PARAMETER.equals(args[0]) ? method : null;
                    }
                    throw new UnsupportedOperationException(invoked.getName());
                });
    }

    private static List<String> primary(EulerSecurityUserPageController controller, String selected) {
        return names(controller.getPrimaryLoginMethods(requestSelecting(selected)));
    }

    private static List<String> secondary(EulerSecurityUserPageController controller, String selected) {
        return names(controller.getSecondaryLoginMethods(requestSelecting(selected)));
    }

    private static List<String> names(List<LoginMethod> methods) {
        return methods.stream().map(LoginMethod::getName).toList();
    }

    @Test
    void withoutADisplayListTheServiceOrderStands() {
        // Nobody calls the setter at all: the page falls back to offering
        // everything the service serves, in the service's own order.
        EulerSecurityUserPageController controller = controller(null);

        Assertions.assertEquals(List.of("password"), primary(controller, null));
        Assertions.assertEquals(List.of("google", "email"), secondary(controller, null));
    }

    @Test
    void anEmptyDisplayListAlsoOffersEverything() {
        EulerSecurityUserPageController controller = controller(List.of());

        Assertions.assertEquals(List.of("password"), primary(controller, null));
        Assertions.assertEquals(List.of("google", "email"), secondary(controller, null));
    }

    @Test
    void aNullDisplayListAlsoOffersEverything() {
        EulerSecurityUserPageController controller = controller(null);
        controller.setLoginPageMethods(null);

        Assertions.assertEquals(List.of("password"), primary(controller, null));
        Assertions.assertEquals(List.of("google", "email"), secondary(controller, null));
    }

    @Test
    void theDisplayListReordersAndHides() {
        EulerSecurityUserPageController controller = controller(List.of("email", "google"));

        // The first listed method is the one expanded, and a method the
        // list omits is absent from the page altogether.
        Assertions.assertEquals(List.of("email"), primary(controller, null));
        Assertions.assertEquals(List.of("google"), secondary(controller, null));
    }

    @Test
    void aDeclaredPrimaryStaysPrimaryWithinTheDisplayList() {
        List<LoginMethod> offered = List.of(
                LoginMethod.withType("password").name("password").build(),
                LoginMethod.withType("oauth2").name("google").primary(true).build(),
                LoginMethod.withType("otp").name("email").build());
        EulerSecurityUserPageController controller = controller(List.of("email", "google"), offered);

        // Listing email first changes where the methods sit, not which of
        // them declared itself primary.
        Assertions.assertEquals(List.of("google"), primary(controller, null));
        Assertions.assertEquals(List.of("email"), secondary(controller, null));
    }

    @Test
    void selectingADisplayedMethodExpandsIt() {
        EulerSecurityUserPageController controller = controller(List.of("email", "google"));

        Assertions.assertEquals(List.of("google"), primary(controller, "google"));
        Assertions.assertEquals(List.of("email"), secondary(controller, "google"));
    }

    @Test
    void selectingAHiddenMethodYieldsNoPrimaryGroup() {
        EulerSecurityUserPageController controller = controller(List.of("email", "google"));

        // The selection addresses a method the page does not show, so
        // nothing is expanded and the listed methods render as buttons.
        Assertions.assertEquals(List.of(), primary(controller, "password"));
        Assertions.assertEquals(List.of("email", "google"), secondary(controller, "password"));
    }

    @Test
    void anUnknownNameFailsLoudly() {
        EulerSecurityUserPageController controller = controller(List.of("email", "webauthn"));

        IllegalStateException failure = Assertions.assertThrows(IllegalStateException.class,
                () -> primary(controller, null));
        Assertions.assertTrue(failure.getMessage().contains("webauthn"), failure.getMessage());
        Assertions.assertTrue(
                failure.getMessage().contains(EulerSecurityEndpoints.USER_LOGIN_PAGE_METHODS_PROP_NAME),
                failure.getMessage());
    }

    @Test
    void aRepeatedNameFailsLoudly() {
        EulerSecurityUserPageController controller = controller(List.of("email", "email"));

        IllegalStateException failure = Assertions.assertThrows(IllegalStateException.class,
                () -> secondary(controller, null));
        Assertions.assertTrue(failure.getMessage().contains("more than once"), failure.getMessage());
    }
}
