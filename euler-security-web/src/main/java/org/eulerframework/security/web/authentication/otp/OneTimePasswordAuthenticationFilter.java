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

import org.springframework.http.HttpMethod;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

/**
 * Filter that processes a one-time-password submission for log in.
 * <p>
 * By default, it uses {@link OneTimePasswordAuthenticationConverter} to
 * extract the {@code otp_ticket} and {@code otp} parameters from the request.
 */
public final class OneTimePasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    public static final String DEFAULT_LOGIN_PROCESSING_URL = "/login/otp";

    public OneTimePasswordAuthenticationFilter() {
        super(PathPatternRequestMatcher.pathPattern(HttpMethod.POST, DEFAULT_LOGIN_PROCESSING_URL));
        setAuthenticationConverter(new OneTimePasswordAuthenticationConverter());
    }
}
