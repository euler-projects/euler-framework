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

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.eulerframework.security.oauth2.core.EulerClientAttestationProof;
import org.eulerframework.security.oauth2.core.EulerOAuth2ClientAttestationType;
import org.eulerframework.security.oauth2.server.authorization.authentication.EulerOAuth2ClientAttestationVerifier;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.StringUtils;

import org.eulerframework.security.oauth2.core.EulerClientAuthenticationMethod;
import org.eulerframework.security.oauth2.core.endpoint.EulerOAuth2HeaderNames;
import org.eulerframework.security.oauth2.core.endpoint.EulerOAuth2ParameterNames;
import org.eulerframework.security.web.authentication.appattest.AppAttestCredential;
import org.eulerframework.security.web.authentication.appattest.AppAttestCredentialResolver;
import org.eulerframework.security.web.authentication.appattest.AppAttestParameterNames;

/**
 * Reads a client attestation off a token endpoint request and normalizes it into a parameter map,
 * deciding along the way which client authentication mechanism the request asked for.
 * <p>
 * Three components need this and none of them owns it: {@link EulerOAuth2ClientAttestationAuthenticationConverter}
 * to build an attestation-only token, {@link EulerAttestationEnrichingPublicClientAuthenticationConverter}
 * to lay an attestation over a public-client token, and
 * {@link EulerOAuth2ClientAttestationAuthenticationSuccessHandler} to verify one laid over a
 * traditional authentication. Keeping it here stops those three from depending on a sibling
 * converter for request parsing.
 * <p>
 * This class also defines the normalized parameter format &mdash; every carriage lands on the same
 * canonical keys so that {@link EulerOAuth2ClientAttestationVerifier} stays transport-agnostic
 * &mdash; and so it owns reading that format back, which is what {@link #resolveProof} does.
 *
 * @see EulerClientAuthenticationMethod#ATTEST_JWT_CLIENT_AUTH
 * @see EulerClientAuthenticationMethod#ATTEST_APPATTEST_CLIENT_AUTH
 */
public final class OAuth2ClientAttestationUtils {

    /**
     * Collect the raw attestation data from the request &mdash; no parsing, no verification, no DB
     * lookup &mdash; into the parameter map consumed by
     * {@link EulerOAuth2ClientAttestationVerifier#verify(Map, ClientAuthenticationMethod)}.
     * <p>
     * It deliberately ignores any traditional credential the request may also carry, so it serves
     * both to build an attestation-only token and to collect an attestation laid over a traditional
     * authentication.
     * <p>
     * The resolved {@link ClientAuthenticationMethod} is handed back to the caller rather than left
     * for it to dig out of the map: it is what an attestation-only token carries, and what the
     * verifier dispatches on, so nothing downstream has to ask the parameters which mechanism they
     * came from.
     *
     * @param request              the token endpoint request
     * @param additionalParameters the map to collect into; left untouched if the request carries no
     *                             attestation signal
     * @return the client authentication method the request resolved to, or {@code null} when it
     *         carried no attestation signal and nothing was collected
     */
    public static ClientAuthenticationMethod collectAttestationParams(HttpServletRequest request,
                                                                     Map<String, Object> additionalParameters) {
        if (!carriesAttestationSignal(request)) {
            return null;
        }

        ClientAuthenticationMethod method = resolveClientAuthenticationMethod(request);

        if (EulerClientAuthenticationMethod.ATTEST_APPATTEST_CLIENT_AUTH.equals(method)) {
            AppAttestCredential credential = AppAttestCredentialResolver.resolve(request);
            if (credential.isPresent()) {
                convertAppleAppAttest(credential, additionalParameters);
            } else {
                // Released clients only: the deprecated form vocabulary is reachable solely through
                // the deprecated type header, which is what identified this request as Apple's.
                convertDeprecatedAppleAppAttestFormParameters(request, additionalParameters);
            }
        } else {
            // The draft's standard variant. Unlike the draft, we treat OAuth-Client-Attestation as an
            // optional header: as long as the public key has not changed it can be omitted, but then
            // the PoP JWT header must carry a verified kid.
            copyOptional(request.getHeader(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION),
                    EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION, additionalParameters);
            copyRequired(request.getHeader(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_POP),
                    EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_POP, additionalParameters);
        }

        // Optional request client_id (for the RFC 6749 Section 6.3 consistency check in the verifier)
        copyOptional(request, OAuth2ParameterNames.CLIENT_ID, additionalParameters);
        return method;
    }

