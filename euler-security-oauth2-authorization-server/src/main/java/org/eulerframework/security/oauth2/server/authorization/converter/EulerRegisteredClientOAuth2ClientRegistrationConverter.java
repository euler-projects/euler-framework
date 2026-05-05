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
import org.eulerframework.security.oauth2.server.authorization.EulerOAuth2ClientMetadataClaimNames;
import org.eulerframework.security.oauth2.server.authorization.settings.EulerConfigurationSettingNames;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponseType;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithm;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.OAuth2ClientRegistration;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.oidc.OidcClientMetadataClaimNames;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.util.CollectionUtils;

/**
 * A {@link Converter} that converts the provided {@link RegisteredClient} to an
 * {@link OAuth2ClientRegistration}.
 *
 * <h2>Extensions over Spring Security's default {@code RegisteredClientOAuth2ClientRegistrationConverter}:</h2>
 * <ul>
 *   <li><b>inline jwks</b> — when the client does not have a {@code jwk_set_url} configured but has
 *       a {@link JWKSet} stored in {@link ClientSettings} (under
 *       {@link EulerConfigurationSettingNames.Client#JWKS}), the converted
 *       {@link OAuth2ClientRegistration} will include the {@code jwks} claim as defined in
 *       <a href="https://datatracker.ietf.org/doc/html/rfc7591#section-2">RFC 7591 §2</a>.</li>
 *   <li><b>token_endpoint_auth_signing_alg</b> — includes the JWS algorithm used for
 *       client authentication at the token endpoint (OIDC Dynamic Registration).</li>
 *   <li><b>id_token_signed_response_alg</b> — includes the JWS algorithm for signing
 *       ID Tokens issued to this client (OIDC Dynamic Registration).</li>
 *   <li><b>tls_client_auth_subject_dn</b> — includes the expected subject DN of the
 *       client certificate for {@code tls_client_auth} (RFC 8705 §2.1.2).</li>
 *   <li><b>tls_client_certificate_bound_access_tokens</b> — includes whether access tokens
 *       must be bound to the client's mutual-TLS certificate (RFC 8705 §3.1).</li>
 * </ul>
 *
 * @author Joe Grandja (original Spring Security implementation)
 * @author Euler Framework contributors (extensions for jwks)
 * @since 7.0
 * @see RegisteredClient
 * @see OAuth2ClientRegistration
 * @see EulerConfigurationSettingNames.Client#JWKS
 */
public final class EulerRegisteredClientOAuth2ClientRegistrationConverter
        implements Converter<RegisteredClient, OAuth2ClientRegistration> {

    @Override
    public OAuth2ClientRegistration convert(RegisteredClient registeredClient) {
        // @formatter:off
		OAuth2ClientRegistration.Builder builder = OAuth2ClientRegistration.builder()
				.clientId(registeredClient.getClientId())
				.clientIdIssuedAt(registeredClient.getClientIdIssuedAt())
				.clientName(registeredClient.getClientName());

		builder
				.tokenEndpointAuthenticationMethod(registeredClient.getClientAuthenticationMethods().iterator().next().getValue());

		if (registeredClient.getClientSecret() != null) {
			builder.clientSecret(registeredClient.getClientSecret());
		}

		if (registeredClient.getClientSecretExpiresAt() != null) {
			builder.clientSecretExpiresAt(registeredClient.getClientSecretExpiresAt());
		}

		if (!CollectionUtils.isEmpty(registeredClient.getRedirectUris())) {
			builder.redirectUris((redirectUris) ->
					redirectUris.addAll(registeredClient.getRedirectUris()));
		}

		builder.grantTypes((grantTypes) ->
				registeredClient.getAuthorizationGrantTypes().forEach((authorizationGrantType) ->
						grantTypes.add(authorizationGrantType.getValue())));

		if (registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.AUTHORIZATION_CODE)) {
			builder.responseType(OAuth2AuthorizationResponseType.CODE.getValue());
		}

		if (!CollectionUtils.isEmpty(registeredClient.getScopes())) {
			builder.scopes((scopes) ->
					scopes.addAll(registeredClient.getScopes()));
		}

		ClientSettings clientSettings = registeredClient.getClientSettings();

		// Extended: support jwks claim in addition to jwk_set_url
		if (clientSettings.getJwkSetUrl() != null) {
			builder.jwkSetUrl(clientSettings.getJwkSetUrl());
		} else if(clientSettings.getSetting(EulerConfigurationSettingNames.Client.JWKS) != null){
			JWKSet jwks = clientSettings.getSetting(EulerConfigurationSettingNames.Client.JWKS);
			builder.claim(EulerOAuth2ClientMetadataClaimNames.JWKS, jwks.toJSONObject());
		}

		// Extended: token_endpoint_auth_signing_alg — OIDC Dynamic Registration
		JwsAlgorithm tokenEndpointAuthSigningAlg = clientSettings.getTokenEndpointAuthenticationSigningAlgorithm();
		if (tokenEndpointAuthSigningAlg != null) {
			builder.claim(OidcClientMetadataClaimNames.TOKEN_ENDPOINT_AUTH_SIGNING_ALG, tokenEndpointAuthSigningAlg.getName());
		}

		// Extended: tls_client_auth_subject_dn — RFC 8705 §2.1.2
		String tlsClientAuthSubjectDN = clientSettings.getX509CertificateSubjectDN();
		if (tlsClientAuthSubjectDN != null) {
			builder.claim(EulerOAuth2ClientMetadataClaimNames.TLS_CLIENT_AUTH_SUBJECT_DN, tlsClientAuthSubjectDN);
		}

		TokenSettings tokenSettings = registeredClient.getTokenSettings();

		// Extended: id_token_signed_response_alg — OIDC Dynamic Registration
		SignatureAlgorithm idTokenSignatureAlg = tokenSettings.getIdTokenSignatureAlgorithm();
		if (idTokenSignatureAlg != null) {
			builder.claim(OidcClientMetadataClaimNames.ID_TOKEN_SIGNED_RESPONSE_ALG, idTokenSignatureAlg.getName());
		}

		// Extended: tls_client_certificate_bound_access_tokens — RFC 8705 §3.1
		if (tokenSettings.isX509CertificateBoundAccessTokens()) {
			builder.claim(EulerOAuth2ClientMetadataClaimNames.TLS_CLIENT_CERTIFICATE_BOUND_ACCESS_TOKENS, true);
		}

		return builder.build();
		// @formatter:on
    }

}
