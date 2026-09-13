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

package org.eulerframework.security.oauth2.server.authorization;

import org.eulerframework.security.authentication.otp.OneTimePasswordAuthenticationToken;
import org.eulerframework.security.authentication.wechat.WechatAuthorizationCodeAuthenticationToken;
import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.eulerframework.security.oauth2.core.oidc.EulerOidcScopes;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.security.Principal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers {@link OAuth2AuthorizationUtils#putExtendClaims}, which decides what the token
 * introspection response says about the user behind an authorization.
 * <p>
 * Grant providers do not agree on what they store under the {@code java.security.Principal}
 * attribute: some store an {@code Authentication} of their own type, some a bare
 * {@link UserDetails}. Matching on concrete token types silently drops the claims for whichever
 * grants were not enumerated, so the cases below pin the shape-based behaviour instead.
 */
class OAuth2AuthorizationUtilsTest {

    private static final String AUTHORITIES_CLAIM = "authorities";
    private static final Set<String> BOTH_SCOPES = Set.of(OidcScopes.PROFILE, EulerOidcScopes.AUTHORITIES);

    @Test
    void readsClaimsFromAnOtpGrantPrincipal() {
        Map<String, Object> claims = claimsFor(otpToken(), BOTH_SCOPES);

        assertEquals("euler", claims.get(StandardClaimNames.PREFERRED_USERNAME));
        assertAuthorities(claims, "user");
    }

    @Test
    void readsClaimsFromAWechatGrantPrincipal() {
        Authentication principal = WechatAuthorizationCodeAuthenticationToken.authenticated(
                userDetails(), "code", List.of(new SimpleGrantedAuthority("user")));

        Map<String, Object> claims = claimsFor(principal, BOTH_SCOPES);

        assertEquals("euler", claims.get(StandardClaimNames.PREFERRED_USERNAME));
        assertAuthorities(claims, "user");
    }

    @Test
    void readsClaimsFromAUsernamePasswordPrincipal() {
        Authentication principal = UsernamePasswordAuthenticationToken.authenticated(
                userDetails(), null, List.of(new SimpleGrantedAuthority("user")));

        Map<String, Object> claims = claimsFor(principal, BOTH_SCOPES);

        assertEquals("euler", claims.get(StandardClaimNames.PREFERRED_USERNAME));
        assertAuthorities(claims, "user");
    }

    @Test
    void readsClaimsFromABareUserDetailsPrincipal() {
        Map<String, Object> claims = claimsFor(userDetails(), BOTH_SCOPES);

        assertEquals("euler", claims.get(StandardClaimNames.PREFERRED_USERNAME));
        assertAuthorities(claims, "user");
    }

    @Test
    void fallsBackToTheTokenAuthoritiesWhenItCarriesNoUserDetails() {
        Authentication principal = UsernamePasswordAuthenticationToken.authenticated(
                "euler", null, List.of(new SimpleGrantedAuthority("user")));

        Map<String, Object> claims = claimsFor(principal, BOTH_SCOPES);

        assertAuthorities(claims, "user");
        assertFalse(claims.containsKey(StandardClaimNames.PREFERRED_USERNAME),
                "a username cannot be recovered from a principal that is not a UserDetails");
    }

    @Test
    void honoursScopeGating() {
        Map<String, Object> authoritiesOnly = claimsFor(otpToken(), Set.of(EulerOidcScopes.AUTHORITIES));
        assertTrue(authoritiesOnly.containsKey(AUTHORITIES_CLAIM));
        assertFalse(authoritiesOnly.containsKey(StandardClaimNames.PREFERRED_USERNAME));

        Map<String, Object> profileOnly = claimsFor(otpToken(), Set.of(OidcScopes.PROFILE));
        assertEquals("euler", profileOnly.get(StandardClaimNames.PREFERRED_USERNAME));
        assertFalse(profileOnly.containsKey(AUTHORITIES_CLAIM));
    }

    @Test
    void putsNothingForAnUnrelatedScopeOrNoPrincipalAtAll() {
        assertTrue(claimsFor(otpToken(), Set.of(OidcScopes.OPENID)).isEmpty());
        assertTrue(claimsFor(otpToken(), Set.of()).isEmpty());
        assertTrue(claimsFor(null, BOTH_SCOPES).isEmpty());
    }

    // ---- helpers ----

    /**
     * Compares by authority name: the claim carries whichever collection the source happened to
     * hold, a {@code Set} from {@code EulerUserDetails} or the {@code List} a token was built with.
     */
    private static void assertAuthorities(Map<String, Object> claims, String... expected) {
        Collection<?> actual = assertInstanceOf(Collection.class, claims.get(AUTHORITIES_CLAIM));
        assertEquals(List.of(expected), actual.stream()
                .map(authority -> ((GrantedAuthority) authority).getAuthority())
                .sorted()
                .toList());
    }

    private static Map<String, Object> claimsFor(Object principalAttribute, Set<String> scopes) {
        OAuth2Authorization.Builder builder = OAuth2Authorization.withRegisteredClient(registeredClient())
                .id("authorization-1")
                .principalName("euler")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE);
        if (principalAttribute != null) {
            builder.attribute(Principal.class.getName(), principalAttribute);
        }

        Map<String, Object> claims = new HashMap<>();
        OAuth2AuthorizationUtils.putExtendClaims(builder.build(), scopes, claims);
        return claims;
    }

    private static Authentication otpToken() {
        return OneTimePasswordAuthenticationToken.authenticated(userDetails(), null,
                List.of(new SimpleGrantedAuthority("user")));
    }

    private static EulerUserDetails userDetails() {
        return EulerUserDetails.builder()
                .userId("usr_1")
                .username("euler")
                .password("password")
                .authorities("user")
                .build();
    }

    private static RegisteredClient registeredClient() {
        return RegisteredClient.withId("client-1")
                .clientId("client-1")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("https://example.com/callback")
                .build();
    }
}
