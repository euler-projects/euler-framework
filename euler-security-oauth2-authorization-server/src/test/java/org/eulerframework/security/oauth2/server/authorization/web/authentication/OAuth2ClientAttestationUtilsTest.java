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
import org.eulerframework.security.oauth2.core.EulerClientAuthenticationMethod;
import org.eulerframework.security.oauth2.core.EulerClientAttestationProof;
import org.eulerframework.security.oauth2.core.EulerOAuth2ClientAttestationType;
import org.eulerframework.security.oauth2.core.endpoint.EulerOAuth2HeaderNames;
import org.eulerframework.security.oauth2.core.endpoint.EulerOAuth2ParameterNames;
import org.eulerframework.security.web.authentication.appattest.AppAttestCredentialResolver;
import org.eulerframework.security.web.authentication.appattest.AppAttestParameterNames;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link OAuth2ClientAttestationUtils}: recognizing a client attestation on a request,
 * deciding which client authentication mechanism it asks for, normalizing either Apple App Attest
 * carriage onto the canonical keys, and reading the proof back out of the result.
 */
class OAuth2ClientAttestationUtilsTest {

    private static final String CLIENT_ID = "client-1";
    private static final String KID_VALUE = "kid-1";
    private static final String CHALLENGE_VALUE = "challenge-1";
    private static final String ASSERTION_VALUE = "assertion-1";
    private static final String ATTESTATION_VALUE = "attestation-1";

    // ---- header carriage ----

    @Test
    void headerCarriageCollectsAllThreeValues() {
        Map<String, Object> params = collect(request(appleHeaders(), Map.of()));

        assertEquals(CHALLENGE_VALUE, params.get(AppAttestParameterNames.HEADER_CHALLENGE));
        assertEquals(KID_VALUE, params.get(AppAttestParameterNames.HEADER_KID));
        assertEquals(ASSERTION_VALUE, params.get(AppAttestParameterNames.HEADER_ASSERTION));
        assertNull(params.get(AppAttestParameterNames.HEADER_ATTESTATION),
                "the App Attest carriage at the OAuth2 endpoints is assertion-only");
    }

    @Test
    void headerCarriageIgnoresFormParametersEntirely() {
        // A stray attestation form parameter must not turn this into an App instance registration.
        Map<String, String> strayAttestation = Map.of(EulerOAuth2ParameterNames.ATTESTATION, ATTESTATION_VALUE);
        Map<String, Object> params = collect(request(appleHeaders(), strayAttestation));

        assertNull(params.get(AppAttestParameterNames.HEADER_ATTESTATION), "the two carriages are never mixed");
        assertEquals(EulerClientAttestationProof.ASSERTION, OAuth2ClientAttestationUtils.resolveProof(params));
    }

    @Test
    void headerCarriageRequiresKid() {
        Map<String, String> headers = appleHeaders();
        headers.remove(AppAttestParameterNames.HEADER_KID);
        HttpServletRequest request = request(headers, Map.of());

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> collect(request));

