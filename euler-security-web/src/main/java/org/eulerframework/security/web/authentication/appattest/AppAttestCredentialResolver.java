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

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * Resolves the App Attest credential fields from a request. This is the single parsing point shared
 * by every endpoint that authenticates with Apple App Attest, across both business domains:
 * <ul>
 *     <li>the App Attest domain ({@code /app_attest/**}), which uses this carriage natively;</li>
 *     <li>the OAuth domain ({@code /oauth2/**}), whose {@code apple_app_attest} client attestation
 *     variant reuses the very same carriage rather than defining OAuth-specific header names.</li>
 * </ul>
 * <p>
 * Two carriages are supported and <b>never mixed</b> within one request: the {@code App-Attest-*}
 * HTTP headers and the {@code app_attest_*} form parameters named by
 * {@link AppAttestParameterNames}. The header carriage applies as soon as any {@code App-Attest-*}
 * header carries a value; otherwise the form carriage applies if any {@code app_attest_*} parameter
 * does; otherwise the result is {@link AppAttestCredential#absent()}, which lets an OAuth domain
 * caller fall back to its own deprecated vocabulary.
 * <p>
 * Resolution is deliberately lenient: it only reports which fields the request carried, leaving
 * every required-field check to the caller, because the required subset differs per endpoint.
 *
 * @see AppAttestCredential
 * @see AppAttestParameterNames
 */
public final class AppAttestCredentialResolver {

    private AppAttestCredentialResolver() {
    }

    /**
     * Resolve the App Attest credential fields from whichever single carriage the request uses.
     *
     * @param request the request to read from
     * @return the resolved fields, or {@link AppAttestCredential#absent()} if the request carries
     * neither carriage
     */
    public static AppAttestCredential resolve(HttpServletRequest request) {
        if (isHeaderCarried(request)) {
            return new AppAttestCredential(
                    request.getHeader(AppAttestParameterNames.HEADER_ATTESTATION),
                    request.getHeader(AppAttestParameterNames.HEADER_ASSERTION),
                    request.getHeader(AppAttestParameterNames.HEADER_KID),
                    request.getHeader(AppAttestParameterNames.HEADER_CHALLENGE));
        }
        if (isFormCarried(request)) {
            return new AppAttestCredential(
                    request.getParameter(AppAttestParameterNames.PARAM_ATTESTATION),
                    request.getParameter(AppAttestParameterNames.PARAM_ASSERTION),
                    request.getParameter(AppAttestParameterNames.PARAM_KID),
                    request.getParameter(AppAttestParameterNames.PARAM_CHALLENGE));
        }
        return AppAttestCredential.absent();
    }

    /**
     * Whether the request carries the App Attest credential in {@code App-Attest-*} headers.
     *
     * @param request the request to inspect
     * @return {@code true} if any {@code App-Attest-*} header has text
     */
    public static boolean isHeaderCarried(HttpServletRequest request) {
        return StringUtils.hasText(request.getHeader(AppAttestParameterNames.HEADER_ATTESTATION))
                || StringUtils.hasText(request.getHeader(AppAttestParameterNames.HEADER_ASSERTION))
                || StringUtils.hasText(request.getHeader(AppAttestParameterNames.HEADER_KID))
                || StringUtils.hasText(request.getHeader(AppAttestParameterNames.HEADER_CHALLENGE));
    }

    /**
     * Whether the request carries the App Attest credential in {@code app_attest_*} form parameters.
     *
     * @param request the request to inspect
     * @return {@code true} if any {@code app_attest_*} parameter has text
     */
    public static boolean isFormCarried(HttpServletRequest request) {
        return StringUtils.hasText(request.getParameter(AppAttestParameterNames.PARAM_ATTESTATION))
                || StringUtils.hasText(request.getParameter(AppAttestParameterNames.PARAM_ASSERTION))
                || StringUtils.hasText(request.getParameter(AppAttestParameterNames.PARAM_KID))
                || StringUtils.hasText(request.getParameter(AppAttestParameterNames.PARAM_CHALLENGE));
    }
}
