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
import org.eulerframework.security.authentication.appattest.AppAttestAttestationRegistrationAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.StringUtils;

/**
 * Extracts the App Attest attestation and challenge from an HTTP request and constructs an
 * {@link AppAttestAttestationRegistrationAuthenticationToken}.
 * <p>
 * Both credential carriages are resolved by {@link AppAttestCredentialResolver} and never mixed:
 * the {@code App-Attest-*} headers, or the {@code app_attest_*} form parameters.
 * <p>
 * The key ID is not read from the request: it is derived from the attestation's credential ID
 * during validation, so the client does not need to send it.
 * <p>
 * Returns {@code null} if the attestation or challenge is missing, indicating the request is not
 * an App instance registration request.
 */
public class AppAttestRegistrationAuthenticationConverter implements AuthenticationConverter {

    @Override
    public Authentication convert(HttpServletRequest request) {
        AppAttestCredential credential = AppAttestCredentialResolver.resolve(request);

        if (!StringUtils.hasText(credential.attestation()) || !StringUtils.hasText(credential.challenge())) {
            return null;
        }

        return AppAttestAttestationRegistrationAuthenticationToken.unauthenticated(
                credential.attestation(), credential.challenge());
    }
}
