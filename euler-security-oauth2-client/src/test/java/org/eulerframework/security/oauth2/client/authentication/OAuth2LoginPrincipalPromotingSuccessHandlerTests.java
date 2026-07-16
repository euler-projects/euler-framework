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
package org.eulerframework.security.oauth2.client.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eulerframework.resource.Tag;
import org.eulerframework.security.core.EulerAuthority;
import org.eulerframework.security.core.EulerUser;
import org.eulerframework.security.core.EulerUserService;
import org.eulerframework.security.core.identity.UserIdentity;
import org.eulerframework.security.core.identity.UserIdentityService;
import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.eulerframework.security.core.userdetails.UserDetailsNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.context.SecurityContextRepository;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OAuth2LoginPrincipalPromotingSuccessHandlerTests {

    @Mock
    EulerUserService userService;

    @Mock
    UserIdentityService userIdentityService;

    @Mock
    SecurityContextRepository securityContextRepository;

    private OAuth2LoginPrincipalPromotingSuccessHandler handler;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        this.mocks = MockitoAnnotations.openMocks(this);
        this.handler = new OAuth2LoginPrincipalPromotingSuccessHandler(
                this.userService, this.userIdentityService);
        this.handler.setSecurityContextRepository(this.securityContextRepository);
        this.handler.setDefaultTargetUrl("/");
    }

    @AfterEach
    void tearDown() throws Exception {
        this.mocks.close();
        SecurityContextHolder.clearContext();
    }

    @Test
    void hitsExistingIdentityAndPromotesToLocalUser() throws Exception {
        this.handler.setPoliciesByRegistrationId(Map.of(
                "google", new PerRegistrationLoginPolicy(true, List.of("user"), "google")));
        UserIdentity existing = UserIdentity.builder()
                .identityId("i-1")
                .identityType("google")
                .subject("sub-abc")
                .userId("u-42")
                .boundAt(java.time.Instant.now())
                .build();
        when(this.userIdentityService.findUserIdentityByRawSubject("google", "sub-abc"))
                .thenReturn(Optional.of(existing));
        when(this.userService.loadUserById("u-42")).thenReturn(stubUser("u-42", "existing"));

        Authentication result = invoke(this.handler, "sub-abc", Map.of("sub", "sub-abc"));

        assertThat(result).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(((EulerUserDetails) result.getPrincipal()).getUserId()).isEqualTo("u-42");
        verify(this.userService, never()).createUser(any(EulerUserDetails.class));
        verify(this.userIdentityService, never()).createUserIdentity(eq("u-42"), any(UserIdentity.class));
    }

    @Test
    void autoProvisionsOnMissAndBindsIdentity() throws Exception {
        this.handler.setPoliciesByRegistrationId(Map.of(
                "google", new PerRegistrationLoginPolicy(true, List.of("user"), "google")));
        when(this.userIdentityService.findUserIdentityByRawSubject("google", "sub-new"))
                .thenReturn(Optional.empty());
        when(this.userService.createUser(any(EulerUserDetails.class)))
                .thenReturn(stubUser("u-99", "user_random"));
        when(this.userService.loadUserById("u-99")).thenReturn(stubUser("u-99", "user_random"));

        Map<String, Object> attrs = new LinkedHashMap<>();
        attrs.put("sub", "sub-new");
        attrs.put("email", "alice@example.com");
        attrs.put("name", "Alice");

        Authentication result = invoke(this.handler, "sub-new", attrs);

        assertThat(((EulerUserDetails) result.getPrincipal()).getUserId()).isEqualTo("u-99");

        ArgumentCaptor<UserIdentity> prototypeCaptor = ArgumentCaptor.forClass(UserIdentity.class);
        verify(this.userIdentityService).createUserIdentity(eq("u-99"), prototypeCaptor.capture());
        UserIdentity prototype = prototypeCaptor.getValue();
        assertThat(prototype.getIdentityType()).isEqualTo("google");
        assertThat((String) prototype.getProperty("sub")).isEqualTo("sub-new");
        assertThat((String) prototype.getProperty("email")).isEqualTo("alice@example.com");
        assertThat((String) prototype.getProperty("name")).isEqualTo("Alice");
    }

    @Test
    void rejectsMissWhenPolicyDisablesAutoCreate() {
        this.handler.setPoliciesByRegistrationId(Map.of(
                "google", new PerRegistrationLoginPolicy(false, List.of(), "google")));
        when(this.userIdentityService.findUserIdentityByRawSubject("google", "sub-x"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> invoke(this.handler, "sub-x", Map.of("sub", "sub-x")))
                .isInstanceOf(UserDetailsNotFoundException.class);
        verify(this.userService, never()).createUser(any(EulerUserDetails.class));
    }

    @Test
    void policyIdentityTypeOverridesRegistrationId() throws Exception {
        this.handler.setPoliciesByRegistrationId(Map.of(
                "google", new PerRegistrationLoginPolicy(true, List.of("user"), "corp_google")));
        UserIdentity existing = UserIdentity.builder()
                .identityId("i-1")
                .identityType("corp_google")
                .subject("sub-abc")
                .userId("u-42")
                .boundAt(java.time.Instant.now())
                .build();
        when(this.userIdentityService.findUserIdentityByRawSubject("corp_google", "sub-abc"))
                .thenReturn(Optional.of(existing));
        when(this.userService.loadUserById("u-42")).thenReturn(stubUser("u-42", "existing"));

        Authentication result = invoke(this.handler, "sub-abc", Map.of("sub", "sub-abc"));
        assertThat(((EulerUserDetails) result.getPrincipal()).getUserId()).isEqualTo("u-42");
    }

    @Test
    void unregisteredRegistrationIdFallsBackToNoAutoCreate() {
        // No policies configured at all - success handler should synthesize
        // a fallback that rejects unknown users rather than crashing.
        when(this.userIdentityService.findUserIdentityByRawSubject("google", "sub-y"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> invoke(this.handler, "sub-y", Map.of("sub", "sub-y")))
                .isInstanceOf(UserDetailsNotFoundException.class);
        verify(this.userService, never()).createUser(any(EulerUserDetails.class));
    }

    private Authentication invoke(OAuth2LoginPrincipalPromotingSuccessHandler handler,
                                  String subject,
                                  Map<String, Object> attributes) throws Exception {
        SecurityContextHolder.clearContext();
        DefaultOAuth2User principal = new DefaultOAuth2User(
                Set.of(new SimpleGrantedAuthority("OIDC_USER")),
                attributes,
                "sub");
        // Sanity: the constructed principal's name (i.e. what the
        // handler will read via principal.getName()) must equal the
        // subject the caller wants to feed in.
        assertThat(principal.getName()).isEqualTo(subject);
        OAuth2AuthenticationToken token = new OAuth2AuthenticationToken(
                principal,
                principal.getAuthorities(),
                "google");
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, token);
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private static EulerUser stubUser(String userId, String username) {
        return new EulerUser() {
            @Override
            public String getUserId() {
                return userId;
            }

            @Override
            public String getUsername() {
                return username;
            }

            @Override
            public String getPassword() {
                return "{noop}stub";
            }

            @Override
            public Collection<? extends EulerAuthority> getAuthorities() {
                return List.of();
            }

            @Override
            public Collection<Tag> getTags() {
                return List.of();
            }

            @Override
            public boolean isAccountNonExpired() {
                return true;
            }

            @Override
            public boolean isAccountNonLocked() {
                return true;
            }

            @Override
            public boolean isCredentialsNonExpired() {
                return true;
            }

            @Override
            public boolean isEnabled() {
                return true;
            }

            @Override
            public void eraseCredentials() {
                // no-op for the stub
            }

            @Override
            public void reloadUserDetails(EulerUserDetails userDetails) {
                // no-op; the test only reads values
            }
        };
    }
}
