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
package org.springframework.security.oauth2.server.authorization.web.authentication;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.MultiValueMap;

import java.util.Map;

/**
 * Exposes the package-private {@link OAuth2EndpointUtils} to code outside
 * {@code org.springframework.security.oauth2.server.authorization.web.authentication}, so that the
 * parameter collection Spring's own client authentication converters use can be reused from another
 * package instead of being reimplemented &mdash; a reimplementation silently drops every parameter it
 * did not think to name.
 *
 * @see OAuth2EndpointUtils
 * @see org.springframework.security.oauth2.server.authorization.authentication.CodeVerifierAuthenticatorAccessor
 */
public final class OAuth2EndpointUtilsAccessor {

    /**
     * The request's form parameters, with the body read only once however many times they are asked
     * for.
     *
     * @param request the token endpoint request
     * @return the form parameters, never {@code null}
     */
    public static MultiValueMap<String, String> getFormParameters(HttpServletRequest request) {
        return OAuth2EndpointUtils.getFormParameters(request);
    }

    /**
     * The request's parameters when it is an {@code authorization_code} grant request, minus the
     * given exclusions, and an empty map otherwise. Single-valued parameters are flattened to their
     * value and multi-valued ones to a {@code String[]}, exactly as Spring's converters leave them.
     *
     * @param request    the token endpoint request
     * @param exclusions parameter names to leave out, typically the credentials already consumed
     * @return the collected parameters, never {@code null}
     */
    public static Map<String, Object> getParametersIfMatchesAuthorizationCodeGrantRequest(
            HttpServletRequest request, String... exclusions) {
        return OAuth2EndpointUtils.getParametersIfMatchesAuthorizationCodeGrantRequest(request, exclusions);
    }

    private OAuth2EndpointUtilsAccessor() {
    }
}
