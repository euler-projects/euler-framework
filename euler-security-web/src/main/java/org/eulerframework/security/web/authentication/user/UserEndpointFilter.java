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
package org.eulerframework.security.web.authentication.user;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eulerframework.common.util.jackson.JacksonUtils;
import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.eulerframework.security.core.userdetails.EulerUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Servlet filter serving the {@code /user} REST surface.
 *
 * <p>The endpoints are not OAuth2 protocol endpoints. Access-token
 * validation is performed by the surrounding security filter chain
 * (typically the authorization-server chain's
 * {@code oauth2ResourceServer.jwt()}); this filter consumes the
 * already-authenticated request and delegates to a single
 * {@link UserDetailsManager} bean.
 *
 * <h2>Endpoints</h2>
 * <ul>
 *   <li>{@code DELETE <baseUri>} &mdash; delete the caller's own account
 *       via {@link UserDetailsManager#deleteUser(String)}</li>
 * </ul>
 *
 * <h2>Authentication</h2>
 * <p>The user is resolved through the standard Spring Security
 * pipeline: {@link SecurityContextHolder} &rarr;
 * {@link Authentication#getName()} (the JWT {@code sub} / OAuth2
 * {@code principalName}) &rarr;
 * {@link EulerUserDetailsService#loadUserByPrincipal(String)} &rarr;
 * {@link EulerUserDetails#getUsername()}. The canonical username (as
 * opposed to any other principal variant such as phone or email) is
 * then handed to {@link UserDetailsManager#deleteUser(String)}, keeping
 * the invocation safe for any Spring-conformant
 * {@code UserDetailsManager} implementation. Requests without a bound
 * principal, or whose principal cannot be resolved, are rejected with
 * {@code 401 invalid_token}. JWT decoding itself is the responsibility
 * of the upstream resource-server filter chain.
 *
 * <h2>Error mapping</h2>
 * <ul>
 *   <li>{@link org.springframework.security.core.userdetails.UsernameNotFoundException
 *       UsernameNotFoundException} &rarr; {@code 404 not_found}</li>
 *   <li>Any other {@link RuntimeException} &rarr;
 *       {@code 500 server_error}</li>
 * </ul>
 *
 * <h2>Post-deletion note</h2>
 * <p>Because access-tokens are stateless JWTs, tokens issued before
 * deletion may remain nominally valid until they expire; clients should
 * clear their local session immediately after receiving {@code 204}.
 */
public class UserEndpointFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(UserEndpointFilter.class);

    private static final String ERROR_NOT_FOUND = "not_found";
    private static final String ERROR_INVALID_TOKEN = "invalid_token";
    private static final String ERROR_SERVER_ERROR = "server_error";

    private final UserDetailsManager userDetailsManager;
    private final EulerUserDetailsService userDetailsService;
    private final RequestMatcher deleteMatcher;
    private final RequestMatcher requestMatcher;

    public UserEndpointFilter(UserDetailsManager userDetailsManager,
                              EulerUserDetailsService userDetailsService,
                              String endpointBaseUri) {
        Assert.notNull(userDetailsManager, "userDetailsManager must not be null");
        Assert.notNull(userDetailsService, "userDetailsService must not be null");
        Assert.hasText(endpointBaseUri, "endpointBaseUri must not be empty");
        Assert.isTrue(!endpointBaseUri.endsWith("/"), "endpointBaseUri must not end with '/'");
        this.userDetailsManager = userDetailsManager;
        this.userDetailsService = userDetailsService;
        this.deleteMatcher = PathPatternRequestMatcher.pathPattern(HttpMethod.DELETE, endpointBaseUri);
        this.requestMatcher = new OrRequestMatcher(this.deleteMatcher);
    }

    /**
     * Returns a {@link RequestMatcher} that matches every endpoint
     * served by this filter. Compose with
     * {@code http.securityMatcher(...)} via an {@code OrRequestMatcher}
     * so that the surrounding chain's authentication rules cover this
     * filter as well.
     */
    public RequestMatcher getRequestMatcher() {
        return this.requestMatcher;
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) throws ServletException, IOException {
        if (!this.requestMatcher.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String username = resolveAuthenticatedUsername();
            if (username == null) {
                sendError(response, HttpStatus.UNAUTHORIZED,
                        ERROR_INVALID_TOKEN, "No authenticated user bound to the request");
                return;
            }

            if (this.deleteMatcher.matches(request)) {
                handleDelete(response, username);
            }
        } catch (UsernameNotFoundException ex) {
            logger.debug("user endpoint rejected (not_found): {}", ex.getMessage());
            sendError(response, HttpStatus.NOT_FOUND, ERROR_NOT_FOUND, ex.getMessage());
        } catch (RuntimeException ex) {
            logger.warn("user endpoint request failed", ex);
            sendError(response, HttpStatus.INTERNAL_SERVER_ERROR, ERROR_SERVER_ERROR, ex.getMessage());
        }
    }

    // ---- handlers ----

    private void handleDelete(HttpServletResponse response, String username) {
        this.userDetailsManager.deleteUser(username);
        response.setStatus(HttpStatus.NO_CONTENT.value());
    }

    // ---- helpers ----

    private String resolveAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        // In a Resource Server chain the principal is an
        // OAuth2AuthenticatedPrincipal whose getUsername() is not
        // directly exposed, so we reverse-resolve via the principal name
        // carried by the JWT 'sub' claim (== Authentication.getName() ==
        // OAuth2 principalName) and pull the canonical username off the
        // EulerUserDetails to keep the UserDetailsManager contract
        // satisfied regardless of which identifier variant the caller
        // authenticated with.
        String principalName = authentication.getName();
        if (!StringUtils.hasText(principalName)) {
            return null;
        }
        try {
            EulerUserDetails userDetails = this.userDetailsService.loadUserByPrincipal(principalName);
            if (userDetails == null) {
                logger.debug("No EulerUserDetails resolved for principal '{}'", principalName);
                return null;
            }
            return userDetails.getUsername();
        } catch (RuntimeException ex) {
            logger.debug("Failed to resolve username for principal '{}': {}", principalName, ex.getMessage());
            return null;
        }
    }

    private void sendError(HttpServletResponse response, HttpStatus status, String error, String description)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", error);
        if (description != null) {
            body.put("error_description", description);
        }
        response.getWriter().write(JacksonUtils.writeValueAsString(body));
    }
}
