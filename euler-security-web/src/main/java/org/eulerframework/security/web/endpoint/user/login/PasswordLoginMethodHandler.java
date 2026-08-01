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
package org.eulerframework.security.web.endpoint.user.login;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.Assert;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * {@link LoginMethodHandler} for {@code type: password}: the traditional
 * username/password form login.
 *
 * <p>Dispatch keys off whether the request already carries the
 * credentials. That is what lets a page rendering this method's form
 * inline submit it in a single hop, instead of paying for a
 * redirect &rarr; render &rarr; resubmit detour:
 * <ul>
 *   <li><b>credentials present &mdash; the caller is submitting.</b>
 *       Answer 307 Temporary Redirect to the formLogin processing URL:
 *       method and body survive the redirect, so
 *       {@code UsernamePasswordAuthenticationFilter} consumes the
 *       original POST and the user is never sent back to a form they
 *       have already filled in. Blank values still count as a
 *       submission &mdash; validating them belongs to formLogin, whose
 *       failure handler reports the error the user expects.</li>
 *   <li><b>credentials absent &mdash; the caller is only selecting this
 *       method</b> (typically an alternate-method button on a page
 *       showing some other method's form). There is nothing to forward,
 *       so answer 302 Found to the login page with this method
 *       selected, which is where its form gets rendered.</li>
 * </ul>
 *
 * <p>Either hop is a convenience of the shared dispatch endpoint, not a
 * requirement: clients that post credentials straight to the formLogin
 * processing URL, or link straight to the login page, bypass this
 * handler entirely.
 */
public class PasswordLoginMethodHandler implements LoginMethodHandler {

    /** The {@code type} value served by this handler. */
    public static final String TYPE = "password";

    private final String loginProcessingUrl;
    private final String loginPageUrl;
    private final String methodParameter;

    /**
     * @param loginProcessingUrl the formLogin processing URL
     *                           ({@code POST {login-processing-url}})
     * @param loginPageUrl       the login page GET URL
     * @param methodParameter    the dispatch method parameter name
     */
    public PasswordLoginMethodHandler(String loginProcessingUrl,
                                      String loginPageUrl,
                                      String methodParameter) {
        Assert.hasText(loginProcessingUrl, "loginProcessingUrl is required");
        Assert.hasText(loginPageUrl, "loginPageUrl is required");
        Assert.hasText(methodParameter, "methodParameter is required");
        this.loginProcessingUrl = loginProcessingUrl;
        this.loginPageUrl = loginPageUrl;
        this.methodParameter = methodParameter;
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public LoginMethod describe(String name, RegisteredLoginMethod method) {
        return LoginMethod.withType(TYPE)
                .name(name)
                .primary(method.isPrimary())
                .build();
    }

    @Override
    public LoginMethodDispatch dispatch(String name, RegisteredLoginMethod method, HttpServletRequest request) {
        if (request.getParameter(
                UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY) != null) {
            // The credentials rode along, so hand this very POST over to
            // formLogin and spare the user a detour through a form they
            // have already filled in.
            return LoginMethodDispatch.redirectPreservingMethod(this.loginProcessingUrl);
        }
        // Nothing to forward: this is a bare selection of the method, so
        // send the caller to the screen that collects the credentials.
        return LoginMethodDispatch.redirect(this.loginPageUrl + "?" + this.methodParameter
                + "=" + URLEncoder.encode(name, StandardCharsets.UTF_8));
    }
}
