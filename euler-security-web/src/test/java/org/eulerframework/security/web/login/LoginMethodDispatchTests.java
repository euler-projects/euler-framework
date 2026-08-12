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
package org.eulerframework.security.web.login;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Dispatch behaviour of the bundled handlers when every login method
 * submits to the single dispatch entry point: a submission carrying the
 * method's input is routed on, a bare selection is answered with that
 * method's own screen.
 */
class LoginMethodDispatchTests {

    private static final String LOGIN_PAGE = "/signin";
    private static final String LOGIN_PROCESSING_URL = "/doLogin";
    private static final String OTP_LOGIN_PROCESSING_URL = "/login/otp";
    private static final String METHOD_PARAMETER = "_m";

    // ---------- password ----------

    @Test
    void passwordSubmissionIsReplayedToFormLoginPreservingMethod() {
        LoginMethodDispatch dispatch = passwordHandler().dispatch(
                passwordRegistration("password"),
                request(Map.of("username", "alice", "password", "s3cret")));

        assertEquals(LoginMethodDispatch.Action.REDIRECT_307, dispatch.getAction());
        assertEquals(LOGIN_PROCESSING_URL, dispatch.getLocation());
    }

    @Test
    void passwordSelectionWithoutCredentialsReturnsItsOwnScreen() {
        LoginMethodDispatch dispatch = passwordHandler().dispatch(
                passwordRegistration("password"), request(Map.of()));

        assertEquals(LoginMethodDispatch.Action.REDIRECT_302, dispatch.getAction());
        assertEquals("/signin?_m=password", dispatch.getLocation());
    }

    /**
     * A submitted-but-empty form is still a submission: formLogin owns
     * credential validation and reports the failure, so the user is not
     * silently bounced back to an error-free form.
     */
    @Test
    void passwordBlankCredentialsStillCountAsSubmission() {
        LoginMethodDispatch dispatch = passwordHandler().dispatch(
                passwordRegistration("password"),
                request(Map.of("username", "", "password", "")));

        assertEquals(LoginMethodDispatch.Action.REDIRECT_307, dispatch.getAction());
        assertEquals(LOGIN_PROCESSING_URL, dispatch.getLocation());
    }

    // ---------- otp ----------

    @Test
    void otpSelectionWithoutInputReturnsItsCollectionScreen() {
        LoginMethodDispatch dispatch = otpHandler().dispatch(
                otpRegistration("sms"), request(Map.of()));

        assertEquals(LoginMethodDispatch.Action.REDIRECT_302, dispatch.getAction());
        assertEquals("/signin?_m=sms", dispatch.getLocation());
    }

    /**
     * A ticket on its own is incomplete. The redirect is the plain
     * collection entry point: dispatch neither reports which half is
     * missing nor resumes the flow at a later step.
     */
    @Test
    void otpTicketWithoutCodeReturnsTheSamePlainScreen() {
        LoginMethodDispatch dispatch = otpHandler().dispatch(
                otpRegistration("sms"), request(Map.of("otp_ticket", "ot_2b8f4e")));

        assertEquals(LoginMethodDispatch.Action.REDIRECT_302, dispatch.getAction());
        assertEquals("/signin?_m=sms", dispatch.getLocation());
    }

    @Test
    void otpCodeWithoutTicketReturnsTheSamePlainScreen() {
        LoginMethodDispatch dispatch = otpHandler().dispatch(
                otpRegistration("email"), request(Map.of("otp", "123456")));

        assertEquals(LoginMethodDispatch.Action.REDIRECT_302, dispatch.getAction());
        assertEquals("/signin?_m=email", dispatch.getLocation());
    }

    @Test
    void otpBlankInputIsTreatedAsIncomplete() {
        LoginMethodDispatch dispatch = otpHandler().dispatch(
                otpRegistration("sms"), request(Map.of("otp_ticket", "  ", "otp", "")));

        assertEquals(LoginMethodDispatch.Action.REDIRECT_302, dispatch.getAction());
        assertEquals("/signin?_m=sms", dispatch.getLocation());
    }

    /**
     * Ticket plus code is the complete credential, replayed intact to
     * the endpoint that verifies it.
     */
    @Test
    void otpCompleteSubmissionIsReplayedPreservingMethod() {
        LoginMethodDispatch dispatch = otpHandler().dispatch(
                otpRegistration("sms"),
                request(Map.of("otp_ticket", "ot_2b8f4e", "otp", "123456")));

        assertEquals(LoginMethodDispatch.Action.REDIRECT_307, dispatch.getAction());
        assertEquals(OTP_LOGIN_PROCESSING_URL, dispatch.getLocation());
    }

    /**
     * The recipient belongs to the ticket-issuing call, not to the login
     * submission, so it does not make one complete.
     */
    @Test
    void otpRecipientAloneIsNotACredential() {
        LoginMethodDispatch dispatch = otpHandler().dispatch(
                otpRegistration("sms"),
                request(Map.of("recipient", "+8613800000000", "otp", "123456")));

        assertEquals(LoginMethodDispatch.Action.REDIRECT_302, dispatch.getAction());
        assertEquals("/signin?_m=sms", dispatch.getLocation());
    }

    /**
     * The method name is the only thing the redirect carries, and it is
     * encoded: a raw {@code +} would form-decode back as a space.
     */
    @Test
    void otpRedirectEncodesTheMethodName() {
        LoginMethodDispatch dispatch = otpHandler().dispatch(
                otpRegistration("sms channel+1"), request(Map.of()));

        assertEquals("/signin?_m=sms+channel%2B1", dispatch.getLocation());
    }

    // ---------- fixtures ----------

    private static PasswordLoginMethodHandler passwordHandler() {
        return new PasswordLoginMethodHandler(LOGIN_PROCESSING_URL, LOGIN_PAGE, METHOD_PARAMETER);
    }

    private static OtpLoginMethodHandler otpHandler() {
        return new OtpLoginMethodHandler(LOGIN_PAGE, METHOD_PARAMETER, OTP_LOGIN_PROCESSING_URL);
    }

    /**
     * A registration as the contributor hands it to a handler: named,
     * and of the type that handler serves.
     */
    private static RegisteredPasswordLoginMethod passwordRegistration(String name) {
        return new RegisteredPasswordLoginMethod("password", name, false);
    }

    private static RegisteredOtpLoginMethod otpRegistration(String name) {
        return new RegisteredOtpLoginMethod("otp", name, "phone", false, "sms");
    }

    /**
     * Minimal {@link HttpServletRequest} exposing request parameters
     * only; any other call fails loudly so that the handlers stay
     * dependent on nothing else.
     */
    private static HttpServletRequest request(Map<String, String> parameters) {
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if ("getParameter".equals(method.getName())) {
                        return parameters.get((String) args[0]);
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }
}
