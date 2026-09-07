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
package org.eulerframework.security.web.access;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eulerframework.security.web.authentication.SecurityJsonResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;

/**
 * {@link AccessDeniedHandlerImpl} that keeps the conventional redirect /
 * {@code sendError} behaviour for browser navigations, but answers a
 * JSON-preferring request (one sending {@code Accept: application/json}) with
 * the shared error envelope from {@link SecurityJsonResponses} instead of
 * routing through {@code sendError -> /error}.
 *
 * <p>This gives a fetch client one consistent JSON contract across login
 * failures and access denials &mdash; notably CSRF failures, which surface
 * here as {@link InvalidCsrfTokenException} / {@link MissingCsrfTokenException}
 * &mdash; rather than the global HTML / error-controller shape.
 */
public class EulerAccessDeniedHandler extends AccessDeniedHandlerImpl {
    private final Logger logger = LoggerFactory.getLogger(EulerAccessDeniedHandler.class);
    
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException,
            ServletException {
        this.logger.warn("Access denied: {}", accessDeniedException.getMessage());
        if (!response.isCommitted() && SecurityJsonResponses.prefersJson(request)) {
            SecurityJsonResponses.writeError(response, HttpStatus.FORBIDDEN, resolveError(accessDeniedException));
            return;
        }
        super.handle(request, response, accessDeniedException);
    }

    /**
     * Map an access denial to the machine-readable {@code error} code carried
     * in the JSON envelope. CSRF failures get a distinct code so a client can
     * re-fetch a token and retry; everything else is a generic denial.
     */
    static String resolveError(AccessDeniedException accessDeniedException) {
        if (accessDeniedException instanceof InvalidCsrfTokenException
                || accessDeniedException instanceof MissingCsrfTokenException) {
            return "invalid_csrf_token";
        }
        return "access_denied";
    }
}
