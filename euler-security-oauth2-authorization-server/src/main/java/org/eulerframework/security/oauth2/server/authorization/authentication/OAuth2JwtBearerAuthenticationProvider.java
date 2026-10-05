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
package org.eulerframework.security.oauth2.server.authorization.authentication;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.eulerframework.security.authentication.NonceService;
import org.eulerframework.security.core.EulerUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthenticationProviderUtilsAccessor;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContext;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@link AuthenticationProvider} for
 * {@code grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer} (RFC 7523) on the token
 * endpoint.
 * <p>
 * Spring Authorization Server has no provider for this grant &mdash; its jwt-bearer support is
 * client-side only &mdash; so the flow is implemented here, following the shape of the grant
 * providers it does ship.
 *
 * <h2>This class is the shell, not the flow</h2>
 * What it does is exactly what RFC 6749 and RFC 7523 say the same way for every issuer:
 * <ul>
 *   <li>the client is authenticated and registered for this grant, and asked for a scope it may
 *       have;</li>
 *   <li>the assertion is a well-formed JWS with a well-formed payload, naming an issuer,
 *       addressed to this authorization server and inside its lifetime;</li>
 *   <li>the assertion is spent once, and only once its signature has been accepted;</li>
 *   <li>on success, access, refresh and id tokens are issued and the authorization stored.</li>
 * </ul>
 * Everything that depends on <em>who</em> issued the assertion &mdash; where the verification key
 * comes from, which account the assertion authenticates, whether one may be opened, what the
 * result says about how the caller proved themselves &mdash; is delegated to the
 * {@link JwtBearerIssuerAuthenticator} that claims the issuer. This provider therefore holds no
 * identity model, no user store and no provisioning policy, and adding a kind of issuer does not
 * touch it.
 *
 * <h2>What it still insists on</h2>
 * Delegation is not abdication. The provider owns the one ordering rule this grant has &mdash; it
 * authenticates through the anchor, makes the assertion single-use, and only then lets the anchor
 * write &mdash; and splits the anchor's interface in two so that no implementation can reorder them.
 * It asks the anchor to authenticate once more after a provisioning, so that a first login is
 * resolved by the same code as every later one. And it status-checks an account whose principal is a
 * {@link UserDetails}, so a disabled or locked account gets no tokens whichever issuer vouched for
 * it.
 *
 * @see OAuth2JwtBearerAuthenticationToken
 * @see JwtBearerIssuerAuthenticator
 * @see JwtBearerAssertion
 */
public class OAuth2JwtBearerAuthenticationProvider implements AuthenticationProvider {

    private static final OAuth2TokenType ID_TOKEN_TOKEN_TYPE = new OAuth2TokenType(OidcParameterNames.ID_TOKEN);

    /**
     * Tolerance for a clock that is not quite synchronised, applied to {@code exp} and
     * {@code iat}.
     */
    private static final Duration CLOCK_SKEW = Duration.ofSeconds(60);

    /**
     * How far back an assertion's {@code iat} may lie and still be accepted. An assertion is a
     * single-use credential minted moments before the request, so this is generous. It is also
     * what bounds an assertion's usable life: {@code exp} is only checked against the clock and a
     * client may set it arbitrarily far ahead, so {@code iat} is the real limit.
     */
    private static final Duration MAX_ASSERTION_AGE = Duration.ofMinutes(10);

    /**
     * How long a spent {@code jti} is remembered.
     * <p>
     * Deliberately not just {@link #MAX_ASSERTION_AGE}. An {@code iat} is accepted up to one clock
     * skew in the future, so an assertion stays within its lifetime until
     * {@code iat + MAX_ASSERTION_AGE}, which for a future-dated one is a skew later than the moment
     * it was first spent. Remembering the {@code jti} for the age alone would leave that last skew
     * as a window in which a replay is still inside the assertion's lifetime but no longer
     * recognised as used. Deriving the retention from the two constants that open the window is
     * what keeps it shut if either is ever retuned.
     */
    private static final Duration JTI_RETENTION = MAX_ASSERTION_AGE.plus(CLOCK_SKEW);

