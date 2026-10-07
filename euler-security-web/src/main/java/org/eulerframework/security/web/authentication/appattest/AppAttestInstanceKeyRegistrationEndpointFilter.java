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

package org.eulerframework.security.web.authentication.appattest;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eulerframework.common.util.jackson.JacksonUtils;
import org.eulerframework.security.authentication.appattest.AppAttestInstanceKeyRegistration;
import org.eulerframework.security.authentication.appattest.AppAttestInstanceKeyRegistrationAuthenticationProvider;
import org.eulerframework.security.authentication.appattest.AppAttestInstanceKeyRegistrationAuthenticationToken;
import org.eulerframework.security.authentication.appattest.DuplicateInstanceKeyException;
import org.eulerframework.security.authentication.appattest.InvalidInstanceKeyException;
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
 * A filter that exposes a {@code POST /app_attest/keys} endpoint for registering the public
 * keys an App instance generates itself.
 * <p>
 * The endpoint is a RESTful collection: one App instance may register several keys, each under a
 * {@code kid} of the caller's own choosing, carried in the submitted JWK and unique across the
 * whole registry. Registration is a {@code POST} and behaves like one &mdash; a key ID already
 * taken is a conflict rather than an overwrite, and the caller picks another. Like the App
 * instance registration endpoint it requires no OAuth client credential and no user session, but
 * it is not anonymous either: an App Attest assertion authenticates the caller as a genuine App
 * instance, and the key is filed under the App Attest KEY that authenticated it.
 * <p>
 * What a registered key may then be used for is not this endpoint's business and is not decided
 * here. The trust it carries is exactly what App Attest proves &mdash; an unmodified installation
 * of a registered app, on genuine Apple hardware, generated this key and holds its private half
 * in the platform's secure area &mdash; and any consumer that wants more than that has to
 * establish the rest itself.
 * <p>
 * It performs <b>key registration only</b>. No account is created, no subject is produced and no
 * login state is established.
 * <p>
 * The App Attest credential is accepted in either carriage (never mixed): the
 * {@code App-Attest-Kid} / {@code App-Attest-Challenge} / {@code App-Attest-Assertion}
 * headers, or the equivalent {@code app_attest_*} form parameters. Only the headers are
 * usable in practice, since the body carries the JWK as JSON. The {@code kid} is required
 * because an assertion's authenticator data does not embed the credential ID.
 * <p>
 * Success response (HTTP 201), the key as registered, which is the submitted JWK reduced to its
 * public members and carrying the caller's own {@code kid}:
 * <pre>
 * {"kty":"EC","crv":"P-256","x":"...","y":"...","alg":"ES256","kid":"..."}
 * </pre>
 * A request whose credential or body is incomplete is answered with {@code 400
 * invalid_request}, as is a body that is not a registrable JWK, carries no {@code kid}, or is
 * larger than one could be; a credential that does not verify with {@code 401
 * key_registration_failed}; a {@code kid} the registry already holds with {@code 409
 * kid_already_registered}.
 *
 * @see AppAttestInstanceKeyRegistrationAuthenticationConverter
 * @see AppAttestInstanceKeyRegistrationAuthenticationProvider
 */
public class AppAttestInstanceKeyRegistrationEndpointFilter extends OncePerRequestFilter {

    private static final Logger logger =
            LoggerFactory.getLogger(AppAttestInstanceKeyRegistrationEndpointFilter.class);

    private final AuthenticationConverter authenticationConverter;
    private final AuthenticationProvider authenticationProvider;
    private final RequestMatcher requestMatcher;

    public AppAttestInstanceKeyRegistrationEndpointFilter(AuthenticationConverter authenticationConverter,
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
    protected void doFilterInternal(
            @Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull FilterChain filterChain) throws ServletException, IOException {
        if (!this.requestMatcher.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // The conversion is inside the try as well as the authentication: the converter is
        // what reads the request body, and a body it cannot use is the same class of answer as
        // a key the provider cannot register.
        try {
            Authentication authRequest = this.authenticationConverter.convert(request);
            if (authRequest == null) {
                sendErrorResponse(response, HttpStatus.BAD_REQUEST,
                        "invalid_request", "Missing required parameters: the App-Attest-Kid, "
                                + "App-Attest-Challenge and App-Attest-Assertion headers, and a JWK request body");
                return;
            }

            Authentication result = this.authenticationProvider.authenticate(authRequest);
            sendSuccessResponse(response, (AppAttestInstanceKeyRegistrationAuthenticationToken) result);
        } catch (InvalidInstanceKeyException ex) {
            logger.debug("Key registration request rejected: {}", ex.getMessage());
            sendErrorResponse(response, HttpStatus.BAD_REQUEST, "invalid_request", ex.getMessage());
        } catch (DuplicateInstanceKeyException ex) {
            // A conflict with what the registry already holds, not a fault of the request or of
            // the credential, so neither of the two answers above describes it. Which key IDs are
            // taken is discoverable by anyone who can authenticate as an App instance, exactly as
            // a taken username is; a fresh UUID is not guessable and a guessable one is no secret.
            logger.debug("Key registration conflicted: {}", ex.getMessage());
            sendErrorResponse(response, HttpStatus.CONFLICT, "kid_already_registered", ex.getMessage());
        } catch (AuthenticationException ex) {
            logger.debug("Key registration failed: {}", ex.getMessage());
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "key_registration_failed", ex.getMessage());
        }
    }

    /**
     * Write the registered key back exactly as the store holds it: it already carries its
     * server-derived {@code kid}, and it was read back after the write rather than taken from
     * the request, so the response is the JWK the registry will actually hand to a consumer.
     */
    private void sendSuccessResponse(HttpServletResponse response,
                                     AppAttestInstanceKeyRegistrationAuthenticationToken result) throws IOException {
        AppAttestInstanceKeyRegistration registration = result.getRegistration();
        response.setStatus(HttpStatus.CREATED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(registration.jwk());
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
