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

package org.eulerframework.security.web.authentication.otp;

import jakarta.servlet.http.HttpServletRequest;
import org.eulerframework.security.authentication.otp.OneTimePasswordAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.StringUtils;

/**
 * An implementation of {@link AuthenticationConverter} that detects whether
 * the request carries both the {@code otp_ticket} and the {@code otp}
 * parameters and constructs a {@link OneTimePasswordAuthenticationToken}
 * from them.
 * <p>
 * Returns {@code null} when either parameter is absent so that unrelated
 * requests fall through to the rest of the filter chain.
 *
 * @see OneTimePasswordAuthenticationFilter
 */
public class OneTimePasswordAuthenticationConverter implements AuthenticationConverter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** Ticket handle issued by the OTP issue endpoint. */
    public static final String PARAM_OTP_TICKET = "otp_ticket";

    /** The one-time password the user read out of band. */
    public static final String PARAM_OTP = "otp";

    @Override
    public Authentication convert(HttpServletRequest request) {
        String otpTicket = request.getParameter(PARAM_OTP_TICKET);
        String otp = request.getParameter(PARAM_OTP);
        if (!StringUtils.hasText(otpTicket) || !StringUtils.hasText(otp)) {
            this.logger.debug("Incomplete OTP login submission: otp_ticket and otp are both required");
            return null;
        }
        return OneTimePasswordAuthenticationToken.unauthenticated(otpTicket, otp);
    }
}
