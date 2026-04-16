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

package org.eulerframework.security.oauth2.server.authorization.converter;

import com.nimbusds.jose.jwk.JWKSet;
import org.eulerframework.security.oauth2.core.EulerClientAuthenticationMethod;
import org.eulerframework.security.oauth2.server.authorization.EulerOAuth2ClientMetadataClaimNames;
import org.eulerframework.security.oauth2.server.authorization.settings.EulerConfigurationSettingNames;
import org.eulerframework.security.oauth2.core.endpoint.EulerOAuth2ParameterNames;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponseType;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithm;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.OAuth2ClientRegistration;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.converter.OAuth2ClientRegistrationRegisteredClientConverter;
import org.springframework.security.oauth2.server.authorization.oidc.OidcClientMetadataClaimNames;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

/**
 * A {@link Converter} that converts the provided {@link OAuth2ClientRegistration} to a
 * {@link RegisteredClient}.
 *
 * <h3>Extensions over Spring Security's default {@link OAuth2ClientRegistrationRegisteredClientConverter}:</h3>
 * <ul>
 *   <li><b>private_key_jwt</b> — full support for RFC 7591 §2 {@code private_key_jwt} authentication.
 *       Extracts client public keys from the {@code jwks} claim and sets
 *       {@link ClientAuthenticationMethod#PRIVATE_KEY_JWT}. No {@code client_secret} is generated.</li>
 *   <li><b>client_secret_jwt</b> — generates {@code client_secret} and sets
 *       {@link ClientAuthenticationMethod#CLIENT_SECRET_JWT}.</li>
 *   <li><b>inline jwks</b> — parses the {@code jwks} claim (RFC 7591 §2) from the registration request
 *       and stores it in {@code ClientSettings} via the {@code settings.client.jwks} setting.
 *       The Spring default converter ignores this field entirely (only reads {@code jwks_uri}).</li>
 *   <li><b>id_token_signed_response_alg</b> — reads the OIDC Dynamic Registration claim and
 *       applies it to {@link TokenSettings} as the ID Token signature algorithm.</li>
 *   <li><b>tls_client_auth_subject_dn</b> — reads the RFC 8705 §2.1.2 claim and
 *       applies it to {@link ClientSettings} as the expected client certificate subject DN.</li>
 *   <li><b>tls_client_certificate_bound_access_tokens</b> — reads the RFC 8705 §3.1 claim
 *       and applies it to {@link TokenSettings}.</li>
 * </ul>
 *
 * @author Joe Grandja (original Spring Security implementation)
 * @author Euler Framework contributors (extensions for private_key_jwt and jwks)
 * @since 7.0
 * @see OAuth2ClientRegistration
 * @see RegisteredClient
 * @see EulerConfigurationSettingNames.Client#JWKS
 */