    private final Logger logger = LoggerFactory.getLogger(OAuth2JwtBearerAuthenticationProvider.class);

    private final List<JwtBearerIssuerAuthenticator> issuerAuthenticators;
    private final NonceService nonceService;
    private final OAuth2AuthorizationService authorizationService;
    private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;

    private UserDetailsChecker userDetailsChecker = new AccountStatusUserDetailsChecker();

    /**
     * @param issuerAuthenticators the trust anchors, asked in order for the assertion's issuer; at
     *                             least one, or the grant can never authenticate anything
     * @param nonceService         where a spent {@code jti} is remembered; the same store the rest
     *                             of this server's one-time values use
     */
    public OAuth2JwtBearerAuthenticationProvider(List<JwtBearerIssuerAuthenticator> issuerAuthenticators,
                                                 NonceService nonceService,
                                                 OAuth2AuthorizationService authorizationService,
                                                 OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator) {
        Assert.notEmpty(issuerAuthenticators, "issuerAuthenticators must not be empty");
        Assert.notNull(nonceService, "nonceService must not be null");
        Assert.notNull(authorizationService, "authorizationService must not be null");
        Assert.notNull(tokenGenerator, "tokenGenerator must not be null");
        this.issuerAuthenticators = List.copyOf(issuerAuthenticators);
        this.nonceService = nonceService;
        this.authorizationService = authorizationService;
        this.tokenGenerator = tokenGenerator;
    }

