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
package org.eulerframework.security.web.authentication.factor;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eulerframework.common.util.jackson.JacksonUtils;
import org.eulerframework.security.authentication.factor.IdentifierConflictException;
import org.eulerframework.security.authentication.factor.InvalidAuthenticationFactorRequestException;
import org.eulerframework.security.authentication.factor.UnsupportedFactorTypeException;
import org.eulerframework.security.authentication.factor.UserAuthenticationFactor;
import org.eulerframework.security.authentication.factor.UserAuthenticationFactorNotFoundException;
import org.eulerframework.security.authentication.factor.UserAuthenticationFactorService;
import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.eulerframework.security.core.userdetails.EulerUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Filter exposing the {@code /user/identities} REST surface (CRUD for
 * {@link UserAuthenticationFactor}).
 * <p>
 * The endpoint family is intentionally <em>not</em> an OAuth2 protocol
 * endpoint. AT validation is performed by the surrounding security filter
 * chain (typically the authorization-server chain's
 * {@code oauth2ResourceServer.jwt()}); this filter consumes the
 * already-authenticated request and delegates to a single
 * {@link UserAuthenticationFactorService} entry-point provided by business
 * code (either a single-factor implementation or a business-side composite
 * router that dispatches by {@code factor_type}).
 *
 * <h2>Endpoints</h2>
 * <ul>
 *     <li>{@code POST   <baseUri>}              - bind a new factor</li>
 *     <li>{@code PUT    <baseUri>/{factorId}}   - update (rebind) an existing factor</li>
 *     <li>{@code GET    <baseUri>}              - list this user's factors</li>
 *     <li>{@code GET    <baseUri>/{factorId}}   - get one factor</li>
 *     <li>{@code DELETE <baseUri>/{factorId}}   - delete one factor</li>
 * </ul>
 *
 * <h2>Authentication</h2>
 * The user id is resolved via the standard Spring Security pipeline:
 * {@link SecurityContextHolder} -&gt; {@link Authentication#getName()} (the
 * JWT {@code sub} / OAuth2 {@code principalName} — i.e. the username) -&gt;
 * {@link EulerUserDetailsService#loadUserByPrincipal(String)} -&gt;
 * {@link EulerUserDetails#getUserId()}.
 * <p>
 * We deliberately do <strong>not</strong> consult
 * {@code UserContextHolder} here: in a Resource Server chain the only
 * principal available on the {@link Authentication} is an
 * {@code OAuth2AuthenticatedPrincipal} that carries no {@code userId} (the
 * {@code sub_details} extension claim and the {@code Principal.class}
 * attribute are populated only on the Authorization Server side), so
 * {@code UserContext#getUserId()} returns {@code null} for these requests.
 * Reverse-resolving via {@link EulerUserDetailsService} mirrors the pattern
 * used by {@code OAuth2OtpAuthenticationProvider} and works uniformly
 * regardless of whether the surrounding chain is Resource Server or
 * Authorization Server.
 * <p>
 * When no authenticated user is bound to the current request, or the
 * principal cannot be reverse-resolved into an {@link EulerUserDetails},
 * the filter responds with {@code 401 invalid_token}. The filter itself
 * does not inspect the JWT — that is the job of the upstream resource-server
 * filter chain.
 *
 * <h2>Response shape</h2>
 * Every factor is rendered as the {@code identity_id} / {@code identity_type} /
 * {@code identifier} / {@code bound_at} envelope plus per-factor
 * {@link UserAuthenticationFactor#extensions()} flattened into the top-level
 * object. Timestamps are emitted as epoch-millis. The internal
 * {@link UserAuthenticationFactor} concept is intentionally surfaced to API
 * consumers under the public "User Identity" terminology.
 * <p>
 * Note: unlike {@code OidcUserInfoEndpointFilter} this filter does not go
 * through {@code AuthenticationManager}/{@code AuthenticationProvider} — the
 * operations are post-authentication resource CRUD, not an authentication
 * exchange. Should we ever need to inject explicit authorization decisions,
 * we will rely on Spring Security's standard
 * {@code AuthorizationFilter} / {@code @PreAuthorize} machinery.
 */
public class UserAuthenticationFactorEndpointFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(UserAuthenticationFactorEndpointFilter.class);

    private static final String ERROR_INVALID_REQUEST = "invalid_request";
    private static final String ERROR_UNSUPPORTED_FACTOR_TYPE = "unsupported_identity_type";
    private static final String ERROR_IDENTIFIER_CONFLICT = "identity_occupied";
    private static final String ERROR_NOT_FOUND = "not_found";
    private static final String ERROR_INVALID_TOKEN = "invalid_token";
    private static final String ERROR_SERVER_ERROR = "server_error";

    private final UserAuthenticationFactorService userAuthenticationFactorService;
    private final EulerUserDetailsService userDetailsService;
    private final String endpointBaseUri;
    private final RequestMatcher collectionMatcher;
    private final RequestMatcher itemGetMatcher;
    private final RequestMatcher itemDeleteMatcher;
    private final RequestMatcher itemUpdateMatcher;
    private final RequestMatcher createMatcher;
    private final RequestMatcher requestMatcher;

    public UserAuthenticationFactorEndpointFilter(UserAuthenticationFactorService userAuthenticationFactorService,
                                                  EulerUserDetailsService userDetailsService,
                                                  String endpointBaseUri) {
        Assert.notNull(userAuthenticationFactorService, "userAuthenticationService must not be null");
        Assert.notNull(userDetailsService, "userDetailsService must not be null");
        Assert.hasText(endpointBaseUri, "endpointBaseUri must not be empty");
        Assert.isTrue(!endpointBaseUri.endsWith("/"), "endpointBaseUri must not end with '/'");
        this.userAuthenticationFactorService = userAuthenticationFactorService;
        this.userDetailsService = userDetailsService;
        this.endpointBaseUri = endpointBaseUri;
        String itemPattern = endpointBaseUri + "/{factorId}";
        this.createMatcher = PathPatternRequestMatcher.pathPattern(HttpMethod.POST, endpointBaseUri);
        this.collectionMatcher = PathPatternRequestMatcher.pathPattern(HttpMethod.GET, endpointBaseUri);
        this.itemGetMatcher = PathPatternRequestMatcher.pathPattern(HttpMethod.GET, itemPattern);
        this.itemUpdateMatcher = PathPatternRequestMatcher.pathPattern(HttpMethod.PUT, itemPattern);
        this.itemDeleteMatcher = PathPatternRequestMatcher.pathPattern(HttpMethod.DELETE, itemPattern);
        this.requestMatcher = new OrRequestMatcher(
                this.createMatcher, this.collectionMatcher,
                this.itemGetMatcher, this.itemUpdateMatcher,
                this.itemDeleteMatcher);
    }

    /**
     * Returns the union {@link RequestMatcher} matching all five endpoints.
     * Useful for combining with {@code http.securityMatcher(...)} to attach
     * this filter chain to the same chain that performs AT validation.
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
            String userId = resolveUserId();
            if (userId == null) {
                sendError(response, HttpStatus.UNAUTHORIZED,
                        ERROR_INVALID_TOKEN, "No authenticated user bound to the request");
                return;
            }

            if (this.createMatcher.matches(request)) {
                handleBind(request, response, userId);
            } else if (this.itemUpdateMatcher.matches(request)) {
                handleUpdate(request, response, userId, extractFactorId(request));
            } else if (this.collectionMatcher.matches(request)) {
                handleList(response, userId);
            } else if (this.itemGetMatcher.matches(request)) {
                handleGet(response, userId, extractFactorId(request));
            } else if (this.itemDeleteMatcher.matches(request)) {
                handleDelete(response, userId, extractFactorId(request));
            }
        } catch (InvalidAuthenticationFactorRequestException ex) {
            logger.debug("user-identities rejected (invalid_request): {}", ex.getMessage());
            sendError(response, HttpStatus.BAD_REQUEST, ERROR_INVALID_REQUEST, ex.getMessage());
        } catch (UnsupportedFactorTypeException ex) {
            logger.debug("user-identities rejected (unsupported_identity_type): {}", ex.getMessage());
            sendError(response, HttpStatus.BAD_REQUEST, ERROR_UNSUPPORTED_FACTOR_TYPE, ex.getMessage());
        } catch (IdentifierConflictException ex) {
            logger.debug("user-identities rejected (identity_occupied): {}", ex.getMessage());
            sendError(response, HttpStatus.CONFLICT, ERROR_IDENTIFIER_CONFLICT, ex.getMessage());
        } catch (UserAuthenticationFactorNotFoundException ex) {
            logger.debug("user-identities rejected (not_found): {}", ex.getMessage());
            sendError(response, HttpStatus.NOT_FOUND, ERROR_NOT_FOUND, ex.getMessage());
        } catch (RuntimeException ex) {
            logger.warn("user-identities request failed", ex);
            sendError(response, HttpStatus.INTERNAL_SERVER_ERROR, ERROR_SERVER_ERROR, ex.getMessage());
        }
    }

    // ---- handlers ----

    private void handleBind(HttpServletRequest request, HttpServletResponse response, String userId)
            throws IOException {
        MultiValueMap<String, String> params = readParameters(request);
        UserAuthenticationFactor factor = this.userAuthenticationFactorService.bind(userId, params);
        sendJson(response, HttpStatus.OK, toJson(factor));
    }

    private void handleUpdate(HttpServletRequest request, HttpServletResponse response,
                              String userId, String factorId) throws IOException {
        MultiValueMap<String, String> params = readParameters(request);
        UserAuthenticationFactor factor = this.userAuthenticationFactorService.update(userId, factorId, params);
        sendJson(response, HttpStatus.OK, toJson(factor));
    }

    private void handleList(HttpServletResponse response, String userId) throws IOException {
        List<UserAuthenticationFactor> factors = this.userAuthenticationFactorService.findAllByUserId(userId);
        // Stable ordering: most recently bound first.
        factors = factors.stream()
                .sorted(Comparator.comparing(UserAuthenticationFactor::boundAt).reversed())
                .toList();
        List<Map<String, Object>> body = factors.stream().map(this::toJson).toList();
        sendJson(response, HttpStatus.OK, body);
    }

    private void handleGet(HttpServletResponse response, String userId, String factorId) throws IOException {
        Optional<UserAuthenticationFactor> factor = this.userAuthenticationFactorService.findById(userId, factorId);
        if (factor.isEmpty()) {
            throw new UserAuthenticationFactorNotFoundException(factorId);
        }
        sendJson(response, HttpStatus.OK, toJson(factor.get()));
    }

    private void handleDelete(HttpServletResponse response, String userId, String factorId) throws IOException {
        this.userAuthenticationFactorService.deleteById(userId, factorId);
        response.setStatus(HttpStatus.NO_CONTENT.value());
    }

    // ---- helpers ----

    private String resolveUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        // In a Resource Server chain the principal is an
        // OAuth2AuthenticatedPrincipal whose getUserId() is null, so we
        // fall back to reverse-resolving via the username carried by the JWT
        // 'sub' claim (== Authentication.getName() == OAuth2 principalName).
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
            return userDetails.getUserId();
        } catch (RuntimeException ex) {
            logger.debug("Failed to resolve userId for principal '{}': {}", principalName, ex.getMessage());
            return null;
        }
    }

    private String extractFactorId(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        String prefix = this.endpointBaseUri + "/";
        int idx = path.indexOf(prefix);
        if (idx < 0) {
            throw new InvalidAuthenticationFactorRequestException("Cannot extract factor id from path: " + path);
        }
        String factorId = path.substring(idx + prefix.length());
        if (factorId.isEmpty()) {
            throw new InvalidAuthenticationFactorRequestException("Missing factor id in path");
        }
        return factorId;
    }

    private MultiValueMap<String, String> readParameters(HttpServletRequest request) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        request.getParameterMap().forEach((k, v) -> {
            for (String value : v) {
                params.add(k, value);
            }
        });
        return params;
    }

    // Hand-rolled serialisation that intentionally excludes the internal
    // {@code userId} SPI field. The internal {@code factorId} / {@code factorType}
    // are surfaced to clients as {@code identity_id} / {@code identity_type}
    // (the public "User Identity" naming). The {@code identifier} field is part
    // of the public response: business documentation explicitly surfaces it on
    // the /user/identities API (e.g. as the SHA-256 hash of phone/email or the
    // raw openid for wechat).
    private Map<String, Object> toJson(UserAuthenticationFactor factor) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("identity_id", factor.factorId());
        body.put("identity_type", factor.factorType());
        body.put("identifier", factor.identifier());
        // Hand the Instant directly to Jackson rather than pre-converting to Long. The global
        // ObjectMapper has WRITE_DATES_AS_TIMESTAMPS enabled and WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS
        // disabled, so jsr310's InstantSerializer emits a JSON number whose value is the epoch
        // milliseconds via gen.writeNumber(long). Going through the dedicated date serializer
        // bypasses the JsSafeModule policy that turns Long into JSON string for JavaScript
        // precision safety - we don't need that protection here because epoch-millis stays well
        // below Number.MAX_SAFE_INTEGER (~year 287396) and clients expect a numeric timestamp.
        body.put("bound_at", factor.boundAt());
        if (factor.extensions() != null) {
            body.putAll(factor.extensions());
        }
        return body;
    }

    private void sendJson(HttpServletResponse response, HttpStatus status, Object body) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(JacksonUtils.writeValueAsString(body));
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
