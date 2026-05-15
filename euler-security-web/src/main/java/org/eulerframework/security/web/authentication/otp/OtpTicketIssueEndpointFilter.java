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

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eulerframework.common.util.jackson.JacksonUtils;
import org.eulerframework.security.authentication.otp.OtpDeliveryFailedException;
import org.eulerframework.security.authentication.otp.OtpInvalidIdentityIdException;
import org.eulerframework.security.authentication.otp.OtpIssueResult;
import org.eulerframework.security.authentication.otp.OtpTicketIssueAuthenticationToken;
import org.eulerframework.security.authentication.otp.OtpUnsupportedChannelException;
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
 * Filter exposing the {@code POST /otp/tickets} endpoint that issues OTP tickets.
 * <p>
 * The endpoint is anonymous and CSRF-exempt. Requests are converted into an
 * {@link OtpTicketIssueAuthenticationToken} via the supplied
 * {@link AuthenticationConverter} and dispatched to an
 * {@link AuthenticationProvider}; the authenticated token's
 * {@link OtpIssueResult} populates the success body.
 *
 * <h2>Request parameters</h2>
 * <ul>
 *     <li>{@code channel} (required) - logical channel name (e.g. {@code sms}, {@code email})</li>
 *     <li>{@code recipient} or {@code identity_id} (exactly one) - delivery target or
 *         the abstract identity to be resolved server-side</li>
 *     <li>{@code purpose} (optional) - opaque tag forwarded to the channel</li>
 *     <li>{@code code_challenge} (required) - PKCE code challenge</li>
 *     <li>{@code code_challenge_method} (required) - must be {@code S256}</li>
 * </ul>
 *
 * <h2>Success response (HTTP 200)</h2>
 * <pre>
 * {"otp_ticket":"ot_...","expires_in":300,"retry_after":60}
 * </pre>
 *
 * <h2>Error responses</h2>
 * Each non-success outcome maps to a {@code error} / {@code error_description}
 * envelope; refer to the constants in this class for the mapping.
 */
public class OtpTicketIssueEndpointFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(OtpTicketIssueEndpointFilter.class);

    private static final String ERROR_INVALID_REQUEST = "invalid_request";
    private static final String ERROR_UNSUPPORTED_CHANNEL = "unsupported_channel";
    private static final String ERROR_INVALID_IDENTITY_ID = "invalid_identity_id";
    private static final String ERROR_DELIVERY_FAILED = "delivery_failed";

    private final AuthenticationConverter authenticationConverter;
    private final AuthenticationProvider authenticationProvider;
    private final RequestMatcher requestMatcher;

    public OtpTicketIssueEndpointFilter(AuthenticationConverter authenticationConverter,
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
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) throws ServletException, IOException {
        if (!this.requestMatcher.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Authentication authRequest = this.authenticationConverter.convert(request);
            if (authRequest == null) {
                sendErrorResponse(response, HttpStatus.BAD_REQUEST,
                        ERROR_INVALID_REQUEST,
                        "Missing or invalid parameters: channel, code_challenge, code_challenge_method=S256, " +
                                "and exactly one of recipient/identity_id are required");
                return;
            }

            Authentication result = this.authenticationProvider.authenticate(authRequest);
            sendSuccessResponse(response, ((OtpTicketIssueAuthenticationToken) result).getIssueResult());
        } catch (OtpUnsupportedChannelException ex) {
            logger.debug("OTP issue rejected (unsupported_channel): {}", ex.getMessage());
            sendErrorResponse(response, HttpStatus.BAD_REQUEST,
                    ERROR_UNSUPPORTED_CHANNEL, ex.getMessage());
        } catch (OtpInvalidIdentityIdException ex) {
            logger.debug("OTP issue rejected (invalid_identity_id): {}", ex.getMessage());
            sendErrorResponse(response, HttpStatus.BAD_REQUEST,
                    ERROR_INVALID_IDENTITY_ID, ex.getMessage());
        } catch (OtpDeliveryFailedException ex) {
            logger.debug("OTP issue failed (delivery_failed): {}", ex.getMessage());
            sendErrorResponse(response, HttpStatus.BAD_GATEWAY,
                    ERROR_DELIVERY_FAILED, ex.getMessage());
        } catch (AuthenticationException ex) {
            logger.debug("OTP issue failed: {}", ex.getMessage());
            sendErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR,
                    ERROR_DELIVERY_FAILED, ex.getMessage());
        }
    }

    private void sendSuccessResponse(HttpServletResponse response, OtpIssueResult result) throws IOException {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        Map<String, Object> body = new HashMap<>();
        body.put("otp_ticket", result.otpTicket());
        if (result.expiresIn() != null) {
            body.put("expires_in", result.expiresIn().toSeconds());
        }
        if (result.retryAfter() != null) {
            body.put("retry_after", result.retryAfter().toSeconds());
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
