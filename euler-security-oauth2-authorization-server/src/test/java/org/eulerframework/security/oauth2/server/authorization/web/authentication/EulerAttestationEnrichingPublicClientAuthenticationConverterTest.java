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
import org.eulerframework.security.oauth2.core.EulerOAuth2ClientAttestationType;
import org.eulerframework.security.oauth2.core.endpoint.EulerOAuth2HeaderNames;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.endpoint.PkceParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;

import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests for {@link EulerAttestationEnrichingPublicClientAuthenticationConverter}: it merges the
 * request's attestation into the public-client token so a provider (which has no request access) can
 * read it, while never suppressing the delegate's result.
 */
class EulerAttestationEnrichingPublicClientAuthenticationConverterTest {

    private static final String CLIENT_ID = "client-1";
    private static final String KID_VALUE = "kid-1";
    private static final String CHALLENGE_VALUE = "challenge-1";
    private static final String ASSERTION_VALUE = "assertion-1";

    @Test
    void enrichesAPublicClientTokenWithoutDisturbingItsOwnParameters() {
        Map<String, Object> original = new LinkedHashMap<>();
        original.put(OAuth2ParameterNames.GRANT_TYPE, "authorization_code");
        original.put(PkceParameterNames.CODE_VERIFIER, "verifier-1");
        OAuth2ClientAuthenticationToken publicToken = new OAuth2ClientAuthenticationToken(
                CLIENT_ID, ClientAuthenticationMethod.NONE, null, original);
        EulerAttestationEnrichingPublicClientAuthenticationConverter converter =
                new EulerAttestationEnrichingPublicClientAuthenticationConverter(request -> publicToken);

        OAuth2ClientAuthenticationToken result = (OAuth2ClientAuthenticationToken)
                converter.convert(request(appleHeaders(), Map.of()));

        assertEquals(ClientAuthenticationMethod.NONE, result.getClientAuthenticationMethod());
        assertEquals(CLIENT_ID, result.getPrincipal());
        // The grant parameters PKCE needs survive the rebuild.
        assertEquals("verifier-1", result.getAdditionalParameters().get(PkceParameterNames.CODE_VERIFIER));
        // The attestation is merged in so the provider can verify it from the token.
        assertEquals(CHALLENGE_VALUE,
                result.getAdditionalParameters().get(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_CHALLENGE));
        assertEquals(KID_VALUE,
                result.getAdditionalParameters().get(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_KID));
        // client_id is placed in the parameters for the verifier's Section 6.3 consistency check.
        assertEquals(CLIENT_ID, result.getAdditionalParameters().get(OAuth2ParameterNames.CLIENT_ID));
    }

    @Test
    void passesTheDelegateResultThroughUnchangedWhenThereIsNoAttestationSignal() {
        OAuth2ClientAuthenticationToken publicToken = new OAuth2ClientAuthenticationToken(
                CLIENT_ID, ClientAuthenticationMethod.NONE, null, Map.of());
        EulerAttestationEnrichingPublicClientAuthenticationConverter converter =
                new EulerAttestationEnrichingPublicClientAuthenticationConverter(request -> publicToken);

        assertSame(publicToken, converter.convert(request(Map.of(), Map.of())));
    }

    @Test
    void passesThroughNullWhenTheDelegateDeclines() {
        EulerAttestationEnrichingPublicClientAuthenticationConverter converter =
                new EulerAttestationEnrichingPublicClientAuthenticationConverter(request -> null);

        assertNull(converter.convert(request(appleHeaders(), Map.of())));
    }

    // ---- helpers ----

    private static Map<String, String> appleHeaders() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_TYPE,
                EulerOAuth2ClientAttestationType.APPLE_APP_ATTEST.value());
        headers.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_CHALLENGE, CHALLENGE_VALUE);
        headers.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_KID, KID_VALUE);
        headers.put(EulerOAuth2HeaderNames.OAUTH_CLIENT_ATTESTATION_ASSERTION, ASSERTION_VALUE);
        return headers;
    }

    private static HttpServletRequest request(Map<String, String> headers, Map<String, String> parameters) {
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getHeader" -> headers.get((String) args[0]);
                    case "getParameter" -> parameters.get((String) args[0]);
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
        return null;
    }
}
