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
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.eulerframework.security.oauth2.core.EulerClientAuthenticationMethod;
import org.eulerframework.security.oauth2.core.endpoint.EulerOAuth2ParameterNames;
import org.eulerframework.security.oauth2.server.authorization.authentication.EulerOAuth2ClientAttestationAuthenticationProvider;
import org.eulerframework.security.web.authentication.appattest.AppAttestParameterNames;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2EndpointUtilsAccessor;
import org.springframework.security.web.authentication.AuthenticationConverter;

/**
 * The {@link AuthenticationConverter} for the attestation-based client authentication methods
 * ({@code attest_jwt_client_auth} and {@code attest_appattest_client_auth}), registered at the
 * <i>end</i> of the {@code OAuth2ClientAuthenticationFilter} converter chain, so it claims only a
 * request no traditional converter claimed &mdash; one whose attestation is therefore its sole
 * credential rather than an overlay on one.
 * <p>
 * {@link #convert} collects the attestation and the grant parameters into a token; it never verifies.
 * Reading the attestation off the request belongs to {@link OAuth2ClientAttestationUtils}, which the
 * two other components needing it share. Verification, which consumes the one-time challenge and
 * resolves the {@code client_id}, belongs to
 * {@link EulerOAuth2ClientAttestationAuthenticationProvider}.
 *
 * @see EulerClientAuthenticationMethod#ATTEST_JWT_CLIENT_AUTH
 * @see EulerClientAuthenticationMethod#ATTEST_APPATTEST_CLIENT_AUTH
 */
public final class EulerOAuth2ClientAttestationAuthenticationConverter implements AuthenticationConverter {

    /**
     * Placeholder principal for a token this converter produces. An attestation-based client is
     * identified by its attestation, not by a {@code client_id} form parameter, so the
     * real {@code client_id} is resolved during verification by the provider. The placeholder only
     * has to be a printable string to pass {@code OAuth2ClientAuthenticationFilter}'s client
     * identifier syntax check.
     */
    private static final String ATTESTATION_PRINCIPAL_PLACEHOLDER = "(attestation)";

    /**
     * Parameters left out of the grant-parameter collection because the attestation collection
     * already consumed them, mirroring how Spring's own client authentication converters exclude the
     * credential they read.
     * <p>
     * {@code client_id} is among them: it travels as the token's principal and is copied explicitly
     * for the RFC 6749 Section 6.3 consistency check. The deprecated four are excluded for the same
     * reason as the {@code app_attest_*} ones &mdash; their values are already in the map under the
     * canonical {@code App-Attest-*} keys &mdash; and because their names are dangerously generic:
     * {@code assertion} is also RFC 7523's parameter, and {@code kid} is a bare abbreviation. Leaving
     * them in would put a credential under a second, ambiguous name where downstream code could pick
     * it up by accident.
     */
    private static final String[] CONSUMED_PARAMETER_EXCLUSIONS = {
            OAuth2ParameterNames.CLIENT_ID,
            AppAttestParameterNames.PARAM_ATTESTATION,
            AppAttestParameterNames.PARAM_KID,
            AppAttestParameterNames.PARAM_CHALLENGE,
            AppAttestParameterNames.PARAM_ASSERTION,
            EulerOAuth2ParameterNames.ATTESTATION,
            EulerOAuth2ParameterNames.KEY_ID,
            EulerOAuth2ParameterNames.CHALLENGE,
            EulerOAuth2ParameterNames.ASSERTION
    };

    @Override
    public Authentication convert(HttpServletRequest request) {
        // Runs last, so a request presenting a traditional credential (including a PKCE-shaped one)
        // was claimed earlier and never reaches here.
        Map<String, Object> additionalParameters = new HashMap<>();
        ClientAuthenticationMethod method =
                OAuth2ClientAttestationUtils.collectAttestationParams(request, additionalParameters);
        if (method == null) {
            return null;
        }

        collectGrantParams(request, additionalParameters);
        String principal = additionalParameters.containsKey(OAuth2ParameterNames.CLIENT_ID)
                ? (String) additionalParameters.get(OAuth2ParameterNames.CLIENT_ID)
                : ATTESTATION_PRINCIPAL_PLACEHOLDER;
        return new OAuth2ClientAuthenticationToken(principal, method, null, additionalParameters);
    }

    /**
     * Collect the grant parameters PKCE enforcement reads from the token. Spring's
     * {@code CodeVerifierAuthenticator} takes {@code grant_type}, {@code code} and
     * {@code code_verifier} from {@code OAuth2ClientAuthenticationToken#getAdditionalParameters()},
     * and the authorization code grant provider performs no PKCE check of its own, so for an
     * attestation-authenticated client this is the only place they can come from.
     * <p>
     * It delegates to the same helper Spring's own client authentication converters use rather than
     * naming those three by hand, which would silently drop every other parameter: the helper returns
     * all remaining parameters for an {@code authorization_code} grant request and nothing otherwise,
     * which is exactly when PKCE applies.
     *
     * @see EulerOAuth2ClientAttestationAuthenticationProvider
     */
    private static void collectGrantParams(HttpServletRequest request, Map<String, Object> target) {
        target.putAll(OAuth2EndpointUtilsAccessor
                .getParametersIfMatchesAuthorizationCodeGrantRequest(request, CONSUMED_PARAMETER_EXCLUSIONS));
    }
}
