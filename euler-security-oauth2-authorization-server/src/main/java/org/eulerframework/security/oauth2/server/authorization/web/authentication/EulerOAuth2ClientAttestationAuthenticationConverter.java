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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.eulerframework.security.oauth2.core.EulerOAuth2ClientAttestationType;
import org.eulerframework.security.oauth2.server.authorization.authentication.EulerOAuth2ClientAttestationAuthenticationProvider;
import org.eulerframework.security.oauth2.server.authorization.authentication.EulerOAuth2ClientAttestationVerifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.endpoint.PkceParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationConverter;

import org.eulerframework.security.oauth2.core.EulerClientAuthenticationMethod;
import org.eulerframework.security.oauth2.core.EulerClientAttestationProof;
import org.eulerframework.security.oauth2.core.endpoint.EulerOAuth2HeaderNames;
import org.eulerframework.security.oauth2.core.endpoint.EulerOAuth2ParameterNames;
import org.springframework.util.StringUtils;

/**
 * The {@link AuthenticationConverter} for {@code attest_jwt_client_auth}, registered at the
 * <i>end</i> of the {@code OAuth2ClientAuthenticationFilter} converter chain, so it claims only a
 * request no traditional converter claimed &mdash; one whose attestation is therefore its sole
 * credential rather than an overlay on one.
 * <p>
 * {@link #convert} collects the attestation and the grant parameters into a token; it never verifies.
 * Verification, which consumes the one-time challenge and resolves the {@code client_id}, belongs to
 * {@link EulerOAuth2ClientAttestationAuthenticationProvider}.
 *
 * @see EulerClientAuthenticationMethod#ATTEST_JWT_CLIENT_AUTH
 */
public final class EulerOAuth2ClientAttestationAuthenticationConverter implements AuthenticationConverter {

    /**
     * Placeholder principal for a token this converter produces. An {@code attest_jwt_client_auth}
     * client is identified by its attestation, not by a {@code client_id} form parameter, so the
     * real {@code client_id} is resolved during verification by the provider. The placeholder only
     * has to be a printable string to pass {@code OAuth2ClientAuthenticationFilter}'s client
     * identifier syntax check.
     */
    private static final String ATTESTATION_PRINCIPAL_PLACEHOLDER = "(attestation)";

    @Override
    public Authentication convert(HttpServletRequest request) {
        // Runs last, so a request presenting a traditional credential (including a PKCE-shaped one)
        // was claimed earlier and never reaches here.
        if (!carriesAttestationSignal(request)) {
            return null;
        }

        Map<String, Object> additionalParameters = new HashMap<>();
        collectAttestationParams(request, additionalParameters);
        // Carry the grant parameters so the provider can enforce PKCE (code_verifier) via
        // CodeVerifierAuthenticatorAccessor#authenticateIfAvailable for an authorization_code grant.
        collectGrantParams(request, additionalParameters);
        String principal = additionalParameters.containsKey(OAuth2ParameterNames.CLIENT_ID)
                ? (String) additionalParameters.get(OAuth2ParameterNames.CLIENT_ID)
                : ATTESTATION_PRINCIPAL_PLACEHOLDER;
        return new OAuth2ClientAuthenticationToken(
                principal, EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH, null, additionalParameters);
    }

    /**
     * Collect the raw attestation data from the request &mdash; no parsing, no verification, no DB
     * lookup &mdash; into the parameter map consumed by
     * {@link EulerOAuth2ClientAttestationVerifier#verify(Map)}.
     * <p>
     * It deliberately ignores any traditional credential the request may also carry, so it serves
     * both to build an attestation-only token and to collect an attestation laid over a traditional
     * authentication.
     *
     * @param request              the token endpoint request
     * @param additionalParameters the map to collect into; left untouched if the request carries no
     *                             attestation signal
     */
    public static void collectAttestationParams(HttpServletRequest request, Map<String, Object> additionalParameters) {
        if (!carriesAttestationSignal(request)) {
            return;
        }

        String attestationJwt = request.getHeader(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION);
        String attestationPopJwt = request.getHeader(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_POP);
        String attestationType = request.getHeader(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_TYPE);

        EulerOAuth2ClientAttestationType clientAttestationType = attestationType != null
                ? EulerOAuth2ClientAttestationType.parse(attestationType)
                : EulerOAuth2ClientAttestationType.JWT;

        additionalParameters.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_TYPE, clientAttestationType);

