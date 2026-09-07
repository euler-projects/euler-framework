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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eulerframework.common.util.jackson.JacksonUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shared helpers that render the outcome of a browser authentication
 * submission as either a redirect (traditional HTML form POST) or a small
 * JSON envelope (XHR / fetch client), decided by HTTP content negotiation on
 * the {@code Accept} request header.
 *
 * <p>Used by the login handlers ({@link JsonLoginRedirectStrategy},
 * {@link JsonLoginFailureHandler}) and by
 * {@code org.eulerframework.security.web.access.EulerAccessDeniedHandler}, so
 * a fetch client sees one consistent JSON contract across authentication
 * success, authentication failure and access denial, while server-rendered
 * pages keep their redirect behaviour.
 *
 * <p>A request is treated as JSON-preferring only when its {@code Accept}
 * header explicitly lists a media type compatible with
 * {@code application/json}. The bare {@code *&#47;*} wildcard that browsers
 * send for ordinary form posts and top-level navigations is ignored. Note
 * that {@code fetch} and {@code XMLHttpRequest} also default to
 * {@code *&#47;*}; an API client must set {@code Accept: application/json}
 * explicitly to opt in.
 *
 * <p>Envelopes (bodies are loose maps so fields can be added later without
 * changing callers):
 * <ul>
 *   <li>success &mdash; HTTP 200, {@code {"redirect_url":"<target>"}}</li>
 *   <li>error &mdash; the given HTTP status,
 *       {@code {"error":"<code>","timestamp":<epoch millis>}}</li>
 * </ul>
 * The error envelope deliberately carries no human message: the client
 * localises from the machine-readable {@code error} code, which also avoids
 * leaking account-state wording.
 */
public final class SecurityJsonResponses {

    /** Body field carrying the URL a successful login should navigate to. */
    public static final String FIELD_REDIRECT_URL = "redirect_url";

    /** Body field carrying the machine-readable error code. */
    public static final String FIELD_ERROR = "error";

    /** Body field carrying the epoch-millisecond instant of the error. */
    public static final String FIELD_TIMESTAMP = "timestamp";

    private SecurityJsonResponses() {
    }

    /**
     * Whether the request prefers a JSON result over a redirect, judged by its
     * {@code Accept} header.
     */
    public static boolean prefersJson(HttpServletRequest request) {
        return prefersJson(request.getHeader(HttpHeaders.ACCEPT));
    }

    /**
     * String form of {@link #prefersJson(HttpServletRequest)}, exposed so the
     * negotiation rule can be exercised without a servlet request.
     */
    public static boolean prefersJson(String acceptHeader) {
        if (!StringUtils.hasText(acceptHeader)) {
            return false;
        }
        try {
            for (MediaType accepted : MediaType.parseMediaTypes(acceptHeader)) {
                // Skip the bare */* that browsers send for form posts and
                // navigations; only an explicit JSON-compatible type opts in.
                if (accepted.isWildcardType()) {
                    continue;
                }
                if (accepted.isCompatibleWith(MediaType.APPLICATION_JSON)) {
                    return true;
                }
            }
        } catch (InvalidMediaTypeException ex) {
            // Malformed Accept header: fall back to redirect behaviour.
        }
        return false;
    }

    /**
     * Write the success envelope (HTTP 200) carrying the URL the browser form
     * flow would otherwise have been redirected to.
     */
    public static void writeSuccess(HttpServletResponse response, String redirectUrl) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(FIELD_REDIRECT_URL, redirectUrl);
        write(response, HttpStatus.OK, body);
    }

    /**
     * Write the error envelope under the given HTTP status, carrying a
     * machine-readable {@code error} code and an epoch-millisecond
     * {@code timestamp}.
     */
    public static void writeError(HttpServletResponse response, HttpStatus status, String error) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(FIELD_ERROR, error);
        // Instant is serialised by the shared ObjectMapper as epoch
        // milliseconds (WRITE_DATES_AS_TIMESTAMPS on, nanoseconds off),
        // matching the bound_at convention used elsewhere.
        body.put(FIELD_TIMESTAMP, Instant.now());
        write(response, status, body);
    }

    private static void write(HttpServletResponse response, HttpStatus status, Map<String, Object> body)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(JacksonUtils.writeValueAsString(body));
    }
}
