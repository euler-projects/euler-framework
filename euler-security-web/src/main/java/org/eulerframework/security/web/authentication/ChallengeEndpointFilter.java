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
package org.eulerframework.security.web.authentication;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eulerframework.common.util.jackson.JacksonUtils;
import org.eulerframework.security.authentication.ChallengeService;
import org.eulerframework.security.authentication.GeneratedChallenge;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

/**
 * A generic filter that exposes a challenge endpoint ({@code POST}) for generating
 * one-time challenges used in challenge-response authentication flows
 * (e.g., Apple App Attest, Attestation-Based Client Authentication).
 * <p>
 * This endpoint is typically anonymous (no authentication required) and should be
 * exempt from CSRF protection. The response includes {@code Cache-Control: no-store}
 * and {@code Pragma: no-cache} headers to prevent caching.
 * <p>
 * Response format:
 * <pre>
 * HTTP/1.1 200 OK
 * Content-Type: application/json
 * Cache-Control: no-store
 *
 * {"challenge": "base64url-data"}
 * </pre>
 *
 * @see ChallengeService
 */
public class ChallengeEndpointFilter extends OncePerRequestFilter {

    private final ChallengeService challengeService;
    private final RequestMatcher requestMatcher;

    /**
     * Create a new {@code ChallengeEndpointFilter} that handles {@code POST} requests
     * to the specified endpoint URI.
     *
     * @param challengeService the service used to generate one-time challenges
     * @param endpointUri      the URI path for this challenge endpoint (e.g., {@code /oauth2/challenge})
     */
    public ChallengeEndpointFilter(ChallengeService challengeService, String endpointUri) {
        Assert.notNull(challengeService, "challengeService must not be null");
        Assert.hasText(endpointUri, "endpointUri must not be empty");
        this.challengeService = challengeService;
        this.requestMatcher = PathPatternRequestMatcher.pathPattern(HttpMethod.POST, endpointUri);
    }

    /**
     * Returns the {@link RequestMatcher} for this endpoint.
     * Can be used to configure CSRF exemption, security matcher, and authorization rules.
     *
     * @return the request matcher for this challenge endpoint
     */
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
            GeneratedChallenge generatedChallenge = this.challengeService.generateChallenge();
            sendChallengeResponse(response, generatedChallenge);
        } catch (Exception ex) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write("{\"error\":\"server_error\",\"error_description\":\"Failed to generate challenge\"}");
        }
    }

    private void sendChallengeResponse(HttpServletResponse response, GeneratedChallenge generatedChallenge) throws IOException {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.addHeader("Cache-Control", "no-store");
        response.addHeader("Pragma", "no-cache");

        Map<String, Object> body = Collections.singletonMap("challenge", generatedChallenge);
        response.getWriter().write(JacksonUtils.writeValueAsString(body));
    }
}
