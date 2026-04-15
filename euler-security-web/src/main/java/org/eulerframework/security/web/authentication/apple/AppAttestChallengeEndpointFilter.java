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

package org.eulerframework.security.web.authentication.apple;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

/**
 * A filter that exposes a {@code POST /app/attest/challenge} endpoint for generating
 * one-time challenges used in Apple App Attest registration and assertion flows.
 * <p>
 * This endpoint is anonymous (no authentication required).
 * <p>
 * Response format:
 * <pre>
 * {"challenge": "base64url-data"}
 * </pre>
 */
public class AppAttestChallengeEndpointFilter extends OncePerRequestFilter {

    public static final String DEFAULT_CHALLENGE_ENDPOINT_URI = "/app/attest/challenge";

    private final ChallengeService challengeService;
    private final RequestMatcher requestMatcher;

    public AppAttestChallengeEndpointFilter(ChallengeService challengeService) {
        this(challengeService, DEFAULT_CHALLENGE_ENDPOINT_URI);
    }

    public AppAttestChallengeEndpointFilter(ChallengeService challengeService, String endpointUri) {
        Assert.notNull(challengeService, "challengeService must not be null");
        Assert.hasText(endpointUri, "endpointUri must not be empty");
        this.challengeService = challengeService;
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

        String json = "{\"challenge\":\"" + escapeJson(generatedChallenge.challenge()) + "\"}";
        response.getWriter().write(json);
    }

    private static String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