        if (EulerOAuth2ClientAttestationType.JWT.equals(clientAttestationType)) {
            // Unlike the draft, we treat OAuth-Client-Attestation as an optional header.
            // As long as the public key has not changed, it can be omitted.
            // However, if OAuth-Client-Attestation is omitted, the PoP JWT header must carry a verified kid.
            copyOptional(attestationJwt, EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION, additionalParameters);
            copyRequired(attestationPopJwt, EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_POP, additionalParameters);
        } else if (EulerOAuth2ClientAttestationType.APPLE_APP_ATTEST.equals(clientAttestationType)) {
            if (isHeaderCarried(request)) {
                convertAppleAppAttestHeaders(request, additionalParameters);
            } else {
                convertAppleAppAttestFormParameters(request, additionalParameters);
            }
        }

        // Optional request client_id (for the RFC 6749 Section 6.3 consistency check in the verifier)
        copyOptional(request, OAuth2ParameterNames.CLIENT_ID, additionalParameters);
    }

    /**
     * Whether the request carries any client attestation signal, i.e. at least one of the
     * {@code OAuth-Client-Attestation}, {@code -PoP} or {@code -Type} headers. A cheap presence
     * check that neither parses nor validates, so it is safe to use as a routing signal.
     *
     * @param request the token endpoint request
     * @return {@code true} if an attestation header is present
     */
    public static boolean carriesAttestationSignal(HttpServletRequest request) {
        return request.getHeader(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION) != null
                || request.getHeader(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_POP) != null
                || request.getHeader(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_TYPE) != null;
    }

    /**
     * Whether the Apple App Attest data is carried in headers rather than in form parameters.
     * <p>
     * The {@code OAuth-Client-Attestation-Assertion} header is the switch. The two carriages are
     * never mixed: a request presenting that header is read entirely from headers, and any App
     * Attest form parameter it may also carry is ignored.
     *
     * @param request the token endpoint request
     * @return {@code true} if the header carriage applies
     */
    public static boolean isHeaderCarried(HttpServletRequest request) {
        return StringUtils.hasText(
                request.getHeader(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_ASSERTION));
    }

    /**
     * Header carriage, the only non-deprecated one: assertion-only, since App instance registration
     * happens at the dedicated registration endpoint and no attestation header exists.
     */
    private static void convertAppleAppAttestHeaders(HttpServletRequest request,
                                                     Map<String, Object> additionalParams) {
        copyRequiredHeader(request, EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_CHALLENGE, additionalParams);
        copyRequiredHeader(request, EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_KID, additionalParams);
        copyRequiredHeader(request, EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_ASSERTION, additionalParams);
    }

    /**
     * Deprecated form-parameter carriage, retained for released clients; see
     * {@link EulerOAuth2ParameterNames#ATTESTATION}.
     * <p>
     * Unlike the header carriage this one may also carry an attestation, so {@code attestation}
     * and {@code assertion} are not mutually exclusive and the three combinations remain valid:
     * attestation only (registers the App Attest KEY and authenticates the client), assertion only
     * (fast path for an already-registered KEY, which is the sole case requiring {@code kid}),
     * or both. Values are stored under the canonical header keys so that downstream components
     * stay transport-agnostic; only {@code attestation} keeps its own key, having no header
     * analog.
     */
    private static void convertAppleAppAttestFormParameters(HttpServletRequest request,
                                                            Map<String, Object> additionalParams) {
        copyRequiredFormParameter(request, EulerOAuth2ParameterNames.CHALLENGE,
                EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_CHALLENGE, additionalParams);

        String attestation = request.getParameter(EulerOAuth2ParameterNames.ATTESTATION);
        if (!StringUtils.hasText(attestation)
                && !StringUtils.hasText(request.getParameter(EulerOAuth2ParameterNames.ASSERTION))) {
            throw newError(OAuth2ErrorCodes.INVALID_REQUEST,
                    EulerOAuth2ParameterNames.ATTESTATION + " or " + EulerOAuth2ParameterNames.ASSERTION, null);
        }
        copyOptional(attestation, EulerOAuth2ParameterNames.ATTESTATION, additionalParams);
        copyOptionalFormParameter(request, EulerOAuth2ParameterNames.ASSERTION,
                EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_ASSERTION, additionalParams);

        // The kid is derivable from an attestation (its credentialId), so it is required only
        // for the assertion-only request, whose authenticator data carries no credentialId.
        if (!StringUtils.hasText(attestation)) {
            copyRequiredFormParameter(request, EulerOAuth2ParameterNames.KEY_ID,
                    EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_KID, additionalParams);
        }
    }

    /**
     * Resolve which proof a request presented from the parameters {@link #collectAttestationParams}
     * collected for it: {@link EulerClientAttestationProof#ATTESTATION} when an attestation is among
     * them, {@link EulerClientAttestationProof#ASSERTION} otherwise.
     * <p>
     * Only the deprecated form carriage can carry an Apple attestation, so for that variant the
     * {@code attestation} key's presence is itself the discriminator. An absent type key means the
     * request carried no {@code OAuth-Client-Attestation-Type} header, which defaults to
     * {@link EulerOAuth2ClientAttestationType#JWT}.
     *
     * @param collectedParams the parameters collected by {@link #collectAttestationParams}
     * @return the proof the server will act on for this request
     * @deprecated compatibility logic; see
     * {@link org.eulerframework.security.core.userdetails.EulerDeviceUserDetailsService}. Removed
     * once the device-to-user mapping is retired.
     */
    @Deprecated
    public static EulerClientAttestationProof resolveProof(Map<String, Object> collectedParams) {
        EulerOAuth2ClientAttestationType clientAttestationType =
                (EulerOAuth2ClientAttestationType) collectedParams
                        .get(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_TYPE);

        String attestationKey = EulerOAuth2ClientAttestationType.APPLE_APP_ATTEST.equals(clientAttestationType)
                ? EulerOAuth2ParameterNames.ATTESTATION
                : EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION;

        return StringUtils.hasText((String) collectedParams.get(attestationKey))
                ? EulerClientAttestationProof.ATTESTATION
                : EulerClientAttestationProof.ASSERTION;
    }

    private static void copyOptional(Object value, String paramName,
                                     Map<String, Object> target) {
        if (value != null) {
            target.put(paramName, value);
        }
    }

    /**
     * Collect the grant parameters PKCE enforcement needs downstream: {@code grant_type}, {@code code}
     * and {@code code_verifier}. For a grant other than {@code authorization_code} these are simply
     * absent.
     */
    private static void collectGrantParams(HttpServletRequest request, Map<String, Object> target) {
        copyOptional(request, OAuth2ParameterNames.GRANT_TYPE, target);
        copyOptional(request, OAuth2ParameterNames.CODE, target);
        copyOptional(request, PkceParameterNames.CODE_VERIFIER, target);
    }

    private static void copyOptional(HttpServletRequest request, String paramName,
                                     Map<String, Object> target) {
        String value = request.getParameter(paramName);
        if (value != null) {
            target.put(paramName, value);
        }
    }

    private static void copyRequiredHeader(HttpServletRequest request, String headerName,
                                           Map<String, Object> target) {
        String value = request.getHeader(headerName);
        if (!StringUtils.hasText(value)) {
            throw newError(OAuth2ErrorCodes.INVALID_REQUEST, headerName, null);
        }
        target.put(headerName, value);
    }

    /**
     * Copy a deprecated form parameter under its canonical header key, so that downstream
     * components stay transport-agnostic.
     */
    private static void copyRequiredFormParameter(HttpServletRequest request, String paramName, String targetKey,
                                                  Map<String, Object> target) {
        String value = request.getParameter(paramName);
        if (!StringUtils.hasText(value)) {
            throw newError(OAuth2ErrorCodes.INVALID_REQUEST, paramName, null);
        }
        target.put(targetKey, value);
    }

    private static void copyOptionalFormParameter(HttpServletRequest request, String paramName, String targetKey,
                                                  Map<String, Object> target) {
        String value = request.getParameter(paramName);
        if (StringUtils.hasText(value)) {
            target.put(targetKey, value);
        }
    }

    private static void copyRequired(Object value, String paramName,
                                     Map<String, Object> target) {
        if (value == null) {
            throw newError(OAuth2ErrorCodes.INVALID_REQUEST, paramName, null);
        }
        target.put(paramName, value);
    }

    private static OAuth2AuthenticationException newError(String errorCode, String parameterName, String errorUri) {
        OAuth2Error error = new OAuth2Error(errorCode, "OAuth 2.0 Parameter: " + parameterName, errorUri);
        return new OAuth2AuthenticationException(error);
    }
}