public final class EulerOAuth2ClientRegistrationRegisteredClientConverter
        implements Converter<OAuth2ClientRegistration, RegisteredClient> {

    private static final StringKeyGenerator CLIENT_ID_GENERATOR = new Base64StringKeyGenerator(
            Base64.getUrlEncoder().withoutPadding(), 32);

    private static final StringKeyGenerator CLIENT_SECRET_GENERATOR = new Base64StringKeyGenerator(
            Base64.getUrlEncoder().withoutPadding(), 48);

    @Override
    public RegisteredClient convert(OAuth2ClientRegistration clientRegistration) {
        // @formatter:off
		RegisteredClient.Builder builder = RegisteredClient.withId(UUID.randomUUID().toString())
				.clientId(CLIENT_ID_GENERATOR.generateKey())
				.clientIdIssuedAt(Instant.now())
				.clientName(clientRegistration.getClientName());

		String authMethod = clientRegistration.getTokenEndpointAuthenticationMethod();

		if (ClientAuthenticationMethod.CLIENT_SECRET_POST.getValue().equals(authMethod)) {
			builder
					.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
					.clientSecret(CLIENT_SECRET_GENERATOR.generateKey());
		}
		else if (ClientAuthenticationMethod.CLIENT_SECRET_JWT.getValue().equals(authMethod)) {
			builder
					.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_JWT)
					.clientSecret(CLIENT_SECRET_GENERATOR.generateKey());
		}
		else if (ClientAuthenticationMethod.PRIVATE_KEY_JWT.getValue().equals(authMethod)) {
			builder.clientAuthenticationMethod(ClientAuthenticationMethod.PRIVATE_KEY_JWT);
			// No client_secret for private_key_jwt — authentication uses client-signed JWT
		}
		else if (ClientAuthenticationMethod.NONE.getValue().equals(authMethod)) {
			builder.clientAuthenticationMethod(ClientAuthenticationMethod.NONE);
			// Public client — no client_secret
		}
		else if (EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH.getValue().equals(authMethod)) {
			builder.clientAuthenticationMethod(EulerClientAuthenticationMethod.ATTEST_JWT_CLIENT_AUTH);
			// Attestation-based client — no client_secret
		}
		else {
			// Default: client_secret_basic (Spring original behavior)
			builder
					.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
					.clientSecret(CLIENT_SECRET_GENERATOR.generateKey());
		}

		if (!CollectionUtils.isEmpty(clientRegistration.getRedirectUris())) {
			builder.redirectUris((redirectUris) ->
					redirectUris.addAll(clientRegistration.getRedirectUris()));
		}

		if (!CollectionUtils.isEmpty(clientRegistration.getGrantTypes())) {
			builder.authorizationGrantTypes((authorizationGrantTypes) ->
					clientRegistration.getGrantTypes().forEach((grantType) ->
							authorizationGrantTypes.add(new AuthorizationGrantType(grantType))));
		}
		else {
			builder.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE);
		}

		if (!CollectionUtils.isEmpty(clientRegistration.getResponseTypes()) &&
				clientRegistration.getResponseTypes().contains(OAuth2AuthorizationResponseType.CODE.getValue())) {
			builder.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE);
		}

		if (!CollectionUtils.isEmpty(clientRegistration.getScopes())) {
			builder.scopes((scopes) ->
					scopes.addAll(clientRegistration.getScopes()));
		}

		ClientSettings.Builder clientSettingsBuilder = ClientSettings.builder()
				.requireProofKey(true)
				.requireAuthorizationConsent(true);
		if (clientRegistration.getJwkSetUrl() != null) {
			clientSettingsBuilder.jwkSetUrl(clientRegistration.getJwkSetUrl().toString());
		}

		// Handle jwks (inline) — RFC 7591 §2, mutually exclusive with jwks_uri
		@SuppressWarnings("unchecked")
		Map<String, Object> jwksClaim = (Map<String, Object>) clientRegistration.getClaims().get(EulerOAuth2ClientMetadataClaimNames.JWKS);
		if (jwksClaim != null) {
			Assert.isNull(clientRegistration.getJwkSetUrl(),
					"The 'jwks_uri' and 'jwks' parameters MUST NOT both be present in the same registration.");
			try {
				JWKSet jwks = JWKSet.parse(jwksClaim);
				clientSettingsBuilder.setting(EulerConfigurationSettingNames.Client.JWKS, jwks);
			}
			catch (ParseException e) {
				throw new IllegalArgumentException("Failed to parse jwks claim: " + e.getMessage(), e);
			}
		}

		// Handle token_endpoint_auth_signing_alg — OIDC Dynamic Registration
		// Required for private_key_jwt and client_secret_jwt to validate JWT assertions
		String signingAlg = (String) clientRegistration.getClaims().get(OidcClientMetadataClaimNames.TOKEN_ENDPOINT_AUTH_SIGNING_ALG);
		if (signingAlg != null) {
			JwsAlgorithm algorithm = resolveSignatureAlgorithm(signingAlg);
			if (algorithm != null) {
				clientSettingsBuilder.tokenEndpointAuthenticationSigningAlgorithm(algorithm);
			}
		}

		// Handle tls_client_auth_subject_dn — RFC 8705 §2.1.2
		String tlsClientAuthSubjectDN = (String) clientRegistration.getClaims().get(EulerOAuth2ClientMetadataClaimNames.TLS_CLIENT_AUTH_SUBJECT_DN);
		if (tlsClientAuthSubjectDN != null) {
			clientSettingsBuilder.x509CertificateSubjectDN(tlsClientAuthSubjectDN);
		}

		TokenSettings.Builder tokenSettingsBuilder = TokenSettings.builder();

		// Handle id_token_signed_response_alg — OIDC Dynamic Registration
		String idTokenSignedResponseAlg = (String) clientRegistration.getClaims().get(OidcClientMetadataClaimNames.ID_TOKEN_SIGNED_RESPONSE_ALG);
		if (idTokenSignedResponseAlg != null) {
			SignatureAlgorithm idTokenAlgorithm = SignatureAlgorithm.from(idTokenSignedResponseAlg);
			if (idTokenAlgorithm != null) {
				tokenSettingsBuilder.idTokenSignatureAlgorithm(idTokenAlgorithm);
			}
		}

		// Handle tls_client_certificate_bound_access_tokens — RFC 8705 §3.1
		Boolean tlsCertBoundTokens = (Boolean) clientRegistration.getClaims().get(EulerOAuth2ClientMetadataClaimNames.TLS_CLIENT_CERTIFICATE_BOUND_ACCESS_TOKENS);
		if (tlsCertBoundTokens != null) {
			tokenSettingsBuilder.x509CertificateBoundAccessTokens(tlsCertBoundTokens);
		}

		builder
				.clientSettings(clientSettingsBuilder.build())
				.tokenSettings(tokenSettingsBuilder.build());

		return builder.build();
		// @formatter:on
    }

    /**
     * Resolves a JWS algorithm name (e.g. "RS256", "ES256", "HS256") to a Spring Security
     * {@link JwsAlgorithm}. Returns {@code null} if the algorithm is not recognized.
     */
    private static JwsAlgorithm resolveSignatureAlgorithm(String algName) {
        Assert.hasText(algName, "algName cannot be empty");
        // Try HMAC-based first (for client_secret_jwt)
        MacAlgorithm macAlg = MacAlgorithm.from(algName);
        if (macAlg != null) {
            return macAlg;
        }
        // Try signature algorithms (for private_key_jwt)
        return SignatureAlgorithm.from(algName);
    }

}
