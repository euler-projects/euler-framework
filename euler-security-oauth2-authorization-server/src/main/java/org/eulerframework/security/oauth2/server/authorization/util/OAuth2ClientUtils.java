package org.eulerframework.security.oauth2.server.authorization.util;

import com.nimbusds.jose.jwk.JWKSet;
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
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class OAuth2ClientUtils {
    public static RegisteredClient toRegisteredClient(EulerOAuth2Client eulerOAuth2Client) {
        if (eulerOAuth2Client == null) {
            return null;
        }

        RegisteredClient.Builder builder = RegisteredClient.withId(eulerOAuth2Client.getRegistrationId());

        Optional.ofNullable(eulerOAuth2Client.getClientId()).ifPresent(builder::clientId);
        Optional.ofNullable(eulerOAuth2Client.getClientIdIssuedAt()).ifPresent(builder::clientIdIssuedAt);
        Optional.ofNullable(eulerOAuth2Client.getClientSecret()).ifPresent(builder::clientSecret);
        Optional.ofNullable(eulerOAuth2Client.getClientSecretExpiresAt()).ifPresent(builder::clientSecretExpiresAt);
        Optional.ofNullable(eulerOAuth2Client.getClientName()).ifPresent(builder::clientName);

        // token_endpoint_auth_method (single string) -> singleton set
        Optional.ofNullable(eulerOAuth2Client.getTokenEndpointAuthMethod())
                .filter(StringUtils::hasText)
                .ifPresent(method -> builder.clientAuthenticationMethod(new ClientAuthenticationMethod(method)));

        // grant_types (Set<String>) -> Set<AuthorizationGrantType>
        Optional.ofNullable(eulerOAuth2Client.getGrantTypes())
                .ifPresent(grantTypes -> builder.authorizationGrantTypes(g ->
                        grantTypes.forEach(type -> g.add(new AuthorizationGrantType(type)))));

        Optional.ofNullable(eulerOAuth2Client.getRedirectUris())
                .ifPresent(uris -> builder.redirectUris(u -> u.addAll(uris)));
        Optional.ofNullable(eulerOAuth2Client.getPostLogoutRedirectUris())
                .ifPresent(uris -> builder.postLogoutRedirectUris(u -> u.addAll(uris)));
        Optional.ofNullable(eulerOAuth2Client.getScopes())
                .ifPresent(scopes -> builder.scopes(s -> s.addAll(scopes)));

        // Build Spring ClientSettings from flat fields + EulerOAuth2ClientSettings
        builder.clientSettings(buildClientSettings(eulerOAuth2Client));

        // Build Spring TokenSettings from EulerOAuth2TokenSettings + top-level fields
        Optional.ofNullable(OAuth2ClientUtils.buildTokenSettings(eulerOAuth2Client))
                .ifPresent(builder::tokenSettings);

        return builder.build();
    }

    private static ClientSettings buildClientSettings(EulerOAuth2Client client) {
        ClientSettings.Builder csBuilder = ClientSettings.builder();

        // Fields promoted from EulerOAuth2Client top-level (RFC 7591)
        Optional.ofNullable(client.getJwksUri()).ifPresent(csBuilder::jwkSetUrl);

        Optional.ofNullable(client.getJwks()).ifPresent(jwksMap -> {
            try {
                JWKSet jwkSet = JWKSet.parse(jwksMap);
                csBuilder.setting(EulerConfigurationSettingNames.Client.JWKS, jwkSet);
            } catch (ParseException e) {
                throw new IllegalStateException("Failed to parse jwks", e);
            }
        });

        // Fields from EulerOAuth2ClientSettings interface
        EulerOAuth2ClientSettings cs = client.getClientSettings();
        if (cs != null) {
            Optional.ofNullable(cs.getRequireProofKey()).ifPresent(csBuilder::requireProofKey);
            Optional.ofNullable(cs.getRequireAuthorizationConsent()).ifPresent(csBuilder::requireAuthorizationConsent);
        }

        // Fields promoted to EulerOAuth2Client top-level (OIDC Dynamic Registration / RFC 8705)
        Optional.ofNullable(client.getTokenEndpointAuthSigningAlgorithm())
                .map(OAuth2ClientUtils::jwsAlgorithm)
                .ifPresent(csBuilder::tokenEndpointAuthenticationSigningAlgorithm);
        Optional.ofNullable(client.getTlsClientAuthSubjectDN()).ifPresent(csBuilder::x509CertificateSubjectDN);

        return csBuilder.build();
    }

    private static TokenSettings buildTokenSettings(EulerOAuth2Client client) {
        EulerOAuth2TokenSettings ts = client.getTokenSettings();
        if (ts == null) {
            return null;
        }

        TokenSettings.Builder tsBuilder = TokenSettings.builder();

        Optional.ofNullable(ts.getAuthorizationCodeTimeToLive())
                .map(Duration::ofSeconds)
                .ifPresent(tsBuilder::authorizationCodeTimeToLive);
        Optional.ofNullable(ts.getAccessTokenTimeToLive())
                .map(Duration::ofSeconds)
                .ifPresent(tsBuilder::accessTokenTimeToLive);
        Optional.ofNullable(ts.getAccessTokenFormat())
                .map(OAuth2TokenFormat::new)
                .ifPresent(tsBuilder::accessTokenFormat);
        Optional.ofNullable(ts.getDeviceCodeTimeToLive())
                .map(Duration::ofSeconds)
                .ifPresent(tsBuilder::deviceCodeTimeToLive);
        Optional.ofNullable(ts.getReuseRefreshTokens()).ifPresent(tsBuilder::reuseRefreshTokens);
        Optional.ofNullable(ts.getRefreshTokenTimeToLive())
                .map(Duration::ofSeconds)
                .ifPresent(tsBuilder::refreshTokenTimeToLive);

        // Fields promoted to EulerOAuth2Client top-level (OIDC Dynamic Registration / RFC 8705)
        Optional.ofNullable(client.getIdTokenSignedResponseAlgorithm())
                .map(OAuth2ClientUtils::signatureAlgorithm)
                .ifPresent(tsBuilder::idTokenSignatureAlgorithm);
        Optional.ofNullable(client.getTlsClientCertificateBoundAccessTokens())
                .ifPresent(tsBuilder::x509CertificateBoundAccessTokens);

        return tsBuilder.build();
    }

    private static JwsAlgorithm jwsAlgorithm(String signingAlgorithm) {
        String name = signingAlgorithm.toUpperCase(Locale.ROOT);
        JwsAlgorithm jwsAlgorithm = SignatureAlgorithm.from(name);
        if (jwsAlgorithm == null) {
            jwsAlgorithm = MacAlgorithm.from(name);
        }
        return jwsAlgorithm;
    }

    private static SignatureAlgorithm signatureAlgorithm(String signatureAlgorithm) {
        return SignatureAlgorithm.from(signatureAlgorithm.toUpperCase(Locale.ROOT));
    }

}
