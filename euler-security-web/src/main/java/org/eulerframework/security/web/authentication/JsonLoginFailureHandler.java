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

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.util.Assert;

import java.io.IOException;

/**
 * An {@link AuthenticationFailureHandler} that answers a JSON-preferring
 * login request with an error envelope from {@link SecurityJsonResponses} and
 * delegates every other request to a conventional handler (typically a
 * {@code SimpleUrlAuthenticationFailureHandler} redirecting back to the login
 * page).
 *
 * <p>Pairs with {@link JsonLoginRedirectStrategy} on the success side so an
 * XHR / fetch client receives a uniform JSON contract for both outcomes,
 * while the server-rendered login page keeps its redirect-and-show-error
 * behaviour.
 *
 * <p>A failure is mapped to a real HTTP status plus a machine-readable
 * {@code error} code (see {@link #resolve}); no human message is emitted, so
 * the client localises from the code and account-state wording is not leaked
 * beyond the code itself.
 */
public class JsonLoginFailureHandler implements AuthenticationFailureHandler {

    private final AuthenticationFailureHandler delegate;

    public JsonLoginFailureHandler(AuthenticationFailureHandler delegate) {
        Assert.notNull(delegate, "delegate must not be null");
        this.delegate = delegate;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {
        if (SecurityJsonResponses.prefersJson(request)) {
            Failure failure = resolve(exception);
            SecurityJsonResponses.writeError(response, failure.status(), failure.error());
            return;
        }
        this.delegate.onAuthenticationFailure(request, response, exception);
    }

    /**
     * Map an authentication failure to the HTTP status and machine-readable
     * {@code error} code the JSON envelope carries.
     *
     * <p>Specific subtypes are matched before their supertypes. Credential
     * failures - a bad password, a wrong or expired one-time code, expired
     * credentials, and the user-not-found case Spring folds into bad
     * credentials - deliberately carry no dedicated code: they fall through
     * to the generic 401 so the envelope reveals nothing beyond
     * "authentication failed", leaving each client to word the failure per
     * its own form context. The remaining account-status failures keep
     * distinct 403 codes, a deliberate UX-over-enumeration trade-off;
     * server-side problems become 500 so a client does not read them as bad
     * credentials and retry forever.
     */
    static Failure resolve(AuthenticationException exception) {
        if (exception instanceof LockedException) {
            return new Failure(HttpStatus.FORBIDDEN, "account_locked");
        }
        if (exception instanceof DisabledException) {
            return new Failure(HttpStatus.FORBIDDEN, "account_disabled");
        }
        if (exception instanceof AccountExpiredException) {
            return new Failure(HttpStatus.FORBIDDEN, "account_expired");
        }
        // InternalAuthenticationServiceException is a subtype of
        // AuthenticationServiceException, so this branch covers both.
        if (exception instanceof AuthenticationServiceException
                || exception instanceof ProviderNotFoundException) {
            return new Failure(HttpStatus.INTERNAL_SERVER_ERROR, "server_error");
        }
        return new Failure(HttpStatus.UNAUTHORIZED, "authentication_failed");
    }

    /**
     * The resolved HTTP status and {@code error} code for a failure.
     */
    record Failure(HttpStatus status, String error) {
    }
}
