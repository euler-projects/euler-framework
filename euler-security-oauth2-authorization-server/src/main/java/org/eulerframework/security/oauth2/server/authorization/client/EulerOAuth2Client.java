package org.eulerframework.security.oauth2.server.authorization.client;

import org.eulerframework.security.oauth2.server.authorization.settings.EulerOAuth2ClientSettings;
import org.eulerframework.security.oauth2.server.authorization.settings.EulerOAuth2TokenSettings;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

/**
 * Represents an OAuth 2.0 client with a JSON-friendly flat structure aligned to
 * <a href="https://datatracker.ietf.org/doc/html/rfc7591">RFC 7591</a> client metadata.
 *
 * <p>Top-level getters correspond to standard claim names defined in
 * {@link org.springframework.security.oauth2.server.authorization.OAuth2ClientMetadataClaimNames}
 * and {@link org.eulerframework.security.oauth2.server.authorization.EulerOAuth2ClientMetadataClaimNames}.
 * Non-RFC extension settings are grouped under {@link #getClientSettings()} and {@link #getTokenSettings()}.
 */
public interface EulerOAuth2Client extends CredentialsContainer {

    // -- Euler extension (EulerOAuth2ClientMetadataClaimNames) --

    /**
     * Returns the Euler-specific internal registration identifier for this client.
     *
     * @return the registration id, or {@code null}
     */
    String getRegistrationId();

    // -- RFC 7591 / OAuth2ClientMetadataClaimNames --

    /**
     * Returns the unique client identifier issued during registration.
     *
     * @return the client id
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc7591#section-3.2.1">RFC 7591 §3.2.1</a>
     */
    String getClientId();

    /**
     * Returns the time at which the client identifier was issued.
     *
     * @return the issued-at instant, or {@code null}
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc7591#section-3.2.1">RFC 7591 §3.2.1</a>
     */
    Instant getClientIdIssuedAt();

    /**
     * Returns the client secret.
     *
     * @return the client secret, or {@code null} for public clients
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc7591#section-3.2.1">RFC 7591 §3.2.1</a>
     */
    String getClientSecret();

    /**
     * Returns the time at which the client secret expires.
     * A value of {@code null} indicates the secret does not expire.
     *
     * @return the expiration instant, or {@code null} if the secret does not expire
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc7591#section-3.2.1">RFC 7591 §3.2.1</a>
     */
    Instant getClientSecretExpiresAt();

    /**
     * Returns the human-readable name of the client.
     *
     * @return the client name, or {@code null}
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc7591#section-2">RFC 7591 §2</a>
     */
    String getClientName();

    /**
     * Returns the set of redirection URIs for use in authorization code
     * and implicit grant flows.
     *
     * @return the redirect URIs, or {@code null}
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc7591#section-2">RFC 7591 §2</a>
     */
    Set<String> getRedirectUris();

    /**
     * Returns the authentication method for the token endpoint.
     *
     * @return a single authentication method string, or {@code null}
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc7591#section-2">RFC 7591 §2</a>
     */
    String getTokenEndpointAuthMethod();

    /**
     * Returns the set of OAuth 2.0 grant types that the client may use.
     *
     * @return the grant type values (e.g. "authorization_code", "refresh_token"), or {@code null}
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc7591#section-2">RFC 7591 §2</a>
     */
    Set<String> getGrantTypes();

    /**
     * Returns the set of OAuth 2.0 response types that the client may use.
     *
     * @return the response type values (e.g. "code"), or {@code null}
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc7591#section-2">RFC 7591 §2</a>
     */
    Set<String> getResponseTypes();

    /**
     * Returns the set of scope values that the client may use.
     *
     * @return the scope values, or {@code null}
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc7591#section-2">RFC 7591 §2</a>
     */
    Set<String> getScopes();

    /**
     * Returns the URL for the client's JSON Web Key Set.
     * Mutually exclusive with {@link #getJwks()}.
     *
     * @return the JWK Set URL, or {@code null}
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc7591#section-2">RFC 7591 §2</a>
     */
    String getJwksUri();

    /**
     * Returns the client's JSON Web Key Set document containing the client's public keys.
     * Mutually exclusive with {@link #getJwksUri()}.
     *
     * @return the JWK Set as a JSON-friendly map, or {@code null}
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc7591#section-2">RFC 7591 §2</a>
     */
    Map<String, Object> getJwks();

    // -- OIDC extension --

    /**
     * Returns the set of URIs to which the RP may request that the end-user's
     * user agent be redirected after a logout.
     *
     * @return the post-logout redirect URIs, or {@code null}
     * @see <a href="https://openid.net/specs/openid-connect-rpinitiated-1_0.html">OIDC RP-Initiated Logout</a>
     */
    Set<String> getPostLogoutRedirectUris();

    // -- RFC 7591 / OIDC Dynamic Registration extension (OidcClientMetadataClaimNames) --

    /**
     * Returns the JWS algorithm that must be used for signing the JWT used to
     * authenticate the client at the token endpoint.
     *
     * @return the algorithm name (e.g. "RS256"), or {@code null}
     * @see <a href="https://openid.net/specs/openid-connect-registration-1_0.html#ClientMetadata">OIDC Dynamic Registration §2</a>
     */
    String getTokenEndpointAuthSigningAlgorithm();

    /**
     * Returns the JWS algorithm for signing the ID Token issued to this client.
     *
     * @return the algorithm name (e.g. "RS256"), or {@code null}
     * @see <a href="https://openid.net/specs/openid-connect-registration-1_0.html#ClientMetadata">OIDC Dynamic Registration §2</a>
     */
    String getIdTokenSignedResponseAlgorithm();

    // -- RFC 8705 extension (EulerOAuth2ClientMetadataClaimNames) --

    /**
     * Returns the expected subject distinguished name of the client certificate
     * used for {@code tls_client_auth} authentication.
     *
     * @return the subject DN, or {@code null}
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc8705#section-2.1.2">RFC 8705 §2.1.2</a>
     */
    String getTlsClientAuthSubjectDN();

    /**
     * Returns whether access tokens issued to this client must be bound
     * to the client's mutual-TLS certificate.
     *
     * @return {@code true} if certificate-bound, {@code null} if unspecified
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc8705#section-3.1">RFC 8705 §3.1</a>
     */
    Boolean getTlsClientCertificateBoundAccessTokens();

    // -- Non-RFC settings (pure interfaces, not AbstractSettings) --

    /**
     * Returns the non-RFC client settings (e.g. proof key, authorization consent).
     *
     * @return the client settings, or {@code null}
     */
    EulerOAuth2ClientSettings getClientSettings();

    /**
     * Returns the non-RFC token settings (e.g. TTL, format, refresh token reuse).
     *
     * @return the token settings, or {@code null}
     */
    EulerOAuth2TokenSettings getTokenSettings();

    // -- Bridge --

    /**
     * Reloads this client's state from the given Spring {@link RegisteredClient}.
     *
     * @param registeredClient the Spring registered client to read from
     */
    void reloadRegisteredClient(RegisteredClient registeredClient);
}
