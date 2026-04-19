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

package org.eulerframework.security.oauth2.core;

import org.eulerframework.security.oauth2.core.endpoint.EulerOAuth2ParameterNames;

import jakarta.annotation.Nonnull;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

public record EulerOAuth2ClientAttestationType(String value) {

    /**
     * Standard PoP JWT as defined in the draft.
     */
    public static final EulerOAuth2ClientAttestationType JWT = new EulerOAuth2ClientAttestationType("jwt");

    /**
     * Apple App Attest used as Client Attestation.
     * When this type is specified, the assertion parameters
     * ({@link EulerOAuth2ParameterNames#KEY_ID},
     * {@link EulerOAuth2ParameterNames#ASSERTION},
     * {@link EulerOAuth2ParameterNames#CHALLENGE})
     * are sent as body parameters.
     */
    public static final EulerOAuth2ClientAttestationType APP_ATTEST = new EulerOAuth2ClientAttestationType("app-attest");

    public static EulerOAuth2ClientAttestationType parse(String value) {
        if (JWT.value().equalsIgnoreCase(value)) {
            return JWT;
        } else if (APP_ATTEST.value().equalsIgnoreCase(value)) {
            return APP_ATTEST;
        } else {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(EulerOAuth2ErrorCodes.INVALID_CLIENT_ATTESTATION,
                            "Unsupported attestation type: " + value, null));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        EulerOAuth2ClientAttestationType that = (EulerOAuth2ClientAttestationType) obj;
        return this.value().equals(that.value());
    }

    @Override
    public int hashCode() {
        return this.value().hashCode();
    }

    @Nonnull
    @Override
    public String toString() {
        return "EulerOAuth2ClientAttestationType{" + "value='" + this.value + '\'' + '}';
    }
}
