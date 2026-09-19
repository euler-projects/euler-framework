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

import jakarta.servlet.http.HttpServletRequest;
import org.eulerframework.security.authentication.appattest.AppAttestAttestationRegistration;
import org.eulerframework.security.oauth2.core.EulerClientAuthenticationMethod;
import org.eulerframework.security.oauth2.core.EulerClientAttestationProof;
import org.eulerframework.security.oauth2.core.EulerOAuth2ClientAttestationType;
import org.eulerframework.security.oauth2.core.endpoint.EulerOAuth2HeaderNames;
import org.eulerframework.security.oauth2.core.endpoint.EulerOAuth2ParameterNames;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.endpoint.PkceParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link EulerOAuth2ClientAttestationAuthenticationConverter}:
 * {@link EulerOAuth2ClientAttestationAuthenticationConverter#collectAttestationParams} (raw
 * collection for the two Apple App Attest carriages, shared with the enriching wrapper and the
 * success handler) and {@link EulerOAuth2ClientAttestationAuthenticationConverter#convert}
 * (claiming an attestation-only request for the basic path, collecting its attestation and grant
 * parameters without verifying).
 */
class EulerOAuth2ClientAttestationAuthenticationConverterTest {

    private static final String CLIENT_ID = "client-1";
    private static final String KID_VALUE = "kid-1";
    private static final String CHALLENGE_VALUE = "challenge-1";
    private static final String ASSERTION_VALUE = "assertion-1";
    private static final String ATTESTATION_VALUE = "attestation-1";

    private final EulerOAuth2ClientAttestationAuthenticationConverter converter =
            new EulerOAuth2ClientAttestationAuthenticationConverter();

    // ---- header carriage (collectAttestationParams) ----

    @Test
    void headerCarriageCollectsAllThreeValues() {
        Map<String, Object> params = collect(request(appleHeaders(), Map.of()));

        assertEquals(CHALLENGE_VALUE, params.get(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_CHALLENGE));
        assertEquals(KID_VALUE, params.get(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_KID));
        assertEquals(ASSERTION_VALUE, params.get(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_ASSERTION));
        assertNull(params.get(EulerOAuth2ParameterNames.ATTESTATION),
                "the header carriage has no attestation analog");
    }

    @Test
    void headerCarriageIgnoresFormParametersEntirely() {
        // A stray attestation form parameter must not turn this into an App instance registration.
        Map<String, String> strayAttestation = Map.of(EulerOAuth2ParameterNames.ATTESTATION, ATTESTATION_VALUE);
        Map<String, Object> params =
                collect(request(appleHeaders(), strayAttestation));

        assertNull(params.get(EulerOAuth2ParameterNames.ATTESTATION), "the two carriages are never mixed");
        assertEquals(EulerClientAttestationProof.ASSERTION,
                EulerOAuth2ClientAttestationAuthenticationConverter.resolveProof(params));
    }

    @Test
    void headerCarriageRequiresKid() {
        Map<String, String> headers = appleHeaders();
        headers.remove(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_KID);
        HttpServletRequest request = request(headers, Map.of());

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> collect(request));

        assertEquals(OAuth2ErrorCodes.INVALID_REQUEST, ex.getError().getErrorCode());
        assertTrue(ex.getError().getDescription()
                .contains(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_KID));
    }

    @Test
    void headerCarriageRequiresChallenge() {
        Map<String, String> headers = appleHeaders();
        headers.remove(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_CHALLENGE);
        HttpServletRequest request = request(headers, Map.of());

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> collect(request));

        assertEquals(OAuth2ErrorCodes.INVALID_REQUEST, ex.getError().getErrorCode());
        assertTrue(ex.getError().getDescription()
                .contains(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_CHALLENGE));
    }

    // ---- deprecated form parameter carriage ----

    @Test
    void formCarriageIsNormalizedOntoTheHeaderKeys() {
        Map<String, String> form = new LinkedHashMap<>();
        form.put(EulerOAuth2ParameterNames.CHALLENGE, CHALLENGE_VALUE);
        form.put(EulerOAuth2ParameterNames.KEY_ID, KID_VALUE);
        form.put(EulerOAuth2ParameterNames.ASSERTION, ASSERTION_VALUE);

        Map<String, Object> params =
                collect(request(appleTypeHeaderOnly(), form));

        assertEquals(CHALLENGE_VALUE, params.get(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_CHALLENGE));
        assertEquals(KID_VALUE, params.get(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_KID));
        assertEquals(ASSERTION_VALUE, params.get(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_ASSERTION));
        assertNull(params.get(EulerOAuth2ParameterNames.CHALLENGE),
                "deprecated keys are not propagated downstream");
    }

    @Test
    void formCarriageAcceptsAttestationWithoutKid() {
        Map<String, String> form = new LinkedHashMap<>();
        form.put(EulerOAuth2ParameterNames.CHALLENGE, CHALLENGE_VALUE);
        form.put(EulerOAuth2ParameterNames.ATTESTATION, ATTESTATION_VALUE);

        Map<String, Object> params =
                collect(request(appleTypeHeaderOnly(), form));

        assertEquals(ATTESTATION_VALUE, params.get(EulerOAuth2ParameterNames.ATTESTATION));
        assertNull(params.get(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_KID),
                "the key id is derivable from an attestation, so it is not part of that contract");
    }

    @Test
    void formCarriageRequiresKidForAssertionOnly() {
        Map<String, String> form = new LinkedHashMap<>();
        form.put(EulerOAuth2ParameterNames.CHALLENGE, CHALLENGE_VALUE);
        form.put(EulerOAuth2ParameterNames.ASSERTION, ASSERTION_VALUE);
        HttpServletRequest request = request(appleTypeHeaderOnly(), form);

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> collect(request));

        assertEquals(OAuth2ErrorCodes.INVALID_REQUEST, ex.getError().getErrorCode());
        assertTrue(ex.getError().getDescription().contains(EulerOAuth2ParameterNames.KEY_ID));
    }

    @Test
    void formCarriageRejectsWhenNeitherAttestationNorAssertionIsPresent() {
        Map<String, String> form = Map.of(EulerOAuth2ParameterNames.CHALLENGE, CHALLENGE_VALUE);
        HttpServletRequest request = request(appleTypeHeaderOnly(), form);

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> collect(request));

        assertEquals(OAuth2ErrorCodes.INVALID_REQUEST, ex.getError().getErrorCode());
    }

    @Test
    void optionalClientIdIsCollected() {
        Map<String, String> form = Map.of(OAuth2ParameterNames.CLIENT_ID, CLIENT_ID);

        Map<String, Object> params = collect(request(appleHeaders(), form));

        assertEquals(CLIENT_ID, params.get(OAuth2ParameterNames.CLIENT_ID));
    }

    // ---- proof resolution ----

    @Test
    void resolveReportsAttestationOnlyForTheFormCarriage() {
        Map<String, String> attestationForm = new LinkedHashMap<>();
        attestationForm.put(EulerOAuth2ParameterNames.CHALLENGE, CHALLENGE_VALUE);
        attestationForm.put(EulerOAuth2ParameterNames.ATTESTATION, ATTESTATION_VALUE);
        assertEquals(EulerClientAttestationProof.ATTESTATION,
                EulerOAuth2ClientAttestationAuthenticationConverter.resolveProof(
                        collect(request(appleTypeHeaderOnly(), attestationForm))));

        Map<String, String> assertionForm = new LinkedHashMap<>();
        assertionForm.put(EulerOAuth2ParameterNames.CHALLENGE, CHALLENGE_VALUE);
        assertionForm.put(EulerOAuth2ParameterNames.KEY_ID, KID_VALUE);
        assertionForm.put(EulerOAuth2ParameterNames.ASSERTION, ASSERTION_VALUE);
        assertEquals(EulerClientAttestationProof.ASSERTION,
                EulerOAuth2ClientAttestationAuthenticationConverter.resolveProof(
                        collect(request(appleTypeHeaderOnly(), assertionForm))));

        assertEquals(EulerClientAttestationProof.ASSERTION,
                EulerOAuth2ClientAttestationAuthenticationConverter.resolveProof(
                        collect(request(appleHeaders(), Map.of()))),
                "the header carriage can never register a device");
    }

    @Test
    void resolveReportsAttestationForTheJwtVariantByHeader() {
        Map<String, String> popOnly = new LinkedHashMap<>();
        popOnly.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_POP, "pop-jwt");
        assertEquals(EulerClientAttestationProof.ASSERTION,
                EulerOAuth2ClientAttestationAuthenticationConverter.resolveProof(
                        collect(request(popOnly, Map.of()))));

        Map<String, String> both = new LinkedHashMap<>(popOnly);
        both.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION, "attestation-jwt");
        assertEquals(EulerClientAttestationProof.ATTESTATION,
                EulerOAuth2ClientAttestationAuthenticationConverter.resolveProof(
                        collect(request(both, Map.of()))));
    }

    // ---- signal detection ----

    @Test
    void collectLeavesTheMapEmptyWithoutAnyAttestationSignal() {
        assertTrue(collect(request(Map.of(), Map.of())).isEmpty());
    }

    @Test
    void headerCarriageIsSelectedByTheAssertionHeaderAlone() {
        Map<String, String> headers = appleTypeHeaderOnly();
        assertFalse(EulerOAuth2ClientAttestationAuthenticationConverter.isHeaderCarried(request(headers, Map.of())));

        headers.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_ASSERTION, ASSERTION_VALUE);
        assertTrue(EulerOAuth2ClientAttestationAuthenticationConverter.isHeaderCarried(request(headers, Map.of())));
    }

    @Test
    void carriesAttestationSignalDetectsAnyOfTheAttestationHeaders() {
        assertFalse(EulerOAuth2ClientAttestationAuthenticationConverter.carriesAttestationSignal(
                request(Map.of(), Map.of())));
        assertTrue(EulerOAuth2ClientAttestationAuthenticationConverter.carriesAttestationSignal(
                request(Map.of(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_TYPE,
                        EulerOAuth2ClientAttestationType.APPLE_APP_ATTEST.value()), Map.of())));
    }

    // ---- convert: claim an attestation-only request for the basic path ----

    @Test
    void convertClaimsAnAttestationRequestAndCollectsItsParameters() {
        HttpServletRequest request = request(appleHeaders(), Map.of(OAuth2ParameterNames.CLIENT_ID, CLIENT_ID));

        Authentication result = this.converter.convert(request);

        assertInstanceOf(OAuth2ClientAuthenticationToken.class, result);
        OAuth2ClientAuthenticationToken token = (OAuth2ClientAuthenticationToken) result;
        assertEquals(EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH, token.getClientAuthenticationMethod());
        // client_id was present, so it becomes the principal; verification still resolves it and
        // the Section 6.3 check compares the two.
        assertEquals(CLIENT_ID, token.getPrincipal());
        // The provider verifies from the token, so the attestation must travel in it.
        assertEquals(CHALLENGE_VALUE, token.getAdditionalParameters()
                .get(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_CHALLENGE));
        assertNull(token.getCredentials(), "the converter collects but does not verify");
    }

    @Test
    void convertUsesAPlaceholderPrincipalWhenTheRequestCarriesNoClientId() {
        Authentication result = this.converter.convert(request(appleHeaders(), Map.of()));

        OAuth2ClientAuthenticationToken token = (OAuth2ClientAuthenticationToken) result;
        assertEquals(EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH, token.getClientAuthenticationMethod());
        assertNotNull(token.getPrincipal(),
                "a printable placeholder is still needed to pass the client identifier syntax check");
    }

    @Test
    void convertCarriesGrantParametersForPkceEnforcement() {
        Map<String, String> form = new LinkedHashMap<>();
        form.put(OAuth2ParameterNames.GRANT_TYPE, "authorization_code");
        form.put(OAuth2ParameterNames.CODE, "code-1");
        form.put(PkceParameterNames.CODE_VERIFIER, "verifier-1");

        OAuth2ClientAuthenticationToken token =
                (OAuth2ClientAuthenticationToken) this.converter.convert(request(appleHeaders(), form));

        assertEquals("code-1", token.getAdditionalParameters().get(OAuth2ParameterNames.CODE));
        assertEquals("verifier-1", token.getAdditionalParameters().get(PkceParameterNames.CODE_VERIFIER));
    }

    @Test
    void convertReturnsNullWithoutAnyAttestationSignal() {
        assertNull(this.converter.convert(request(Map.of(), Map.of())));
    }

    // ---- helpers ----

    private static Map<String, Object> collect(HttpServletRequest request) {
        Map<String, Object> params = new HashMap<>();
        EulerOAuth2ClientAttestationAuthenticationConverter.collectAttestationParams(request, params);
        return params;
    }

    private static Map<String, String> appleHeaders() {
        Map<String, String> headers = appleTypeHeaderOnly();
        headers.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_CHALLENGE, CHALLENGE_VALUE);
        headers.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_KID, KID_VALUE);
        headers.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_ASSERTION, ASSERTION_VALUE);
        return headers;
    }

    private static Map<String, String> appleTypeHeaderOnly() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_TYPE,
                EulerOAuth2ClientAttestationType.APPLE_APP_ATTEST.value());
        return headers;
    }

    private static AppAttestAttestationRegistration registration(String keyId, String clientId) {
        return new AppAttestAttestationRegistration(keyId, "ABCD1234EF", "com.example.app", clientId,
                null, null, null, null, null, null, 0);
    }

    /**
     * Minimal {@link HttpServletRequest} stub backed by maps. The converter only reads headers,
     * parameters and attributes, so a dynamic proxy avoids pulling in a servlet test dependency.
     */
    private static HttpServletRequest request(Map<String, String> headers, Map<String, String> parameters) {
        return request(headers, parameters, Map.of());
    }

    private static HttpServletRequest request(Map<String, String> headers, Map<String, String> parameters,
                                              Map<String, Object> attributes) {
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getHeader" -> headers.get((String) args[0]);
                    case "getParameter" -> parameters.get((String) args[0]);
                    case "getAttribute" -> attributes.get((String) args[0]);
                    case "toString" -> "HttpServletRequestStub";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> primitiveDefault(method.getReturnType());
                });
    }

    private static Object primitiveDefault(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            return null;
        }
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == int.class) {
            return 0;
        }
        if (returnType == long.class) {
            return 0L;
        }
        if (returnType == short.class) {
            return (short) 0;
        }
        if (returnType == byte.class) {
            return (byte) 0;
        }
        if (returnType == char.class) {
            return (char) 0;
        }
        if (returnType == float.class) {
            return 0F;
        }
        if (returnType == double.class) {
            return 0D;
        }
        return null;
    }
}
