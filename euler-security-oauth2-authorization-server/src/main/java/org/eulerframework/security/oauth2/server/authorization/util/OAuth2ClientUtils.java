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

package org.eulerframework.security.oauth2.server.authorization.util;

import org.eulerframework.security.oauth2.server.authorization.client.EulerOAuth2Client;
import org.eulerframework.security.oauth2.server.authorization.settings.EulerConfigurationSettingNames;
import org.eulerframework.security.oauth2.server.authorization.settings.EulerOAuth2ClientSettings;
import org.eulerframework.security.oauth2.server.authorization.settings.EulerOAuth2TokenSettings;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithm;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.ConfigurationSettingNames;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Bidirectional adapter between Euler Framework's {@link EulerOAuth2Client} domain model and
 * Spring Authorization Server's {@link RegisteredClient}.
 *
 * <p>This utility is the structural inverse of
 * {@link EulerOAuth2Client#reloadRegisteredClient(RegisteredClient)}. Several attributes
 * defined by <a href="https://openid.net/specs/openid-connect-registration-1_0.html">OIDC
 * Dynamic Client Registration</a> and
 * <a href="https://datatracker.ietf.org/doc/html/rfc8705">RFC&nbsp;8705</a> — namely
 * {@code jwks_uri}, {@code jwks}, {@code token_endpoint_auth_signing_alg},
 * {@code tls_client_auth_subject_dn}, {@code id_token_signed_response_alg} and
 * {@code tls_client_certificate_bound_access_tokens} — are promoted to first-class
 * properties of {@link EulerOAuth2Client} so that they may be persisted as dedicated
 * relational columns. When such a client is marshalled back into Spring's model, these
 * promoted fields are folded into {@link ClientSettings} or {@link TokenSettings}, whereas
 * the remaining, genuinely Euler-specific extensions are drawn from
 * {@link EulerOAuth2ClientSettings} and {@link EulerOAuth2TokenSettings}.
 *
 * <p>The class is stateless and not intended to be instantiated.
 *
 * @see EulerOAuth2Client
 * @see RegisteredClient
 * @see EulerOAuth2Client#reloadRegisteredClient(RegisteredClient)
 */
public final class OAuth2ClientUtils {

    private OAuth2ClientUtils() {
        // Utility class — not instantiable.
    }

    /**
     * Translates an {@link EulerOAuth2Client} into an equivalent Spring
     * {@link RegisteredClient}.
     *
     * <p>For each settings group ({@link ClientSettings} and {@link TokenSettings}) the
     * translation is performed in two successive stages:
     * <ol>
     *   <li><em>Typed assignment.</em> Each property exposed through a dedicated builder
     *       method is assigned via that method, performing any required type coercion
     *       (for instance {@code Long} seconds&nbsp;&rarr;&nbsp;{@link Duration},
     *       {@code String}&nbsp;&rarr;&nbsp;{@link OAuth2TokenFormat}, and
     *       {@code String}&nbsp;&rarr;&nbsp;{@link JwsAlgorithm}).</li>
     *   <li><em>Extension merge.</em> The remaining, genuinely extensible entries of the
     *       underlying settings map are copied verbatim into the builder, but only
     *       <em>after</em> the keys already consumed in stage&nbsp;1 have been stripped.
     *       This ordering is load-bearing: it prevents the raw, un-coerced values retained
     *       in the Euler settings map from overwriting their type-converted counterparts.</li>
     * </ol>
     *
     * @param eulerOAuth2Client the source client, or {@code null}
     * @return the corresponding {@link RegisteredClient}, or {@code null} when the input is {@code null}
     * @throws IllegalArgumentException if {@code eulerOAuth2Client} is non-null and carries
     *         a blank {@code registrationId}, or references an unrecognized signing-algorithm name
     */
    public static RegisteredClient toRegisteredClient(EulerOAuth2Client eulerOAuth2Client) {
        if (eulerOAuth2Client == null) {
            return null;
        }
        Assert.hasText(eulerOAuth2Client.getRegistrationId(), "registrationId cannot be empty");

        RegisteredClient.Builder builder = RegisteredClient.withId(eulerOAuth2Client.getRegistrationId());

        Optional.ofNullable(eulerOAuth2Client.getClientId()).ifPresent(builder::clientId);
        Optional.ofNullable(eulerOAuth2Client.getClientIdIssuedAt()).ifPresent(builder::clientIdIssuedAt);
        Optional.ofNullable(eulerOAuth2Client.getClientSecret()).ifPresent(builder::clientSecret);
        Optional.ofNullable(eulerOAuth2Client.getClientSecretExpiresAt()).ifPresent(builder::clientSecretExpiresAt);
        Optional.ofNullable(eulerOAuth2Client.getClientName()).ifPresent(builder::clientName);

        // RFC 7591 admits exactly one token_endpoint_auth_method per client, whereas Spring
        // models authentication methods as a (potentially multi-valued) set. We therefore
        // lift the scalar into a singleton entry.
        Optional.ofNullable(eulerOAuth2Client.getTokenEndpointAuthMethod())
                .filter(StringUtils::hasText)
                .ifPresent(method -> builder.clientAuthenticationMethod(new ClientAuthenticationMethod(method)));

        // grant_types are persisted as opaque strings per RFC 7591 and must be re-wrapped
        // as Spring's AuthorizationGrantType value objects.
        Optional.ofNullable(eulerOAuth2Client.getGrantTypes())
                .ifPresent(grantTypes -> builder.authorizationGrantTypes(g ->
                        grantTypes.forEach(type -> g.add(new AuthorizationGrantType(type)))));

        Optional.ofNullable(eulerOAuth2Client.getRedirectUris())
                .ifPresent(uris -> builder.redirectUris(u -> u.addAll(uris)));
        Optional.ofNullable(eulerOAuth2Client.getPostLogoutRedirectUris())
                .ifPresent(uris -> builder.postLogoutRedirectUris(u -> u.addAll(uris)));
        Optional.ofNullable(eulerOAuth2Client.getScopes())
                .ifPresent(scopes -> builder.scopes(s -> s.addAll(scopes)));

        builder.clientSettings(buildClientSettings(eulerOAuth2Client));
        builder.tokenSettings(buildTokenSettings(eulerOAuth2Client));

        return builder.build();
    }

    /**
     * Assembles the Spring {@link ClientSettings} from both the promoted top-level fields of
     * {@link EulerOAuth2Client} and its optional {@link EulerOAuth2ClientSettings} extension.
     *
     * <p>A {@link ClientSettings} instance is returned unconditionally so that Spring's own
     * defaults (notably {@code requireProofKey=false}) apply whenever no Euler-specific
     * configuration is supplied.
     *
     * @param client the source client (non-null)
     * @return a fully populated {@link ClientSettings}
     */
    private static ClientSettings buildClientSettings(EulerOAuth2Client client) {
        ClientSettings.Builder csBuilder = ClientSettings.builder();

        // Stage 1a — properties promoted to first-class fields of EulerOAuth2Client
        // (OIDC Dynamic Client Registration / RFC 8705).
        Optional.ofNullable(client.getJwksUri()).ifPresent(csBuilder::jwkSetUrl);
        Optional.ofNullable(client.getTokenEndpointAuthSigningAlgorithm())
                .map(OAuth2ClientUtils::jwsAlgorithm)
                .ifPresent(csBuilder::tokenEndpointAuthenticationSigningAlgorithm);
        Optional.ofNullable(client.getTlsClientAuthSubjectDN())
                .ifPresent(csBuilder::x509CertificateSubjectDN);

        // Stage 1b — Euler-specific, non-RFC properties exposed via typed getters.
        EulerOAuth2ClientSettings eulerClientSettings = client.getClientSettings();
        if (eulerClientSettings != null) {
            Optional.ofNullable(eulerClientSettings.getRequireProofKey())
                    .ifPresent(csBuilder::requireProofKey);
            Optional.ofNullable(eulerClientSettings.getRequireAuthorizationConsent())
                    .ifPresent(csBuilder::requireAuthorizationConsent);
        }

        // Stage 2 — collect any remaining extension entries. The Stage 1b keys are stripped
        // first so that the emptiness check reflects the presence of genuinely extensible
        // content, and the inline JWKS (which has no dedicated builder method) is folded
        // in last so that the top-level field takes precedence over any homonymous map
        // entry inherited from the extension block.
        Map<String, Object> extra = new HashMap<>();
        if (eulerClientSettings != null) {
            extra.putAll(eulerClientSettings.getSettings());
            extra.remove(ConfigurationSettingNames.Client.REQUIRE_PROOF_KEY);
            extra.remove(ConfigurationSettingNames.Client.REQUIRE_AUTHORIZATION_CONSENT);
        }
        if (client.getJwks() != null) {
            extra.put(EulerConfigurationSettingNames.Client.JWKS, client.getJwks());
        }
        if (!extra.isEmpty()) {
            csBuilder.settings(s -> s.putAll(extra));
        }

        return csBuilder.build();
    }

    /**
     * Assembles the Spring {@link TokenSettings} from both the promoted top-level fields of
     * {@link EulerOAuth2Client} and its optional {@link EulerOAuth2TokenSettings} extension.
     *
     * <p>A {@link TokenSettings} instance is returned unconditionally: the RFC-promoted
     * fields ({@code id_token_signed_response_alg} and
     * {@code tls_client_certificate_bound_access_tokens}) must be materialised even when
     * no Euler-specific token configuration is supplied.
     *
     * @param client the source client (non-null)
     * @return a fully populated {@link TokenSettings}
     */
    private static TokenSettings buildTokenSettings(EulerOAuth2Client client) {
        TokenSettings.Builder tsBuilder = TokenSettings.builder();

        // Stage 1a — properties promoted to first-class fields of EulerOAuth2Client
        // (OIDC Dynamic Client Registration / RFC 8705).
        Optional.ofNullable(client.getIdTokenSignedResponseAlgorithm())
                .map(OAuth2ClientUtils::signatureAlgorithm)
                .ifPresent(tsBuilder::idTokenSignatureAlgorithm);
        Optional.ofNullable(client.getTlsClientCertificateBoundAccessTokens())
                .ifPresent(tsBuilder::x509CertificateBoundAccessTokens);

        // Stage 1b — Euler-specific, non-RFC properties exposed via typed getters. Time-to-live
        // values are persisted as Long seconds for JSON friendliness and are widened here.
        EulerOAuth2TokenSettings eulerTokenSettings = client.getTokenSettings();
        if (eulerTokenSettings != null) {
            Optional.ofNullable(eulerTokenSettings.getAuthorizationCodeTimeToLive())
                    .map(Duration::ofSeconds)
                    .ifPresent(tsBuilder::authorizationCodeTimeToLive);
            Optional.ofNullable(eulerTokenSettings.getAccessTokenTimeToLive())
                    .map(Duration::ofSeconds)
                    .ifPresent(tsBuilder::accessTokenTimeToLive);
            Optional.ofNullable(eulerTokenSettings.getAccessTokenFormat())
                    .map(OAuth2TokenFormat::new)
                    .ifPresent(tsBuilder::accessTokenFormat);
            Optional.ofNullable(eulerTokenSettings.getDeviceCodeTimeToLive())
                    .map(Duration::ofSeconds)
                    .ifPresent(tsBuilder::deviceCodeTimeToLive);
            Optional.ofNullable(eulerTokenSettings.getReuseRefreshTokens())
                    .ifPresent(tsBuilder::reuseRefreshTokens);
            Optional.ofNullable(eulerTokenSettings.getRefreshTokenTimeToLive())
                    .map(Duration::ofSeconds)
                    .ifPresent(tsBuilder::refreshTokenTimeToLive);

            // Stage 2 — merge any remaining extension entries from the underlying settings
            // map. The Stage 1b keys are stripped first so that the emptiness check reflects
            // the presence of genuinely extensible content, and so that the type-coerced
            // values (Duration, OAuth2TokenFormat) assigned in Stage 1b are not silently
            // overwritten by their raw counterparts (Long seconds, String) retained in
            // EulerOAuth2TokenSettings.
            Map<String, Object> extra = new HashMap<>(eulerTokenSettings.getSettings());
            extra.remove(ConfigurationSettingNames.Token.AUTHORIZATION_CODE_TIME_TO_LIVE);
            extra.remove(ConfigurationSettingNames.Token.ACCESS_TOKEN_TIME_TO_LIVE);
            extra.remove(ConfigurationSettingNames.Token.ACCESS_TOKEN_FORMAT);
            extra.remove(ConfigurationSettingNames.Token.DEVICE_CODE_TIME_TO_LIVE);
            extra.remove(ConfigurationSettingNames.Token.REUSE_REFRESH_TOKENS);
            extra.remove(ConfigurationSettingNames.Token.REFRESH_TOKEN_TIME_TO_LIVE);
            if (!extra.isEmpty()) {
                tsBuilder.settings(s -> s.putAll(extra));
            }
        }

        return tsBuilder.build();
    }

    /**
     * Resolves a JWS algorithm identifier to its corresponding {@link JwsAlgorithm} instance,
     * searching both asymmetric {@link SignatureAlgorithm}s and symmetric {@link MacAlgorithm}s.
     *
     * <p>Used to decode {@code token_endpoint_auth_signing_alg} (OIDC Dynamic Client
     * Registration), which may reference any JWS algorithm admissible for
     * {@code private_key_jwt} or {@code client_secret_jwt} client authentication.
     *
     * @param signingAlgorithm the algorithm identifier (case-insensitive, e.g. {@code "RS256"}
     *                         or {@code "HS256"})
     * @return the resolved algorithm; never {@code null}
     * @throws IllegalArgumentException if {@code signingAlgorithm} does not denote a
     *         registered JWS algorithm
     */
    private static JwsAlgorithm jwsAlgorithm(String signingAlgorithm) {
        String name = signingAlgorithm.toUpperCase(Locale.ROOT);
        JwsAlgorithm resolved = SignatureAlgorithm.from(name);
        if (resolved == null) {
            resolved = MacAlgorithm.from(name);
        }
        if (resolved == null) {
            throw new IllegalArgumentException("Unknown JWS signing algorithm: " + signingAlgorithm);
        }
        return resolved;
    }

    /**
     * Resolves an asymmetric signature algorithm identifier to its corresponding
     * {@link SignatureAlgorithm} instance.
     *
     * <p>Used to decode {@code id_token_signed_response_alg} (OIDC Dynamic Client
     * Registration), which is constrained to an asymmetric JWS algorithm suitable for
     * signing ID Tokens.
     *
     * @param signatureAlgorithm the algorithm identifier (case-insensitive, e.g. {@code "RS256"})
     * @return the resolved algorithm; never {@code null}
     * @throws IllegalArgumentException if {@code signatureAlgorithm} does not denote a
     *         registered asymmetric JWS algorithm
     */
    private static SignatureAlgorithm signatureAlgorithm(String signatureAlgorithm) {
        SignatureAlgorithm resolved = SignatureAlgorithm.from(signatureAlgorithm.toUpperCase(Locale.ROOT));
        if (resolved == null) {
            throw new IllegalArgumentException("Unknown asymmetric JWS signing algorithm: " + signatureAlgorithm);
        }
        return resolved;
    }

}
