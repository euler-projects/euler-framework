/*
 * Copyright 2013-2026 the original author or authors.
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
package org.eulerframework.security.oauth2.server.authorization.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eulerframework.security.authentication.GeneratedChallenge;
import org.springframework.security.core.Authentication;

import java.io.IOException;

/**
 * A strategy for handling a successfully generated challenge.
 *
 * @see OAuth2ChallengeEndpointFilter
 */
@FunctionalInterface
public interface ChallengeGenerationSuccessHandler {

    /**
     * Called when a challenge has been successfully generated.
     *
     * @param request              the request that resulted in a successful challenge generation
     * @param response             the response
     * @param clientAuthentication the authenticated OAuth2 client
     * @param challenge            the generated challenge containing challenge and format
     * @throws IOException      if an I/O error occurs
     * @throws ServletException if a servlet error occurs
     */
    void onChallengeGenerated(HttpServletRequest request, HttpServletResponse response,
                               Authentication clientAuthentication, GeneratedChallenge challenge)
            throws IOException, ServletException;
}
