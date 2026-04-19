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
import org.eulerframework.security.authentication.appattest.DeviceAppAttestationRegistrationAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.StringUtils;

/**
 * Extracts {@code key_id}, {@code attestation}, and {@code challenge} parameters from
 * an HTTP request and constructs a {@link DeviceAppAttestationRegistrationAuthenticationToken}.
 * <p>
 * Returns {@code null} if any required parameter is missing, indicating the request
 * is not a device attestation registration request.
 */
public class AppAttestRegistrationAuthenticationConverter implements AuthenticationConverter {

    private static final String PARAM_KEY_ID = "key_id";
    private static final String PARAM_ATTESTATION = "attestation";
    private static final String PARAM_CHALLENGE = "challenge";

    @Override
    public Authentication convert(HttpServletRequest request) {
        String keyId = request.getParameter(PARAM_KEY_ID);
        String attestation = request.getParameter(PARAM_ATTESTATION);
        String challenge = request.getParameter(PARAM_CHALLENGE);

        if (!StringUtils.hasText(keyId) || !StringUtils.hasText(attestation) || !StringUtils.hasText(challenge)) {
            return null;
        }

        return DeviceAppAttestationRegistrationAuthenticationToken.unauthenticated(keyId, attestation, challenge);
    }
}