        assertEquals(OAuth2ErrorCodes.INVALID_REQUEST, ex.getError().getErrorCode());
        assertTrue(ex.getError().getDescription().contains(AppAttestParameterNames.HEADER_KID));
    }

    @Test
    void headerCarriageRequiresChallenge() {
        Map<String, String> headers = appleHeaders();
        headers.remove(AppAttestParameterNames.HEADER_CHALLENGE);
        HttpServletRequest request = request(headers, Map.of());

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> collect(request));

        assertEquals(OAuth2ErrorCodes.INVALID_REQUEST, ex.getError().getErrorCode());
        assertTrue(ex.getError().getDescription().contains(AppAttestParameterNames.HEADER_CHALLENGE));
    }

    // ---- deprecated form parameter carriage ----

    @Test
    void formCarriageIsNormalizedOntoTheHeaderKeys() {
        Map<String, String> form = new LinkedHashMap<>();
        form.put(EulerOAuth2ParameterNames.CHALLENGE, CHALLENGE_VALUE);
        form.put(EulerOAuth2ParameterNames.KEY_ID, KID_VALUE);
        form.put(EulerOAuth2ParameterNames.ASSERTION, ASSERTION_VALUE);

        Map<String, Object> params = collect(request(deprecatedAppleTypeHeaderOnly(), form));

        assertEquals(CHALLENGE_VALUE, params.get(AppAttestParameterNames.HEADER_CHALLENGE));
        assertEquals(KID_VALUE, params.get(AppAttestParameterNames.HEADER_KID));
        assertEquals(ASSERTION_VALUE, params.get(AppAttestParameterNames.HEADER_ASSERTION));
        assertNull(params.get(EulerOAuth2ParameterNames.CHALLENGE),
                "deprecated keys are not propagated downstream");
    }

    @Test
    void formCarriageAcceptsAttestationWithoutKid() {
        Map<String, String> form = new LinkedHashMap<>();
        form.put(EulerOAuth2ParameterNames.CHALLENGE, CHALLENGE_VALUE);
        form.put(EulerOAuth2ParameterNames.ATTESTATION, ATTESTATION_VALUE);

        Map<String, Object> params = collect(request(deprecatedAppleTypeHeaderOnly(), form));

        assertEquals(ATTESTATION_VALUE, params.get(AppAttestParameterNames.HEADER_ATTESTATION));
        assertNull(params.get(AppAttestParameterNames.HEADER_KID),
                "the key id is derivable from an attestation, so it is not part of that contract");
    }

    @Test
    void formCarriageRequiresKidForAssertionOnly() {
        Map<String, String> form = new LinkedHashMap<>();
        form.put(EulerOAuth2ParameterNames.CHALLENGE, CHALLENGE_VALUE);
        form.put(EulerOAuth2ParameterNames.ASSERTION, ASSERTION_VALUE);
        HttpServletRequest request = request(deprecatedAppleTypeHeaderOnly(), form);

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> collect(request));

        assertEquals(OAuth2ErrorCodes.INVALID_REQUEST, ex.getError().getErrorCode());
        assertTrue(ex.getError().getDescription().contains(EulerOAuth2ParameterNames.KEY_ID));
    }

    @Test
    void formCarriageRejectsWhenNeitherAttestationNorAssertionIsPresent() {
        Map<String, String> form = Map.of(EulerOAuth2ParameterNames.CHALLENGE, CHALLENGE_VALUE);
        HttpServletRequest request = request(deprecatedAppleTypeHeaderOnly(), form);

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

    // ---- mechanism resolution ----

    @Test
    void resolvesTheClientAuthenticationMethodFromTheCarriage() {
        assertEquals(EulerClientAuthenticationMethod.ATTEST_APPATTEST_CLIENT_AUTH,
                resolveMethod(request(appleHeaders(), Map.of())),
                "the App Attest carriage selects its own method value, with no type header involved");

        assertEquals(EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH,
                resolveMethod(request(
                        Map.of(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_POP, "pop-jwt"), Map.of())),
                "anything else is the draft's standard variant");

        Map<String, String> form = new LinkedHashMap<>();
        form.put(EulerOAuth2ParameterNames.CHALLENGE, CHALLENGE_VALUE);
        form.put(EulerOAuth2ParameterNames.KEY_ID, KID_VALUE);
        form.put(EulerOAuth2ParameterNames.ASSERTION, ASSERTION_VALUE);
        assertEquals(EulerClientAuthenticationMethod.ATTEST_APPATTEST_CLIENT_AUTH,
                resolveMethod(request(deprecatedAppleTypeHeaderOnly(), form)),
                "the deprecated type header still identifies a released client's carriage");

        assertNull(resolveMethod(request(Map.of(), Map.of())), "no signal, so nothing collected");
    }

    // ---- proof resolution ----

    @Test
    void resolveReportsAttestationOnlyForTheFormCarriage() {
        Map<String, String> attestationForm = new LinkedHashMap<>();
        attestationForm.put(EulerOAuth2ParameterNames.CHALLENGE, CHALLENGE_VALUE);
        attestationForm.put(EulerOAuth2ParameterNames.ATTESTATION, ATTESTATION_VALUE);
        assertEquals(EulerClientAttestationProof.ATTESTATION, OAuth2ClientAttestationUtils.resolveProof(
                collect(request(deprecatedAppleTypeHeaderOnly(), attestationForm))));

        Map<String, String> assertionForm = new LinkedHashMap<>();
        assertionForm.put(EulerOAuth2ParameterNames.CHALLENGE, CHALLENGE_VALUE);
        assertionForm.put(EulerOAuth2ParameterNames.KEY_ID, KID_VALUE);
        assertionForm.put(EulerOAuth2ParameterNames.ASSERTION, ASSERTION_VALUE);
        assertEquals(EulerClientAttestationProof.ASSERTION, OAuth2ClientAttestationUtils.resolveProof(
                collect(request(deprecatedAppleTypeHeaderOnly(), assertionForm))));

        assertEquals(EulerClientAttestationProof.ASSERTION, OAuth2ClientAttestationUtils.resolveProof(
                        collect(request(appleHeaders(), Map.of()))),
                "the header carriage can never register a device");
    }

    @Test
    void resolveReportsAttestationForTheJwtVariantByHeader() {
        Map<String, String> popOnly = new LinkedHashMap<>();
        popOnly.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_POP, "pop-jwt");
        assertEquals(EulerClientAttestationProof.ASSERTION,
                OAuth2ClientAttestationUtils.resolveProof(collect(request(popOnly, Map.of()))));

        Map<String, String> both = new LinkedHashMap<>(popOnly);
        both.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION, "attestation-jwt");
        assertEquals(EulerClientAttestationProof.ATTESTATION,
                OAuth2ClientAttestationUtils.resolveProof(collect(request(both, Map.of()))));
    }

    // ---- signal detection ----

    @Test
    void collectLeavesTheMapEmptyWithoutAnyAttestationSignal() {
        assertTrue(collect(request(Map.of(), Map.of())).isEmpty());
    }

    @Test
    void headerCarriageIsSelectedByTheAssertionHeaderAlone() {
        Map<String, String> headers = new LinkedHashMap<>();
        assertFalse(AppAttestCredentialResolver.isHeaderCarried(request(headers, Map.of())));

        headers.put(AppAttestParameterNames.HEADER_ASSERTION, ASSERTION_VALUE);
        assertTrue(AppAttestCredentialResolver.isHeaderCarried(request(headers, Map.of())));
    }

    @Test
    void carriesAttestationSignalDetectsAnyOfTheAttestationHeaders() {
        assertFalse(OAuth2ClientAttestationUtils.carriesAttestationSignal(request(Map.of(), Map.of())));
        assertTrue(OAuth2ClientAttestationUtils.carriesAttestationSignal(
                request(Map.of(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_TYPE,
                        EulerOAuth2ClientAttestationType.APPLE_APP_ATTEST.value()), Map.of())));
        assertTrue(OAuth2ClientAttestationUtils.carriesAttestationSignal(
                request(Map.of(AppAttestParameterNames.HEADER_ASSERTION, "assertion-1"), Map.of())),
                "the App Attest carriage is a signal on its own, without the deprecated type header");
    }

    // ---- helpers ----

    private static Map<String, Object> collect(HttpServletRequest request) {
        Map<String, Object> params = new HashMap<>();
        OAuth2ClientAttestationUtils.collectAttestationParams(request, params);
        return params;
    }

    private static ClientAuthenticationMethod resolveMethod(HttpServletRequest request) {
        return OAuth2ClientAttestationUtils.collectAttestationParams(request, new HashMap<>());
    }

    private static Map<String, String> appleHeaders() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(AppAttestParameterNames.HEADER_CHALLENGE, CHALLENGE_VALUE);
        headers.put(AppAttestParameterNames.HEADER_KID, KID_VALUE);
        headers.put(AppAttestParameterNames.HEADER_ASSERTION, ASSERTION_VALUE);
        return headers;
    }

    /**
     * The deprecated type header on its own. It is what makes a request carrying the deprecated
     * form-parameter vocabulary recognizable as Apple App Attest at all, so only those fixtures need
     * it; a request using the {@code App-Attest-*} carriage is recognized without it.
     */
    private static Map<String, String> deprecatedAppleTypeHeaderOnly() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_TYPE,
                EulerOAuth2ClientAttestationType.APPLE_APP_ATTEST.value());
        return headers;
    }

    private static HttpServletRequest request(Map<String, String> headers, Map<String, String> parameters) {
        return TestHttpServletRequests.post(headers, parameters);
    }
}