    public void setUserDetailsChecker(UserDetailsChecker userDetailsChecker) {
        Assert.notNull(userDetailsChecker, "userDetailsChecker must not be null");
        this.userDetailsChecker = userDetailsChecker;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        OAuth2JwtBearerAuthenticationToken jwtBearerAuthenticationToken =
                (OAuth2JwtBearerAuthenticationToken) authentication;

        OAuth2ClientAuthenticationToken clientPrincipal =
                OAuth2AuthenticationProviderUtilsAccessor.getAuthenticatedClientElseThrowInvalidClient(jwtBearerAuthenticationToken);
        RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();
        if (registeredClient == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
        }
        if (!registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.JWT_BEARER)) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT);
        }

        validateScope(jwtBearerAuthenticationToken, registeredClient);
        Set<String> authorizedScopes = Collections.unmodifiableSet(jwtBearerAuthenticationToken.getScopes());

        JwtBearerAssertion assertion =
                validateAssertion(jwtBearerAuthenticationToken.getAssertion(), clientPrincipal);
        Authentication userPrincipal = authenticateAssertion(assertion);

        // The account's own usability is checked here rather than left to each issuer
        // authenticator, so that a disabled, locked or expired account is refused the same way
        // whichever issuer vouched for it. An authenticator whose principal is not a UserDetails
        // has said it is not describing a local account, and there is nothing here to check.
        if (userPrincipal.getPrincipal() instanceof UserDetails userDetails) {
            this.userDetailsChecker.check(userDetails);
        }
        if (userPrincipal instanceof AbstractAuthenticationToken userToken) {
            userToken.setDetails(jwtBearerAuthenticationToken.getDetails());
        }

        OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization.withRegisteredClient(registeredClient)
                .principalName(userPrincipal.getName())
                .authorizationGrantType(AuthorizationGrantType.JWT_BEARER)
                .authorizedScopes(authorizedScopes)
                .attribute(Principal.class.getName(), userPrincipal);

        DefaultOAuth2TokenContext.Builder tokenContextBuilder = DefaultOAuth2TokenContext.builder()
                .registeredClient(registeredClient)
                .principal(userPrincipal)
                .authorizationServerContext(AuthorizationServerContextHolder.getContext())
                .authorizationGrantType(AuthorizationGrantType.JWT_BEARER)
                .authorizedScopes(authorizedScopes)
                .authorizationGrant(jwtBearerAuthenticationToken);

        // ----- Access token -----
        OAuth2TokenContext tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.ACCESS_TOKEN).build();
        OAuth2Token generatedAccessToken = this.tokenGenerator.generate(tokenContext);
        if (generatedAccessToken == null) {
            throw JwtBearerErrors.serverError("The token generator failed to generate the access token.");
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace("Generated access token");
        }

        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
                generatedAccessToken.getTokenValue(), generatedAccessToken.getIssuedAt(),
                generatedAccessToken.getExpiresAt(), tokenContext.getAuthorizedScopes());
        if (generatedAccessToken instanceof ClaimAccessor) {
            authorizationBuilder.token(accessToken, (metadata) ->
                    metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, ((ClaimAccessor) generatedAccessToken).getClaims()));
        } else {
            authorizationBuilder.accessToken(accessToken);
        }

        // ----- Refresh token -----
        OAuth2RefreshToken refreshToken = null;
        if (registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.REFRESH_TOKEN)) {
            tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.REFRESH_TOKEN).build();
            OAuth2Token generatedRefreshToken = this.tokenGenerator.generate(tokenContext);
            if (generatedRefreshToken != null) {
                if (!(generatedRefreshToken instanceof OAuth2RefreshToken)) {
                    throw JwtBearerErrors.serverError(
                            "The token generator failed to generate a valid refresh token.");
                }

                if (this.logger.isTraceEnabled()) {
                    this.logger.trace("Generated refresh token");
                }

                refreshToken = (OAuth2RefreshToken) generatedRefreshToken;
                authorizationBuilder.refreshToken(refreshToken);
            }
        }

        // ----- ID token -----
        OidcIdToken idToken;
        if (tokenContext.getAuthorizedScopes().contains(OidcScopes.OPENID)) {
            tokenContext = tokenContextBuilder
                    .tokenType(ID_TOKEN_TOKEN_TYPE)
                    .authorization(authorizationBuilder.build())   // ID token customizer may need access to access/refresh token
                    .build();
            OAuth2Token generatedIdToken = this.tokenGenerator.generate(tokenContext);
            if (!(generatedIdToken instanceof Jwt)) {
                throw JwtBearerErrors.serverError("The token generator failed to generate the ID token.");
            }

            if (this.logger.isTraceEnabled()) {
                this.logger.trace("Generated id token");
            }

            idToken = new OidcIdToken(generatedIdToken.getTokenValue(), generatedIdToken.getIssuedAt(),
                    generatedIdToken.getExpiresAt(), ((Jwt) generatedIdToken).getClaims());
            authorizationBuilder.token(idToken, (metadata) ->
                    metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, idToken.getClaims()));
        } else {
            idToken = null;
        }

        OAuth2Authorization authorization = authorizationBuilder.build();
        this.authorizationService.save(authorization);

        Map<String, Object> additionalParameters = Collections.emptyMap();
        if (idToken != null) {
            additionalParameters = new HashMap<>();
            additionalParameters.put(OidcParameterNames.ID_TOKEN, idToken.getTokenValue());
        }

        return new OAuth2AccessTokenAuthenticationToken(
                registeredClient, clientPrincipal, accessToken, refreshToken, additionalParameters);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2JwtBearerAuthenticationToken.class.isAssignableFrom(authentication);
    }

    // ========== The assertion envelope ==========

    /**
     * Read the assertion and check everything about it that does not depend on who issued it.
     * <p>
     * What is left once this returns is the signature and the account, both of which belong to the
     * issuer's authenticator; the result is what gets handed to it.
     */
    private JwtBearerAssertion validateAssertion(String assertion,
                                                 OAuth2ClientAuthenticationToken clientPrincipal) {
        SignedJWT signedAssertion = parse(assertion);
        JWTClaimsSet claims = readClaims(signedAssertion);

        if (!StringUtils.hasText(claims.getIssuer())) {
            throw JwtBearerErrors.invalidGrant("the assertion is missing the iss claim");
        }
        validateAudience(claims);
        validateLifetime(claims);

        return new JwtBearerAssertion(signedAssertion, claims, clientPrincipal);
    }

    private static SignedJWT parse(String assertion) {
        if (!StringUtils.hasText(assertion)) {
            throw JwtBearerErrors.invalidGrant("the assertion is empty");
        }
        try {
            return SignedJWT.parse(assertion);
        } catch (ParseException | RuntimeException e) {
            throw JwtBearerErrors.invalidGrant("the assertion is not a well-formed JWS");
        }
    }

    private static JWTClaimsSet readClaims(SignedJWT signedAssertion) {
        try {
            return signedAssertion.getJWTClaimsSet();
        } catch (ParseException | RuntimeException e) {
            throw JwtBearerErrors.invalidGrant("the assertion payload is not a well-formed JWT claims set");
        }
    }

    /**
     * Check the {@code aud} required by RFC 7523 Section 3: the assertion has to be addressed to
     * this authorization server, which stops one minted for a different service from being replayed
     * here. Both identifiers of this server are accepted &mdash; its issuer and the absolute URL of
     * its token endpoint &mdash; because RFC 7523 names the token endpoint while RFC 8414 style
     * deployments commonly configure the issuer, and refusing either would only produce an
     * integration puzzle with no security gain.
     */
    private void validateAudience(JWTClaimsSet claims) {
        List<String> audience = claims.getAudience();
        if (CollectionUtils.isEmpty(audience)) {
            throw JwtBearerErrors.invalidGrant("the assertion is missing the aud claim");
        }
        Set<String> accepted = acceptedAudiences();
        if (audience.stream().noneMatch(accepted::contains)) {
            throw JwtBearerErrors.invalidGrant("the assertion aud does not identify this authorization server");
        }
    }

    private static Set<String> acceptedAudiences() {
        AuthorizationServerContext context = AuthorizationServerContextHolder.getContext();
        if (context == null || !StringUtils.hasText(context.getIssuer())) {
            // Always set by the token endpoint filter; reaching this means the provider is being
            // driven outside a request, which cannot be answered meaningfully.
            throw JwtBearerErrors.serverError("The authorization server context is not available.");
        }
        String issuer = context.getIssuer();
        Set<String> accepted = new LinkedHashSet<>(2);
        accepted.add(issuer);
        AuthorizationServerSettings settings = context.getAuthorizationServerSettings();
        if (settings != null && StringUtils.hasText(settings.getTokenEndpoint())) {
            accepted.add(issuer + settings.getTokenEndpoint());
        }
        return accepted;
    }

    /**
     * Check {@code exp} and {@code iat}. Both are required here, {@code iat} being the one that
     * bounds how long a stolen assertion stays usable; RFC 7523 leaves it optional, and requiring it
     * is a tightening rather than a deviation.
     */
    private void validateLifetime(JWTClaimsSet claims) {
        Instant now = Instant.now();

        Date expiresAt = claims.getExpirationTime();
        if (expiresAt == null) {
            throw JwtBearerErrors.invalidGrant("the assertion is missing the exp claim");
        }
        if (expiresAt.toInstant().plus(CLOCK_SKEW).isBefore(now)) {
            throw JwtBearerErrors.invalidGrant("the assertion has expired");
        }

        Date issuedAt = claims.getIssueTime();
        if (issuedAt == null) {
            throw JwtBearerErrors.invalidGrant("the assertion is missing the iat claim");
        }
        Instant issued = issuedAt.toInstant();
        if (issued.isAfter(now.plus(CLOCK_SKEW)) || issued.isBefore(now.minus(MAX_ASSERTION_AGE))) {
            throw JwtBearerErrors.invalidGrant("the assertion iat is outside the acceptable time window");
        }
    }

    // ========== Delegation ==========

    /**
     * Authenticate through the anchor that vouches for the assertion's issuer, make the assertion
     * single-use, and only then let it write.
     * <p>
     * An anchor that opened an account is asked to authenticate the assertion again rather than
     * being taken at its word about the result: {@code authenticate} is the only thing that produces
     * one, so a first login is resolved by the same code that will resolve every later login of the
     * same account, and cannot come out with a different principal or different authorities.
     */
    private Authentication authenticateAssertion(JwtBearerAssertion assertion) {
        JwtBearerIssuerAuthenticator issuerAuthenticator =
                selectIssuerAuthenticator(assertion.getIssuer(), assertion.getClientPrincipal());

        Authentication result = issuerAuthenticator.authenticate(assertion);

        // The one ordering rule this grant has, kept here rather than left to the anchors: the
        // assertion is genuine by now and nothing has been written, which is the only moment it can
        // be made single-use. Spending it earlier would let anyone who can reach the endpoint fill
        // the nonce store with values it chose, and deny a real assertion whose jti it had guessed
        // or intercepted; spending it later would let a replay run ahead of the very check meant to
        // stop it, and open an account twice.
        spendAssertion(assertion);

        if (result == null) {
            EulerUser provisioned = issuerAuthenticator.provision(assertion);
            if (provisioned == null) {
                // Why an issuer opens no accounts is a deployment fact, and naming it here would let
                // a caller tell "there is no such account" from "there is no such account and none
                // may be opened". Both have to be the same answer, and the same one an anchor gives
                // when it cannot reach an account either.
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Refusing a jwt-bearer assertion: the issuer authenticator '{}' "
                            + "opened no account for it", issuerAuthenticator.getClass().getName());
                }
                throw JwtBearerErrors.refuse();
            }
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Provisioned account '{}' for a first jwt-bearer login", provisioned.getUserId());
            }
            result = issuerAuthenticator.authenticate(assertion);
            if (result == null) {
                throw JwtBearerErrors.serverError("The issuer authenticator opened an account it then could "
                        + "not authenticate: " + issuerAuthenticator.getClass().getName());
            }
        }
        if (!result.isAuthenticated()) {
            throw JwtBearerErrors.serverError("The issuer authenticator returned an unauthenticated result: "
                    + issuerAuthenticator.getClass().getName());
        }
        return result;
    }

    /**
     * Make the assertion single-use. RFC 7523 leaves {@code jti} optional; this grant does not,
     * because without it there is nothing to make an assertion single-use.
     */
    private void spendAssertion(JwtBearerAssertion assertion) {
        String jti = assertion.getClaims().getJWTID();
        if (!StringUtils.hasText(jti)) {
            throw JwtBearerErrors.invalidGrant("the assertion is missing the jti claim");
        }
        if (!this.nonceService.recordIfAbsent(jti, JTI_RETENTION)) {
            throw JwtBearerErrors.invalidGrant("the assertion has already been used (duplicate jti)");
        }
    }

    /**
     * The first authenticator that claims this issuer, asked in the order they were configured.
     * <p>
     * That none does is the caller's error and not a server fault: the assertion names an issuer
     * this server has no reason to believe, which is exactly what RFC 7523 Section 5 leaves to be
     * agreed out of band.
     */
    private JwtBearerIssuerAuthenticator selectIssuerAuthenticator(String issuer,
                                                                   OAuth2ClientAuthenticationToken clientPrincipal) {
        for (JwtBearerIssuerAuthenticator issuerAuthenticator : this.issuerAuthenticators) {
            if (issuerAuthenticator.supports(issuer, clientPrincipal)) {
                return issuerAuthenticator;
            }
        }
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("No JwtBearerIssuerAuthenticator recognizes the assertion issuer");
        }
        throw JwtBearerErrors.invalidGrant("the assertion issuer is not trusted");
    }

    private void validateScope(OAuth2JwtBearerAuthenticationToken token, RegisteredClient registeredClient) {
        Set<String> requestedScopes = token.getScopes();
        Set<String> allowedScopes = registeredClient.getScopes();
        if (!requestedScopes.isEmpty() && !allowedScopes.containsAll(requestedScopes)) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Invalid request: requested scope is not allowed for registered client '{}'",
                        registeredClient.getId());
            }
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_SCOPE));
        }
    }
}
