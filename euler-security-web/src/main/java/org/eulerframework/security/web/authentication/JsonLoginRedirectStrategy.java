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
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;

import java.io.IOException;

/**
 * A {@link RedirectStrategy} that answers a JSON-preferring login request
 * with the success envelope from {@link SecurityJsonResponses} (HTTP 200 +
 * {@code {redirect_url}}) instead of issuing a 302, and delegates every
 * other request to a conventional redirect.
 *
 * <p>Installed on a {@code SimpleUrlAuthenticationSuccessHandler} (or any
 * {@code AbstractAuthenticationTargetUrlRequestHandler}), it reuses that
 * handler's target-URL resolution verbatim &mdash; the
 * {@code targetUrlParameter}, saved request and default target all keep
 * working &mdash; and changes only how the final URL is emitted. A
 * traditional form post, which does not send
 * {@code Accept: application/json}, is redirected exactly as before.
 */
public class JsonLoginRedirectStrategy implements RedirectStrategy {

    private final RedirectStrategy delegate;

    public JsonLoginRedirectStrategy() {
        this(new DefaultRedirectStrategy());
    }

    public JsonLoginRedirectStrategy(RedirectStrategy delegate) {
        this.delegate = delegate;
    }

    @Override
    public void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url)
            throws IOException {
        if (SecurityJsonResponses.prefersJson(request)) {
            SecurityJsonResponses.writeSuccess(response, url);
            return;
        }
        this.delegate.sendRedirect(request, response, url);
    }
}
