/*
 * Copyright 2013-2024 the original author or authors.
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
package org.eulerframework.security.web.endpoint.csrf;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

/**
 * Same-origin guard and cache hardening for the {@code /_csrf} token endpoints.
 *
 * <p>Complements the application-level CORS policy with two server-side checks:
 * {@link #isSameOriginRequest(HttpServletRequest)} rejects cross-origin reads
 * regardless of CORS configuration, and
 * {@link #applyNoStoreHeaders(HttpServletResponse)} forbids intermediate caching
 * of the token. Both decisions are emitted at {@code TRACE} level under the
 * logger name {@code org.eulerframework.security.web.endpoint.csrf} for
 * reverse-proxy diagnostics.</p>
 */
final class CsrfEndpointGuards {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsrfEndpointGuards.class);

    private static final String SEC_FETCH_SITE = "Sec-Fetch-Site";
    private static final String SEC_FETCH_SITE_SAME_ORIGIN = "same-origin";
    private static final String SEC_FETCH_SITE_NONE = "none";

    private CsrfEndpointGuards() {
    }

    /**
     * Returns {@code true} when the request originates from the same origin as
     * the endpoint.
     *
     * <p>Accepts {@code Sec-Fetch-Site} values of {@code same-origin} and
     * {@code none}, and accepts a missing or matching {@code Origin} header.
     * The expected origin is reconstructed from
     * {@link HttpServletRequest#getScheme()},
     * {@link HttpServletRequest#getServerName()} and
     * {@link HttpServletRequest#getServerPort()}, which honour
     * {@code server.forward-headers-strategy}. Requests carrying neither
     * header are accepted as non-browser callers.</p>
     */
    static boolean isSameOriginRequest(HttpServletRequest request) {
        String secFetchSite = request.getHeader(SEC_FETCH_SITE);
        String origin = request.getHeader(HttpHeaders.ORIGIN);
        String expected = expectedOrigin(request);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("CSRF same-origin check: uri={}, Sec-Fetch-Site={}, Origin={}, expectedOrigin={} (scheme={}, serverName={}, serverPort={})",
                    request.getRequestURI(), secFetchSite, origin, expected,
                    request.getScheme(), request.getServerName(), request.getServerPort());
        }

        if (secFetchSite != null
                && !SEC_FETCH_SITE_SAME_ORIGIN.equals(secFetchSite)
                && !SEC_FETCH_SITE_NONE.equals(secFetchSite)) {
            LOGGER.trace("CSRF same-origin check: REJECT uri={} reason=Sec-Fetch-Site={}",
                    request.getRequestURI(), secFetchSite);
            return false;
        }

        if (origin != null && !origin.equals(expected)) {
            LOGGER.trace("CSRF same-origin check: REJECT uri={} reason=Origin mismatch, received={}, expected={}",
                    request.getRequestURI(), origin, expected);
            return false;
        }

        if (LOGGER.isTraceEnabled()) {
            String matched;
            if (SEC_FETCH_SITE_SAME_ORIGIN.equals(secFetchSite)) {
                matched = "Sec-Fetch-Site=same-origin";
            } else if (SEC_FETCH_SITE_NONE.equals(secFetchSite)) {
                matched = "Sec-Fetch-Site=none";
            } else if (origin != null) {
                matched = "Origin matches expected";
            } else {
                matched = "no Sec-Fetch-Site and no Origin";
            }
            LOGGER.trace("CSRF same-origin check: ALLOW uri={} via {}", request.getRequestURI(), matched);
        }
        return true;
    }

    /**
     * Sets {@code Cache-Control: no-store, private}, {@code Pragma: no-cache},
     * {@code Vary: Origin} and {@code X-Content-Type-Options: nosniff} on the
     * response.
     */
    static void applyNoStoreHeaders(HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store, private");
        response.setHeader(HttpHeaders.PRAGMA, "no-cache");
        response.addHeader(HttpHeaders.VARY, HttpHeaders.ORIGIN);
        response.setHeader("X-Content-Type-Options", "nosniff");
        LOGGER.trace("CSRF response hardened: Cache-Control=no-store/private, Vary=Origin, X-Content-Type-Options=nosniff");
    }

    private static String expectedOrigin(HttpServletRequest request) {
        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();
        boolean defaultPort = ("http".equals(scheme) && port == 80)
                || ("https".equals(scheme) && port == 443);
        return defaultPort
                ? scheme + "://" + host
                : scheme + "://" + host + ":" + port;
    }
}
