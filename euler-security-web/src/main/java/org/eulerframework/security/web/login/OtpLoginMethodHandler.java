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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * {@link LoginMethodHandler} for {@code type: otp}: one-time-password
 * login via SMS, email, or voice channel.
 *
 * <p>OTP login takes two requests, of which only the second is a login
 * submission:
 * <ol>
 *   <li>the client asks the OTP issue endpoint
 *       ({@code POST /otp/tickets}, {@code channel} + {@code recipient})
 *       for a ticket. It answers with an {@code otp_ticket} while the
 *       code itself goes out of band to the recipient. This is an API
 *       call, not a form post, and no login method is involved.</li>
 *   <li>the client submits the {@code otp_ticket} it holds together
 *       with the {@code otp} the user typed in. That pair is this
 *       method's credential, and forwarding it is what this handler
 *       does.</li>
 * </ol>
 *
 * <p>Dispatch judges the submission as a whole: ticket plus code is
 * complete and gets replayed to the OTP login endpoint, anything less
 * returns this method's collection screen unchanged. It deliberately
 * does not work out which half is missing or resume a partly filled
 * flow; driving the user through the fields is the client's job.
 *
 * <p>Channel resolution: the registration's explicit
 * {@link RegisteredOtpLoginMethod#getChannel() channel}, else a
 * canonical mapping from {@code identity-type}
 * ({@code phone → sms}, {@code email → email}). If neither resolves,
 * the method is skipped with a WARN log.
 */
public class OtpLoginMethodHandler implements LoginMethodHandler {

    private static final String TYPE = RegisteredOtpLoginMethod.TYPE;

    /**
     * Attribute key the resolved delivery channel is published under.
     */
    public static final String ATTR_CHANNEL = "channel";

    /** Ticket handle issued by the OTP issue endpoint. */
    private static final String PARAM_OTP_TICKET = "otp_ticket";

    /** The one-time password the user read out of band. */
    private static final String PARAM_OTP = "otp";

    /**
     * Where a complete submission is replayed.
     *
     * <p>TODO placeholder: the OTP login endpoint does not exist yet.
     * Once it does, this should become a configurable endpoint, the way
     * the formLogin processing URL already is.
     */
    private static final String OTP_LOGIN_PROCESSING_URL = "/otp/login";

    private static final Map<String, String> CANONICAL_CHANNEL_MAP = Map.of(
            "phone", "sms",
            "email", "email"
    );

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String loginPageUrl;
    private final String methodParameter;

    /**
     * @param loginPageUrl    the login page GET URL
     * @param methodParameter the dispatch method parameter name
     */
    public OtpLoginMethodHandler(String loginPageUrl, String methodParameter) {
        Assert.hasText(loginPageUrl, "loginPageUrl is required");
        Assert.hasText(methodParameter, "methodParameter is required");
        this.loginPageUrl = loginPageUrl;
        this.methodParameter = methodParameter;
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public LoginMethod describe(RegisteredLoginMethod method) {
        String channel = resolveChannel((RegisteredOtpLoginMethod) method);
        if (channel == null) {
            this.logger.warn("Login method '{}' (type=otp) has no resolvable channel: "
                    + "neither an explicit channel nor a canonical mapping for identity-type='{}' exists; skipping.",
                    method.getName(), method.getIdentityType());
            return null;
        }
        return LoginMethod.withType(TYPE)
                .name(method.getName())
                .primary(method.isPrimary())
                .attribute(ATTR_CHANNEL, channel)
                .build();
    }

    @Override
    public LoginMethodDispatch dispatch(RegisteredLoginMethod method, HttpServletRequest request) {
        if (StringUtils.hasText(request.getParameter(PARAM_OTP_TICKET))
                && StringUtils.hasText(request.getParameter(PARAM_OTP))) {
            // Ticket and code both rode along, so hand this very POST to
            // the endpoint that verifies them.
            return LoginMethodDispatch.redirectPreservingMethod(OTP_LOGIN_PROCESSING_URL);
        }
        // Incomplete: return this method's collection screen and let the
        // client obtain a ticket and gather the code. Which half is
        // missing is the client's business, not the dispatcher's.
        return LoginMethodDispatch.redirect(this.loginPageUrl + "?" + this.methodParameter
                + "=" + URLEncoder.encode(method.getName(), StandardCharsets.UTF_8));
    }

    private String resolveChannel(RegisteredOtpLoginMethod method) {
        if (StringUtils.hasText(method.getChannel())) {
            return method.getChannel();
        }
        String identityType = method.getIdentityType();
        if (identityType != null) {
            return CANONICAL_CHANNEL_MAP.get(identityType.toLowerCase());
        }
        return null;
    }
}
