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

package org.eulerframework.security.oauth2.server.authorization.client;

import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import com.nimbusds.jose.jwk.JWKSet;
import org.eulerframework.security.oauth2.server.authorization.settings.EulerConfigurationSettingNames;
import org.eulerframework.security.oauth2.server.authorization.settings.EulerOAuth2ClientSettings;
import org.eulerframework.security.oauth2.server.authorization.settings.EulerOAuth2TokenSettings;
import org.eulerframework.security.jackson.JWKSetDeserializer;
import org.eulerframework.security.jackson.JWKSetSerializer;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithm;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public final class DefaultEulerOAuth2Client implements EulerOAuth2Client {

    private String registrationId;
    private String clientId;
    private Instant clientIdIssuedAt;
    private String clientSecret;
    private Instant clientSecretExpiresAt;
    private String clientName;
    private Set<String> redirectUris;
    private String tokenEndpointAuthMethod;
    private Set<String> grantTypes;
    private Set<String> responseTypes;
    private Set<String> scopes;
    private String jwksUri;
    @JsonSerialize(using = JWKSetSerializer.class)
    @JsonDeserialize(using = JWKSetDeserializer.class)
    private JWKSet jwks;
    private Set<String> postLogoutRedirectUris;
    private String tokenEndpointAuthSigningAlgorithm;
    private String idTokenSignedResponseAlgorithm;
    private String tlsClientAuthSubjectDN;
    private Boolean tlsClientCertificateBoundAccessTokens;

    private EulerOAuth2ClientSettings clientSettings;
    private EulerOAuth2TokenSettings tokenSettings;

    // ==================== EulerOAuth2Client getters/setters ====================

    @Override
    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public Instant getClientIdIssuedAt() {
        return clientIdIssuedAt;
    }

    public void setClientIdIssuedAt(Instant clientIdIssuedAt) {
        this.clientIdIssuedAt = clientIdIssuedAt;
    }

    @Override
    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    @Override
    public Instant getClientSecretExpiresAt() {
        return clientSecretExpiresAt;
    }

    public void setClientSecretExpiresAt(Instant clientSecretExpiresAt) {
        this.clientSecretExpiresAt = clientSecretExpiresAt;
    }

    @Override
    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    @Override
    public Set<String> getRedirectUris() {
        return redirectUris;
    }

    public void setRedirectUris(Set<String> redirectUris) {
        this.redirectUris = redirectUris;
    }

    @Override
    public String getTokenEndpointAuthMethod() {
        return tokenEndpointAuthMethod;
    }

    public void setTokenEndpointAuthMethod(String tokenEndpointAuthMethod) {
        this.tokenEndpointAuthMethod = tokenEndpointAuthMethod;
    }

    @Override
    public Set<String> getGrantTypes() {
        return grantTypes;
    }

    public void setGrantTypes(Set<String> grantTypes) {
        this.grantTypes = grantTypes;
    }

    @Override
    public Set<String> getResponseTypes() {
        return responseTypes;
    }

    public void setResponseTypes(Set<String> responseTypes) {
        this.responseTypes = responseTypes;
    }

    @Override
    public Set<String> getScopes() {
        return scopes;
    }

    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }

    @Override
    public String getJwksUri() {
        return jwksUri;
    }

    public void setJwksUri(String jwksUri) {
        this.jwksUri = jwksUri;
    }

    @Override
    public JWKSet getJwks() {
        return jwks;
    }

    public void setJwks(JWKSet jwks) {
        this.jwks = jwks;
    }

    @Override
    public Set<String> getPostLogoutRedirectUris() {
        return postLogoutRedirectUris;
    }

    public void setPostLogoutRedirectUris(Set<String> postLogoutRedirectUris) {
        this.postLogoutRedirectUris = postLogoutRedirectUris;
    }

    @Override
    public String getTokenEndpointAuthSigningAlgorithm() {
        return tokenEndpointAuthSigningAlgorithm;
    }

    public void setTokenEndpointAuthSigningAlgorithm(String tokenEndpointAuthSigningAlgorithm) {
        this.tokenEndpointAuthSigningAlgorithm = tokenEndpointAuthSigningAlgorithm;
    }

    @Override
    public String getIdTokenSignedResponseAlgorithm() {
        return idTokenSignedResponseAlgorithm;
    }

    public void setIdTokenSignedResponseAlgorithm(String idTokenSignedResponseAlgorithm) {
        this.idTokenSignedResponseAlgorithm = idTokenSignedResponseAlgorithm;
    }

    @Override
    public String getTlsClientAuthSubjectDN() {
        return tlsClientAuthSubjectDN;
    }

    public void setTlsClientAuthSubjectDN(String tlsClientAuthSubjectDN) {
        this.tlsClientAuthSubjectDN = tlsClientAuthSubjectDN;
    }

    @Override
    public Boolean getTlsClientCertificateBoundAccessTokens() {
        return tlsClientCertificateBoundAccessTokens;
    }

    public void setTlsClientCertificateBoundAccessTokens(Boolean tlsClientCertificateBoundAccessTokens) {
        this.tlsClientCertificateBoundAccessTokens = tlsClientCertificateBoundAccessTokens;
    }

    @Override
    public EulerOAuth2ClientSettings getClientSettings() {
        return clientSettings;
    }

    public void setClientSettings(EulerOAuth2ClientSettings clientSettings) {
        this.clientSettings = clientSettings;
    }

    @Override
    public EulerOAuth2TokenSettings getTokenSettings() {
        return tokenSettings;
    }

    public void setTokenSettings(EulerOAuth2TokenSettings tokenSettings) {
        this.tokenSettings = tokenSettings;
    }

    // ==================== CredentialsContainer ====================

    @Override
    public void eraseCredentials() {
        this.clientSecret = null;
    }

    // ==================== Bridge ====================

    @Override
    public void reloadRegisteredClient(RegisteredClient registeredClient) {
        // token_endpoint_auth_method: RFC 7591 requires exactly one method
        if (registeredClient.getClientAuthenticationMethods() != null
                && registeredClient.getClientAuthenticationMethods().size() > 1) {
            throw new IllegalStateException(
                    "RFC 7591 requires exactly one token_endpoint_auth_method, but found "
                            + registeredClient.getClientAuthenticationMethods().size());
        }

        this.registrationId = registeredClient.getId();
        this.clientId = registeredClient.getClientId();
        this.clientIdIssuedAt = registeredClient.getClientIdIssuedAt();
        this.clientSecret = registeredClient.getClientSecret();
        this.clientSecretExpiresAt = registeredClient.getClientSecretExpiresAt();
        this.clientName = registeredClient.getClientName();

        this.tokenEndpointAuthMethod = Optional.ofNullable(registeredClient.getClientAuthenticationMethods())
                .flatMap(methods -> methods.stream().findFirst())
                .map(ClientAuthenticationMethod::getValue)
                .orElse(null);

        this.grantTypes = Optional.ofNullable(registeredClient.getAuthorizationGrantTypes())
                .map(Collection::stream)
                .map(stream -> stream
                        .map(AuthorizationGrantType::getValue)
                        .collect(Collectors.toSet()))
                .orElse(null);

        // response_types: derive from grant types (authorization_code -> code)
        if (this.grantTypes != null && this.grantTypes.contains(AuthorizationGrantType.AUTHORIZATION_CODE.getValue())) {
            this.responseTypes = Collections.singleton("code");
        } else {
            this.responseTypes = null;
        }

        this.redirectUris = Optional.ofNullable(registeredClient.getRedirectUris())
                .map(Collections::unmodifiableSet)
                .orElse(null);

        this.postLogoutRedirectUris = Optional.ofNullable(registeredClient.getPostLogoutRedirectUris())
                .map(Collections::unmodifiableSet)
                .orElse(null);

        this.scopes = Optional.ofNullable(registeredClient.getScopes())
                .map(Collections::unmodifiableSet)
                .orElse(null);

        ClientSettings registeredClientSettings = registeredClient.getClientSettings();
        if (registeredClientSettings != null) {
            // Promoted to top-level (OIDC Dynamic Registration / RFC 8705)
            this.jwksUri = registeredClientSettings.getJwkSetUrl();
            this.jwks = registeredClientSettings.getSetting(EulerConfigurationSettingNames.Client.JWKS);
            JwsAlgorithm sigAlg = registeredClientSettings.getTokenEndpointAuthenticationSigningAlgorithm();
            this.tokenEndpointAuthSigningAlgorithm = sigAlg != null ? sigAlg.getName() : null;
            this.tlsClientAuthSubjectDN = registeredClientSettings.getX509CertificateSubjectDN();

            // Euler-specific client settings — only non-RFC fields are extracted
            this.clientSettings = EulerOAuth2ClientSettings.from(registeredClientSettings);
        } else {
            this.jwksUri = null;
            this.jwks = null;
            this.tokenEndpointAuthSigningAlgorithm = null;
            this.tlsClientAuthSubjectDN = null;
            this.clientSettings = null;
        }

        TokenSettings registeredClientTokenSettings = registeredClient.getTokenSettings();
        if (registeredClientTokenSettings != null) {
            // Promoted to top-level (OIDC Dynamic Registration / RFC 8705)
            SignatureAlgorithm idTokenSigAlg = registeredClientTokenSettings.getIdTokenSignatureAlgorithm();
            this.idTokenSignedResponseAlgorithm = idTokenSigAlg != null ? idTokenSigAlg.getName() : null;
            this.tlsClientCertificateBoundAccessTokens = registeredClientTokenSettings.isX509CertificateBoundAccessTokens();

            // Euler-specific token settings — only non-RFC fields are extracted
            this.tokenSettings = EulerOAuth2TokenSettings.from(registeredClientTokenSettings);
        } else {
            this.idTokenSignedResponseAlgorithm = null;
            this.tlsClientCertificateBoundAccessTokens = null;
            this.tokenSettings = null;
        }
    }
}
