/*
 * Copyright 2013-2026 the original author or authors.
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

package org.eulerframework.security.authentication.apple;

import org.eulerframework.security.core.userdetails.EulerAppleAppAttestUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.util.Assert;

/**
 * An {@link org.springframework.security.authentication.AuthenticationProvider} that
 * handles Apple App Attest assertion authentication (re-authentication with a previously
 * registered device).
 * <p>
 * Unlike {@link AppleAppAttestAttestationAuthenticationProvider}, this provider does <b>not</b>
 * support automatic user creation. The user must have been previously registered through
 * the attestation flow.
 */
public class AppleAppAttestAssertionAuthenticationProvider
        implements AuthenticationProvider, MessageSourceAware {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final AppleAppAttestValidationService validationService;

    private final EulerAppleAppAttestUserDetailsService appleAppAttestUserDetailsService;

    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    private UserDetailsChecker preAuthenticationChecks = new DefaultPreAuthenticationChecks();
    private UserDetailsChecker postAuthenticationChecks = new DefaultPostAuthenticationChecks();

    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    public AppleAppAttestAssertionAuthenticationProvider(AppleAppAttestValidationService validationService,
                                                          EulerAppleAppAttestUserDetailsService appleAppAttestUserDetailsService) {
        Assert.notNull(validationService, "validationService must not be null");
        Assert.notNull(appleAppAttestUserDetailsService, "appleAppAttestUserDetailsService must not be null");
        this.validationService = validationService;
        this.appleAppAttestUserDetailsService = appleAppAttestUserDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(AppleAppAttestAssertionAuthenticationToken.class, authentication,
                () -> "Only AppleAppAttestAssertionAuthenticationToken is supported");
        AppleAppAttestAssertionAuthenticationToken token = (AppleAppAttestAssertionAuthenticationToken) authentication;

        String keyId = token.getKeyId();
        String assertion = (String) token.getCredentials();
        String challenge = token.getChallenge();

        // Validate assertion via the delegated validation service
        AppleAppAttestUser attestUser = this.validationService.validateAssertion(keyId, assertion, challenge);

        // Assertion flow: user must already exist (registered via attestation)
        UserDetails user = this.appleAppAttestUserDetailsService.loadUserByAppleAppAttestUser(attestUser);

        this.preAuthenticationChecks.check(user);
        this.postAuthenticationChecks.check(user);

        Object principalToReturn = user;
        return createSuccessAuthentication(principalToReturn, authentication, user);
    }

    protected Authentication createSuccessAuthentication(Object principal, Authentication authentication,
                                                         UserDetails user) {
        AppleAppAttestAssertionAuthenticationToken token = (AppleAppAttestAssertionAuthenticationToken) authentication;
        AppleAppAttestAssertionAuthenticationToken result = AppleAppAttestAssertionAuthenticationToken.authenticated(
                principal, null,
                token.getKeyId(), token.getChallenge(),
                this.authoritiesMapper.mapAuthorities(user.getAuthorities()));
        result.setDetails(authentication.getDetails());
        this.logger.debug("Authenticated user via App Attest assertion");
        return result;
    }

    @Override
    public void setMessageSource(MessageSource messageSource) {
        this.messages = new MessageSourceAccessor(messageSource);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (AppleAppAttestAssertionAuthenticationToken.class.isAssignableFrom(authentication));
    }

    public void setPreAuthenticationChecks(UserDetailsChecker preAuthenticationChecks) {
        this.preAuthenticationChecks = preAuthenticationChecks;
    }

    public void setPostAuthenticationChecks(UserDetailsChecker postAuthenticationChecks) {
        this.postAuthenticationChecks = postAuthenticationChecks;
    }

    public void setAuthoritiesMapper(GrantedAuthoritiesMapper authoritiesMapper) {
        this.authoritiesMapper = authoritiesMapper;
    }

    private class DefaultPreAuthenticationChecks implements UserDetailsChecker {

        @Override
        public void check(UserDetails user) {
            if (!user.isAccountNonLocked()) {
                AppleAppAttestAssertionAuthenticationProvider.this.logger
                        .debug("Failed to authenticate since user account is locked");
                throw new LockedException(AppleAppAttestAssertionAuthenticationProvider.this.messages
                        .getMessage("AbstractUserDetailsAuthenticationProvider.locked", "User account is locked"));
            }
            if (!user.isEnabled()) {
                AppleAppAttestAssertionAuthenticationProvider.this.logger
                        .debug("Failed to authenticate since user account is disabled");
                throw new DisabledException(AppleAppAttestAssertionAuthenticationProvider.this.messages
                        .getMessage("AbstractUserDetailsAuthenticationProvider.disabled", "User is disabled"));
            }
            if (!user.isAccountNonExpired()) {
                AppleAppAttestAssertionAuthenticationProvider.this.logger
                        .debug("Failed to authenticate since user account has expired");
                throw new AccountExpiredException(AppleAppAttestAssertionAuthenticationProvider.this.messages
                        .getMessage("AbstractUserDetailsAuthenticationProvider.expired", "User account has expired"));
            }
        }
    }

    private class DefaultPostAuthenticationChecks implements UserDetailsChecker {

        @Override
        public void check(UserDetails user) {
            if (!user.isCredentialsNonExpired()) {
                AppleAppAttestAssertionAuthenticationProvider.this.logger
                        .debug("Failed to authenticate since user account credentials have expired");
                throw new CredentialsExpiredException(AppleAppAttestAssertionAuthenticationProvider.this.messages
                        .getMessage("AbstractUserDetailsAuthenticationProvider.credentialsExpired",
                                "User credentials have expired"));
            }
        }
    }
}
