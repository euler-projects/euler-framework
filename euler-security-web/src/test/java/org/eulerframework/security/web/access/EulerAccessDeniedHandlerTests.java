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
package org.eulerframework.security.web.access;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies the access-denial-to-error-code mapping behind the JSON envelope.
 * {@link EulerAccessDeniedHandler#resolveError} is a pure function, so this
 * needs no servlet collaborators.
 */
class EulerAccessDeniedHandlerTests {

    @Test
    void invalidCsrfTokenMapsToInvalidCsrfToken() {
        CsrfToken expected = new DefaultCsrfToken("X-XSRF-TOKEN", "_csrf", "expected-token");
        assertEquals("invalid_csrf_token",
                EulerAccessDeniedHandler.resolveError(new InvalidCsrfTokenException(expected, "actual-token")));
    }

    @Test
    void missingCsrfTokenMapsToInvalidCsrfToken() {
        assertEquals("invalid_csrf_token",
                EulerAccessDeniedHandler.resolveError(new MissingCsrfTokenException("actual-token")));
    }

    @Test
    void genericAccessDeniedMapsToAccessDenied() {
        assertEquals("access_denied",
                EulerAccessDeniedHandler.resolveError(new AccessDeniedException("not allowed")));
    }
}
