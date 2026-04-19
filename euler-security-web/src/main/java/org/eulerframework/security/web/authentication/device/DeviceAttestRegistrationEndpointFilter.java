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

package org.eulerframework.security.web.authentication.device;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eulerframework.common.util.jackson.JacksonUtils;
import org.eulerframework.security.authentication.device.DeviceAttestationRegistrationAuthenticationProvider;
import org.eulerframework.security.authentication.device.DeviceAttestationRegistrationAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * A filter that exposes a {@code POST /device/attest} endpoint for device attestation
 * device registration (attestation).
 * <p>
 * This endpoint is anonymous (no authentication required). The filter uses an
 * {@link AuthenticationConverter} to extract registration parameters from the request
 * and delegates to an {@link AuthenticationProvider} for attestation validation,
 * device registration, and user creation.
 * <p>
 * Request parameters:
 * <ul>
 *     <li>{@code key_id} - the key identifier from DCAppAttestService</li>
 *     <li>{@code attestation} - the Base64-encoded attestation object</li>
 *     <li>{@code challenge} - the challenge value obtained from the challenge endpoint</li>
 * </ul>
 * <p>
 * Success response (HTTP 200):
 * <pre>
 * {"key_id": "...", "username": "apple_app_..."}
 * </pre>
 *
 * @see DeviceAttestRegistrationAuthenticationConverter
 * @see DeviceAttestationRegistrationAuthenticationProvider
 */
public class DeviceAttestRegistrationEndpointFilter extends OncePerRequestFilter {

    public static final String DEFAULT_REGISTRATION_ENDPOINT_URI = "/device/attest";

    private static final Logger logger = LoggerFactory.getLogger(DeviceAttestRegistrationEndpointFilter.class);

    private final AuthenticationConverter authenticationConverter;
    private final AuthenticationProvider authenticationProvider;
    private final RequestMatcher requestMatcher;

    public DeviceAttestRegistrationEndpointFilter(AuthenticationConverter authenticationConverter,
                                               AuthenticationProvider authenticationProvider) {
        this(authenticationConverter, authenticationProvider, DEFAULT_REGISTRATION_ENDPOINT_URI);
    }

    public DeviceAttestRegistrationEndpointFilter(AuthenticationConverter authenticationConverter,
                                               AuthenticationProvider authenticationProvider,
                                               String endpointUri) {
        Assert.notNull(authenticationConverter, "authenticationConverter must not be null");
        Assert.notNull(authenticationProvider, "authenticationProvider must not be null");
        Assert.hasText(endpointUri, "endpointUri must not be empty");
        this.authenticationConverter = authenticationConverter;
        this.authenticationProvider = authenticationProvider;
        this.requestMatcher = PathPatternRequestMatcher.pathPattern(HttpMethod.POST, endpointUri);
    }

    public RequestMatcher getRequestMatcher() {
        return this.requestMatcher;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!this.requestMatcher.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Authentication authRequest = this.authenticationConverter.convert(request);
            if (authRequest == null) {
                sendErrorResponse(response, HttpStatus.BAD_REQUEST,
                        "invalid_request", "Missing required parameters: key_id, attestation, challenge");
                return;
            }

            Authentication result = this.authenticationProvider.authenticate(authRequest);
            sendSuccessResponse(response, (DeviceAttestationRegistrationAuthenticationToken) result);
        } catch (AuthenticationException ex) {
            logger.debug("Device attestation registration failed: {}", ex.getMessage());
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED,
                    "registration_failed", ex.getMessage());
        }
    }

    private void sendSuccessResponse(HttpServletResponse response,
                                     DeviceAttestationRegistrationAuthenticationToken result) throws IOException {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        Map<String, Object> body = new HashMap<>();
        body.put("key_id", result.getKeyId());
        Object principal = result.getPrincipal();
        if (principal != null) {
            body.put("username", principal.toString());
        }

        response.getWriter().write(JacksonUtils.writeValueAsString(body));
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status,
                                   String error, String description) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        Map<String, Object> body = new HashMap<>();
        body.put("error", error);
        if (description != null) {
            body.put("error_description", description);
        }

        response.getWriter().write(JacksonUtils.writeValueAsString(body));
    }
}