    /**
     * Whether the request carries any client attestation signal: one of the
     * {@code OAuth-Client-Attestation}, {@code -PoP} or deprecated {@code -Type} headers, or an
     * Apple App Attest credential in either of its carriages. A cheap presence check that neither
     * parses nor validates, so it is safe to use as a routing signal.
     *
     * @param request the token endpoint request
     * @return {@code true} if an attestation signal is present
     */
    public static boolean carriesAttestationSignal(HttpServletRequest request) {
        return request.getHeader(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION) != null
                || request.getHeader(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_POP) != null
                || request.getHeader(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_TYPE) != null
                || AppAttestCredentialResolver.isHeaderCarried(request)
                || AppAttestCredentialResolver.isFormCarried(request);
    }

    /**
     * Resolve which proof a request presented from the parameters {@link #collectAttestationParams}
     * collected for it: {@link EulerClientAttestationProof#ATTESTATION} when an attestation is among
     * them, {@link EulerClientAttestationProof#ASSERTION} otherwise.
     * <p>
     * Each mechanism normalizes its attestation onto its own key and no request ever carries two
     * mechanisms, so which key holds it needs no mechanism lookup.
     *
     * @param collectedParams the parameters collected by {@link #collectAttestationParams}
     * @return the proof the server will act on for this request
     * @deprecated compatibility logic; see
     * {@link org.eulerframework.security.core.userdetails.EulerDeviceUserDetailsService}. Removed
     * once the device-to-user mapping is retired.
     */
    @Deprecated
    public static EulerClientAttestationProof resolveProof(Map<String, Object> collectedParams) {
        return StringUtils.hasText((String) collectedParams.get(AppAttestParameterNames.HEADER_ATTESTATION))
                || StringUtils.hasText((String) collectedParams.get(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION))
                ? EulerClientAttestationProof.ATTESTATION
                : EulerClientAttestationProof.ASSERTION;
    }

    /**
     * Resolve the client authentication method a request authenticates with, from the credential it
     * carries. Section 5 of the draft expects each proof of possession mechanism to be identifiable
     * by its own token endpoint authentication method value, so this is the single place deciding
     * which mechanism a request asked for: a further mechanism is added here and given its collector
     * in {@link #collectAttestationParams}, and every consumer then dispatches on the resolved method
     * without change.
     * <p>
     * The deprecated {@code OAuth-Client-Attestation-Type} header still wins when present, and
     * recognizing released clients is its <b>sole</b> remaining use: they carry the Apple credential
     * in the deprecated {@code attestation} / {@code assertion} / {@code challenge} / {@code kid}
     * form parameters, names too generic to be told apart from an unrelated form parameter, so the
     * carriage cannot identify them.
     *
     * @param request the token endpoint request
     * @return the attestation-based client authentication method this request authenticates with
     */
    private static ClientAuthenticationMethod resolveClientAuthenticationMethod(HttpServletRequest request) {
        String declared = request.getHeader(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_TYPE);
        if (StringUtils.hasText(declared)) {
            return methodForDeclaredType(EulerOAuth2ClientAttestationType.parse(declared));
        }
        if (AppAttestCredentialResolver.resolve(request).isPresent()) {
            return EulerClientAuthenticationMethod.ATTEST_APPATTEST_CLIENT_AUTH;
        }
        return EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH;
    }

    /**
     * The method a released client asked for through the deprecated
     * {@code OAuth-Client-Attestation-Type} header.
     */
    private static ClientAuthenticationMethod methodForDeclaredType(EulerOAuth2ClientAttestationType declaredType) {
        if (EulerOAuth2ClientAttestationType.APPLE_APP_ATTEST.equals(declaredType)) {
            return EulerClientAuthenticationMethod.ATTEST_APPATTEST_CLIENT_AUTH;
        }
        return EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH;
    }

