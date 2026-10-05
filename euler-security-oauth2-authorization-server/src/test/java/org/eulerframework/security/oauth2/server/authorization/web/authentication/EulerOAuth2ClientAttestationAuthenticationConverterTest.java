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
import org.eulerframework.security.web.authentication.appattest.AppAttestParameterNames;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.endpoint.PkceParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests for {@link EulerOAuth2ClientAttestationAuthenticationConverter#convert}: claiming an
 * attestation-only request for the basic path and building its token without verifying.
 * <p>
 * Reading the attestation off the request &mdash; the two Apple App Attest carriages, which
 * mechanism they resolve to, and the proof they present &mdash; is
 * {@link OAuth2ClientAttestationUtils}' job and is covered by {@link OAuth2ClientAttestationUtilsTest}.
 */
class EulerOAuth2ClientAttestationAuthenticationConverterTest {

    private static final String CLIENT_ID = "client-1";
    private static final String KID_VALUE = "kid-1";
    private static final String CHALLENGE_VALUE = "challenge-1";
    private static final String ASSERTION_VALUE = "assertion-1";

    private final EulerOAuth2ClientAttestationAuthenticationConverter converter =
            new EulerOAuth2ClientAttestationAuthenticationConverter();

    @Test
    void convertClaimsAnAttestationRequestAndCollectsItsParameters() {
        HttpServletRequest request = request(appleHeaders(), Map.of(OAuth2ParameterNames.CLIENT_ID, CLIENT_ID));

        Authentication result = this.converter.convert(request);

        assertInstanceOf(OAuth2ClientAuthenticationToken.class, result);
        OAuth2ClientAuthenticationToken token = (OAuth2ClientAuthenticationToken) result;
        assertEquals(EulerClientAuthenticationMethod.ATTEST_APPATTEST_CLIENT_AUTH,
                token.getClientAuthenticationMethod());
        // client_id was present, so it becomes the principal; verification still resolves it and
        // the Section 6.3 check compares the two.
        assertEquals(CLIENT_ID, token.getPrincipal());
        // The provider verifies from the token, so the attestation must travel in it.
        assertEquals(CHALLENGE_VALUE, token.getAdditionalParameters()
                .get(AppAttestParameterNames.HEADER_CHALLENGE));
        assertNull(token.getCredentials(), "the converter collects but does not verify");
    }

    @Test
    void convertUsesAPlaceholderPrincipalWhenTheRequestCarriesNoClientId() {
        Authentication result = this.converter.convert(request(appleHeaders(), Map.of()));

        OAuth2ClientAuthenticationToken token = (OAuth2ClientAuthenticationToken) result;
        assertEquals(EulerClientAuthenticationMethod.ATTEST_APPATTEST_CLIENT_AUTH,
                token.getClientAuthenticationMethod());
        assertNotNull(token.getPrincipal(),
                "a printable placeholder is still needed to pass the client identifier syntax check");
    }

    /**
     * Spring's {@code CodeVerifierAuthenticator} reads {@code grant_type}, {@code code} and
     * {@code code_verifier} from the token, and the authorization code grant provider performs no
     * PKCE check of its own, so a client authenticated by its attestation gets PKCE enforced only if
     * these travel in the token.
     */
    @Test
    void convertCarriesGrantParametersForPkceEnforcement() {
        Map<String, String> form = new LinkedHashMap<>();
        form.put(OAuth2ParameterNames.GRANT_TYPE, "authorization_code");
        form.put(OAuth2ParameterNames.CODE, "code-1");
        form.put(PkceParameterNames.CODE_VERIFIER, "verifier-1");
        form.put(OAuth2ParameterNames.CLIENT_SECRET, "consumed-secret");
        form.put(OAuth2ParameterNames.CLIENT_ASSERTION_TYPE, "consumed-assertion-type");
        form.put(OAuth2ParameterNames.CLIENT_ASSERTION, "consumed-client-assertion");
        form.put(AppAttestParameterNames.PARAM_KID, "kid-from-form");

        OAuth2ClientAuthenticationToken token =
                (OAuth2ClientAuthenticationToken) this.converter.convert(request(appleHeaders(), form));

        assertEquals("authorization_code", token.getAdditionalParameters().get(OAuth2ParameterNames.GRANT_TYPE));
        assertEquals("code-1", token.getAdditionalParameters().get(OAuth2ParameterNames.CODE));
        assertEquals("verifier-1", token.getAdditionalParameters().get(PkceParameterNames.CODE_VERIFIER));
        assertNull(token.getAdditionalParameters().get(OAuth2ParameterNames.CLIENT_SECRET));
        assertNull(token.getAdditionalParameters().get(OAuth2ParameterNames.CLIENT_ASSERTION_TYPE));
        assertNull(token.getAdditionalParameters().get(OAuth2ParameterNames.CLIENT_ASSERTION));
        assertNull(token.getAdditionalParameters().get(AppAttestParameterNames.PARAM_KID),
                "the attestation collection already consumed it under its canonical key, so the raw "
                        + "form name must not ride along as a second, ambiguous copy");
    }

    @Test
    void convertReturnsNullWithoutAnyAttestationSignal() {
        assertNull(this.converter.convert(request(Map.of(), Map.of())));
    }

    // ---- helpers ----

    private static Map<String, String> appleHeaders() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(AppAttestParameterNames.HEADER_CHALLENGE, CHALLENGE_VALUE);
        headers.put(AppAttestParameterNames.HEADER_KID, KID_VALUE);
        headers.put(AppAttestParameterNames.HEADER_ASSERTION, ASSERTION_VALUE);
        return headers;
    }

    private static HttpServletRequest request(Map<String, String> headers, Map<String, String> parameters) {
        return TestHttpServletRequests.post(headers, parameters);
    }
}
