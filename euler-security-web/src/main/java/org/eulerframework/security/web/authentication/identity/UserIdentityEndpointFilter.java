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
package org.eulerframework.security.web.authentication.identity;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eulerframework.common.util.jackson.JacksonUtils;
import org.eulerframework.security.core.identity.IdentityOccupiedException;
import org.eulerframework.security.core.identity.InvalidUserIdentityException;
import org.eulerframework.security.core.identity.UnsupportedIdentityTypeException;
import org.eulerframework.security.core.identity.UserIdentity;
import org.eulerframework.security.core.identity.UserIdentityNotFoundException;
import org.eulerframework.security.core.identity.UserIdentityService;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servlet filter serving the {@code /user/identities} REST surface.
 *
 * <p>The endpoints are not OAuth2 protocol endpoints. Access-token
 * validation is performed by the surrounding security filter chain
 * (typically the authorization-server chain's
 * {@code oauth2ResourceServer.jwt()}); this filter consumes the
 * already-authenticated request and delegates to a single
 * {@link UserIdentityService} bean.
 *
 * <h2>Endpoints</h2>
 * <ul>
 *   <li>{@code POST   <baseUri>}              &mdash; bind a new identity</li>
 *   <li>{@code GET    <baseUri>}              &mdash; list identities owned by the caller</li>
 *   <li>{@code GET    <baseUri>/{identityId}} &mdash; read a single identity</li>
 *   <li>{@code PUT    <baseUri>/{identityId}} &mdash; replace an identity</li>
 *   <li>{@code DELETE <baseUri>/{identityId}} &mdash; delete an identity</li>
 * </ul>
 *
 * <h2>Authentication</h2>
 * <p>The user id is resolved through the standard Spring Security
 * pipeline: {@link SecurityContextHolder} &rarr;
 * {@link Authentication#getName()} (the JWT {@code sub} / OAuth2
 * {@code principalName}) &rarr;
 * {@link EulerUserDetailsService#loadUserByPrincipal(String)} &rarr;
 * {@link EulerUserDetails#getUserId()}. Requests without a bound
 * principal, or whose principal cannot be resolved, are rejected with
 * {@code 401 invalid_token}. JWT decoding itself is the responsibility
 * of the upstream resource-server filter chain.
 *
 * <h2>Wire format</h2>
 * <p>Each identity is rendered as:
 * <pre>{@code
 * {
 *   "identity_id":   "...",
 *   "identity_type": "phone",
 *   "subject":       "9c1b8e2a3f6d...",
 *   "identifier":    "",
 *   "bound_at":      1778899139687,
 *   "phone":         "+8613*****00"
 * }
 * }</pre>
 * Per-type extension attributes carried on the {@link UserIdentity}
 * are flattened onto the envelope (e.g. {@code phone} for the phone
 * identity; {@code openid} / {@code nickname} / {@code unionid} for
 * WeChat). Timestamps are emitted as epoch milliseconds.
 *
 * <p>{@code subject} is the deterministic per-type unique key derived
 * by the owning backend from the raw value; the derivation function is
 * implementation defined and opaque to this filter.
 *
 * <p>{@code identifier} is a fixed empty-string placeholder retained
 * for clients whose parsing logic expects the key to be present; new
 * clients should consume {@code subject} and the per-type extension
 * attributes instead.
 *
 * <h2>Error mapping</h2>
 * <p>Exceptions raised by the {@link UserIdentityService} are
 * translated to OAuth2-style error envelopes:
 * <ul>
 *   <li>{@link org.eulerframework.security.core.identity.InvalidUserIdentityException
 *       InvalidUserIdentityException} &rarr;
 *       {@code 400 invalid_request}</li>
 *   <li>{@link org.eulerframework.security.core.identity.UnsupportedIdentityTypeException
 *       UnsupportedIdentityTypeException} &rarr;
 *       {@code 400 unsupported_identity_type}</li>
 *   <li>{@link org.eulerframework.security.core.identity.IdentityOccupiedException
 *       IdentityOccupiedException} &rarr;
 *       {@code 409 identity_occupied}</li>
 *   <li>{@link org.eulerframework.security.core.identity.UserIdentityNotFoundException
 *       UserIdentityNotFoundException} &rarr; {@code 404 not_found}</li>
 * </ul>
 */
public class UserIdentityEndpointFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(UserIdentityEndpointFilter.class);

    private static final String ERROR_INVALID_REQUEST = "invalid_request";
    private static final String ERROR_UNSUPPORTED_IDENTITY_TYPE = "unsupported_identity_type";
    private static final String ERROR_IDENTITY_OCCUPIED = "identity_occupied";
    private static final String ERROR_NOT_FOUND = "not_found";
    private static final String ERROR_INVALID_TOKEN = "invalid_token";
    private static final String ERROR_SERVER_ERROR = "server_error";

    private final UserIdentityService userIdentityService;
    private final EulerUserDetailsService userDetailsService;
    private final String endpointBaseUri;
    private final RequestMatcher collectionMatcher;
    private final RequestMatcher itemGetMatcher;
    private final RequestMatcher itemDeleteMatcher;
    private final RequestMatcher itemUpdateMatcher;
    private final RequestMatcher createMatcher;
    private final RequestMatcher requestMatcher;

    public UserIdentityEndpointFilter(UserIdentityService userIdentityService,
                                      EulerUserDetailsService userDetailsService,
                                      String endpointBaseUri) {
        Assert.notNull(userIdentityService, "userIdentityService must not be null");
        Assert.notNull(userDetailsService, "userDetailsService must not be null");
        Assert.hasText(endpointBaseUri, "endpointBaseUri must not be empty");
        Assert.isTrue(!endpointBaseUri.endsWith("/"), "endpointBaseUri must not end with '/'");
        this.userIdentityService = userIdentityService;
        this.userDetailsService = userDetailsService;
        this.endpointBaseUri = endpointBaseUri;
        String itemPattern = endpointBaseUri + "/{identityId}";
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
            String userId = resolveUserId();
            if (userId == null) {
                sendError(response, HttpStatus.UNAUTHORIZED,
                        ERROR_INVALID_TOKEN, "No authenticated user bound to the request");
                return;
            }

            if (this.createMatcher.matches(request)) {
                handleBind(request, response, userId);
            } else if (this.itemUpdateMatcher.matches(request)) {
                handleUpdate(request, response, userId, extractIdentityId(request));
            } else if (this.collectionMatcher.matches(request)) {
                handleList(response, userId);
            } else if (this.itemGetMatcher.matches(request)) {
                handleGet(response, userId, extractIdentityId(request));
            } else if (this.itemDeleteMatcher.matches(request)) {
                handleDelete(response, userId, extractIdentityId(request));
            }
        } catch (InvalidUserIdentityException ex) {
            logger.debug("user-identities rejected (invalid_request): {}", ex.getMessage());
            sendError(response, HttpStatus.BAD_REQUEST, ERROR_INVALID_REQUEST, ex.getMessage());
        } catch (UnsupportedIdentityTypeException ex) {
            logger.debug("user-identities rejected (unsupported_identity_type): {}", ex.getMessage());
            sendError(response, HttpStatus.BAD_REQUEST, ERROR_UNSUPPORTED_IDENTITY_TYPE, ex.getMessage());
        } catch (IdentityOccupiedException ex) {
            logger.debug("user-identities rejected (identity_occupied): {}", ex.getMessage());
            sendError(response, HttpStatus.CONFLICT, ERROR_IDENTITY_OCCUPIED, ex.getMessage());
        } catch (UserIdentityNotFoundException ex) {
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
        UserIdentity identity = this.userIdentityService.createUserIdentity(userId, params);
        sendJson(response, HttpStatus.OK, toJson(identity));
    }

    private void handleUpdate(HttpServletRequest request, HttpServletResponse response,
                              String userId, String identityId) throws IOException {
        MultiValueMap<String, String> params = readParameters(request);
        UserIdentity identity = this.userIdentityService.updateUserIdentity(userId, identityId, params);
        sendJson(response, HttpStatus.OK, toJson(identity));
    }

    private void handleList(HttpServletResponse response, String userId) throws IOException {
        List<UserIdentity> identities = this.userIdentityService.listUserIdentities(userId);
        List<Map<String, Object>> body = identities.stream().map(this::toJson).toList();
        sendJson(response, HttpStatus.OK, body);
    }

    private void handleGet(HttpServletResponse response, String userId, String identityId) throws IOException {
        Optional<UserIdentity> identity = this.userIdentityService.getUserIdentity(userId, identityId);
        if (identity.isEmpty()) {
            throw new UserIdentityNotFoundException(identityId);
        }
        sendJson(response, HttpStatus.OK, toJson(identity.get()));
    }

    private void handleDelete(HttpServletResponse response, String userId, String identityId) throws IOException {
        this.userIdentityService.deleteUserIdentity(userId, identityId);
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

    private String extractIdentityId(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        String prefix = this.endpointBaseUri + "/";
        int idx = path.indexOf(prefix);
        if (idx < 0) {
            throw new InvalidUserIdentityException("Cannot extract identity id from path: " + path);
        }
        String identityId = path.substring(idx + prefix.length());
        if (identityId.isEmpty()) {
            throw new InvalidUserIdentityException("Missing identity id in path");
        }
        return identityId;
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

    /**
     * Hand-rolled serialisation that emits the envelope fields
     * explicitly (omitting the SPI-internal {@code userId}) and then
     * appends the per-type extension attributes returned by
     * {@link UserIdentity#getExtensions()}. {@code identifier} is
     * emitted as a fixed empty string for clients whose parsing logic
     * expects the key to be present; new clients should consume
     * {@code subject} and the per-type extension attributes instead.
     */
    private Map<String, Object> toJson(UserIdentity identity) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("identity_id", identity.getIdentityId());
        body.put("identity_type", identity.getIdentityType());
        body.put("subject", identity.getSubject());
        body.put("identifier", "");
        // Hand the Instant directly to Jackson rather than pre-converting to
        // a Long. The shared ObjectMapper enables WRITE_DATES_AS_TIMESTAMPS
        // with WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS disabled, so jsr310's
        // InstantSerializer emits epoch milliseconds via writeNumber(long).
        // Going through the dedicated serializer bypasses the JsSafeModule
        // policy that turns Long into JSON string for JavaScript precision
        // safety, which is not needed here: epoch-millis stays well below
        // Number.MAX_SAFE_INTEGER and clients expect a numeric timestamp.
        body.put("bound_at", identity.getBoundAt());
        identity.getExtensions().forEach((k, v) ->
                body.put(org.eulerframework.common.util.StringUtils
                        .camelStyleToUnderLineLowerCase(k), v));
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