    /**
     * The App Attest carriage, shared with the App Attest business domain and the only
     * non-deprecated one: assertion-only, since App instance registration happens at the dedicated
     * registration endpoint. Values are stored under the canonical {@code App-Attest-*} keys so that
     * downstream components stay transport-agnostic.
     */
    private static void convertAppleAppAttest(AppAttestCredential credential,
                                              Map<String, Object> additionalParams) {
        copyRequired(credential.challenge(), AppAttestParameterNames.HEADER_CHALLENGE, additionalParams);
        copyRequired(credential.kid(), AppAttestParameterNames.HEADER_KID, additionalParams);
        copyRequired(credential.assertion(), AppAttestParameterNames.HEADER_ASSERTION, additionalParams);
    }

    /**
     * Deprecated form-parameter carriage, retained for released clients; see
     * {@link EulerOAuth2ParameterNames#ATTESTATION}. Reached only when the request carries none of
     * the {@code App-Attest-*} / {@code app_attest_*} vocabulary.
     * <p>
     * Unlike the App Attest carriage this one may also carry an attestation, so {@code attestation}
     * and {@code assertion} are not mutually exclusive and the three combinations remain valid:
     * attestation only (registers the App Attest KEY and authenticates the client), assertion only
     * (fast path for an already-registered KEY, which is the sole case requiring {@code kid}),
     * or both. Values are stored under the canonical {@code App-Attest-*} keys so that downstream
     * components stay transport-agnostic.
     */
    @Deprecated
    private static void convertDeprecatedAppleAppAttestFormParameters(HttpServletRequest request,
                                                                     Map<String, Object> additionalParams) {
        copyRequiredFormParameter(request, EulerOAuth2ParameterNames.CHALLENGE,
                AppAttestParameterNames.HEADER_CHALLENGE, additionalParams);

        String attestation = request.getParameter(EulerOAuth2ParameterNames.ATTESTATION);
        if (!StringUtils.hasText(attestation)
                && !StringUtils.hasText(request.getParameter(EulerOAuth2ParameterNames.ASSERTION))) {
            throw newError(OAuth2ErrorCodes.INVALID_REQUEST,
                    EulerOAuth2ParameterNames.ATTESTATION + " or " + EulerOAuth2ParameterNames.ASSERTION, null);
        }
        copyOptional(attestation, AppAttestParameterNames.HEADER_ATTESTATION, additionalParams);
        copyOptionalFormParameter(request, EulerOAuth2ParameterNames.ASSERTION,
                AppAttestParameterNames.HEADER_ASSERTION, additionalParams);

        // The kid is derivable from an attestation (its credentialId), so it is required only
        // for the assertion-only request, whose authenticator data carries no credentialId.
        if (!StringUtils.hasText(attestation)) {
            copyRequiredFormParameter(request, EulerOAuth2ParameterNames.KEY_ID,
                    AppAttestParameterNames.HEADER_KID, additionalParams);
        }
    }

    private static void copyOptional(Object value, String paramName, Map<String, Object> target) {
        if (value != null) {
            target.put(paramName, value);
        }
    }

    private static void copyOptional(HttpServletRequest request, String paramName, Map<String, Object> target) {
        String value = request.getParameter(paramName);
        if (value != null) {
            target.put(paramName, value);
        }
    }

    private static void copyRequired(Object value, String paramName, Map<String, Object> target) {
        if (value == null) {
            throw newError(OAuth2ErrorCodes.INVALID_REQUEST, paramName, null);
        }
        target.put(paramName, value);
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

    private static OAuth2AuthenticationException newError(String errorCode, String parameterName, String errorUri) {
        OAuth2Error error = new OAuth2Error(errorCode, "OAuth 2.0 Parameter: " + parameterName, errorUri);
        return new OAuth2AuthenticationException(error);
    }

    private OAuth2ClientAttestationUtils() {
    }
}
