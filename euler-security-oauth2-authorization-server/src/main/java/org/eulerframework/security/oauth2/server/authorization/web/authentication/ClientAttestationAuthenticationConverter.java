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
package org.eulerframework.security.oauth2.server.authorization.web.authentication;

import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import org.eulerframework.security.authentication.apple.AppAttestRegistration;
import org.eulerframework.security.authentication.apple.AppAttestRegistrationService;
import org.eulerframework.security.oauth2.core.EulerClientAuthenticationMethod;
import org.eulerframework.security.oauth2.core.EulerOAuth2ErrorCodes;
import org.eulerframework.security.oauth2.core.endpoint.EulerOAuth2ParameterNames;

/**
 * An {@link AuthenticationConverter} that extracts Client Attestation data from
 * the request and creates an {@link OAuth2ClientAuthenticationToken} for
 * {@code attest_jwt_client_auth} clients.
 * <p>
 * This converter is registered <b>after</b> all default client authentication converters
 * in {@code OAuth2ClientAuthenticationFilter}. It only matches when no standard converter
 * (e.g., {@code ClientSecretBasicAuthenticationConverter}) has already matched.
 * <p>
 * The converter resolves the {@code client_id} by looking up the {@code key_id} from the
 * PoP data via {@link AppAttestRegistrationService}. All attestation data is stored in the
 * token's {@code additionalParameters} for the companion
 * {@link org.eulerframework.security.oauth2.server.authorization.authentication.ClientAttestationAuthenticationProvider}
 * to verify.
 *
 * @see EulerOAuth2ParameterNames
 * @see EulerClientAuthenticationMethod#ATTEST_JWT_CLIENT_AUTH
 */
public final class ClientAttestationAuthenticationConverter implements AuthenticationConverter {

    private final AppAttestRegistrationService appAttestRegistrationService;

    public ClientAttestationAuthenticationConverter(AppAttestRegistrationService appAttestRegistrationService) {
        Assert.notNull(appAttestRegistrationService, "appAttestRegistrationService must not be null");
        this.appAttestRegistrationService = appAttestRegistrationService;
    }

    @Override
    public Authentication convert(HttpServletRequest request) {
        // 1. Check for attestation signal
        String attestationJwt = request.getHeader(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION);
        String popData = request.getHeader(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_POP);
        String popTypeHeader = request.getHeader(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_POP_TYPE);

        boolean hasAttestation = attestationJwt != null || popData != null || popTypeHeader != null;
        if (!hasAttestation) {
            return null;
        }

        String popType = popTypeHeader != null ? popTypeHeader : EulerOAuth2ParameterNames.POP_TYPE_JWT;

        // 2. Extract keyId based on PoP type
        String keyId;
        Map<String, Object> additionalParams = new LinkedHashMap<>();

        switch (popType) {
            case EulerOAuth2ParameterNames.POP_TYPE_JWT -> {
                if (popData == null) {
                    throw attestationError("PoP-Type=jwt but missing "
                            + EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_POP + " header");
                }
                try {
                    SignedJWT signedJWT = SignedJWT.parse(popData);
                    keyId = signedJWT.getHeader().getKeyID();
                } catch (ParseException e) {
                    throw attestationError("Failed to parse PoP JWT: " + e.getMessage());
                }
                if (keyId == null) {
                    throw attestationError("PoP JWT missing kid in header");
                }
            }
            case EulerOAuth2ParameterNames.POP_TYPE_APP_ATTEST -> {
                keyId = request.getParameter(EulerOAuth2ParameterNames.KEY_ID);
                String assertion = request.getParameter(EulerOAuth2ParameterNames.ASSERTION);
                String challenge = request.getParameter(EulerOAuth2ParameterNames.CHALLENGE);
                if (!StringUtils.hasText(keyId) || !StringUtils.hasText(assertion)
                        || !StringUtils.hasText(challenge)) {
                    throw attestationError("App-Attest PoP requires "
                            + EulerOAuth2ParameterNames.KEY_ID + ", "
                            + EulerOAuth2ParameterNames.ASSERTION + ", and "
                            + EulerOAuth2ParameterNames.CHALLENGE + " parameters");
                }
                additionalParams.put(EulerOAuth2ParameterNames.ASSERTION, assertion);
                additionalParams.put(EulerOAuth2ParameterNames.CHALLENGE, challenge);
            }
            default -> throw attestationError("Unsupported PoP-Type: " + popType);
        }

        // 3. Look up registration to resolve clientId
        AppAttestRegistration registration = this.appAttestRegistrationService.findByKeyId(keyId);
        if (registration == null) {
            throw attestationError("Unknown key_id: " + keyId);
        }
        String clientId = registration.getClientId();
        if (clientId == null) {
            throw attestationError("No client_id bound for key_id: " + keyId);
        }

        // RFC6749 compliance: if request carries client_id, it must match
        String requestClientId = request.getParameter(OAuth2ParameterNames.CLIENT_ID);
        if (requestClientId != null && !requestClientId.equals(clientId)) {
            throw new OAuth2AuthenticationException(new OAuth2Error(
                    OAuth2ErrorCodes.INVALID_CLIENT, "client_id mismatch", null));
        }

        // 4. Store all attestation data for the provider
        if (attestationJwt != null) {
            additionalParams.put(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION, attestationJwt);
        }
        if (popData != null) {
            additionalParams.put(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_POP, popData);
        }
        additionalParams.put(EulerOAuth2ParameterNames.OAUTH_CLIENT_ATTESTATION_POP_TYPE, popType);
        additionalParams.put(EulerOAuth2ParameterNames.KEY_ID, keyId);

        return new OAuth2ClientAuthenticationToken(clientId,
                EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH, null, additionalParams);
    }

    private static OAuth2AuthenticationException attestationError(String description) {
        return new OAuth2AuthenticationException(
                new OAuth2Error(EulerOAuth2ErrorCodes.INVALID_CLIENT_ATTESTATION, description, null));
    }
}
