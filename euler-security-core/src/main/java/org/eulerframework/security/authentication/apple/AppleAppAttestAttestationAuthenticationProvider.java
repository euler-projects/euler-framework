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
import org.eulerframework.security.core.userdetails.UserDetailsNotFountException;
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

public class AppleAppAttestAttestationAuthenticationProvider
        implements AuthenticationProvider, MessageSourceAware {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final AppleAppAttestValidationService validationService;

    private final EulerAppleAppAttestUserDetailsService appleAppAttestUserDetailsService;

    private final AppleAppAttestKeyCredentialService keyCredentialService;

    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    protected boolean autoCreateUserIfNotExists = false;

    private UserDetailsChecker preAuthenticationChecks = new DefaultPreAuthenticationChecks();
    private UserDetailsChecker postAuthenticationChecks = new DefaultPostAuthenticationChecks();

    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    public AppleAppAttestAttestationAuthenticationProvider(AppleAppAttestValidationService validationService,
                                                           EulerAppleAppAttestUserDetailsService appleAppAttestUserDetailsService,
                                                           AppleAppAttestKeyCredentialService keyCredentialService) {
        Assert.notNull(validationService, "validationService must not be null");
        Assert.notNull(appleAppAttestUserDetailsService, "appleAppAttestUserDetailsService must not be null");
        Assert.notNull(keyCredentialService, "keyCredentialService must not be null");
        this.validationService = validationService;
        this.appleAppAttestUserDetailsService = appleAppAttestUserDetailsService;
        this.keyCredentialService = keyCredentialService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(AppleAppAttestAttestationAuthenticationToken.class, authentication,
                () -> "Only AppleAppAttestAttestationAuthenticationToken is supported");
        AppleAppAttestAttestationAuthenticationToken token = (AppleAppAttestAttestationAuthenticationToken) authentication;

        String keyId = token.getKeyId();
        String attestation = (String) token.getCredentials();
        String challenge = token.getChallenge();

        // Validate attestation via the delegated validation service
        AppleAppAttestUser attestUser = this.validationService.validateAttestation(keyId, attestation, challenge);

        // Save the key credential (public key + initial sign count) for future assertion verification
        if (attestUser.getPublicKey() != null) {
            AppleAppAttestKeyCredential credential = new AppleAppAttestKeyCredential(keyId, attestUser.getPublicKey(), 0);
            this.keyCredentialService.saveKeyCredential(credential);
        }

        UserDetails user;
        try {
            user = this.appleAppAttestUserDetailsService.loadUserByAppleAppAttestUser(attestUser);
            // Pre-auth checks for existing users (locked/disabled/expired)
            this.preAuthenticationChecks.check(user);
        } catch (UserDetailsNotFountException ex) {
            this.logger.debug("Failed to find user with App Attest key ID '{}'", attestUser.getKeyId());
            if (!this.autoCreateUserIfNotExists) {
                throw ex;
            }

            user = this.appleAppAttestUserDetailsService.createUser(attestUser);
        }

        this.postAuthenticationChecks.check(user);

        Object principalToReturn = user;
        return createSuccessAuthentication(principalToReturn, authentication, user);
    }

    protected Authentication createSuccessAuthentication(Object principal, Authentication authentication,
                                                         UserDetails user) {
        AppleAppAttestAttestationAuthenticationToken token = (AppleAppAttestAttestationAuthenticationToken) authentication;
        AppleAppAttestAttestationAuthenticationToken result = AppleAppAttestAttestationAuthenticationToken.authenticated(
                principal, null,
                token.getKeyId(), token.getChallenge(),
                this.authoritiesMapper.mapAuthorities(user.getAuthorities()));
        result.setDetails(authentication.getDetails());
        this.logger.debug("Authenticated user");
        return result;
    }

    public boolean isAutoCreateUserIfNotExists() {
        return autoCreateUserIfNotExists;
    }

    public void setAutoCreateUserIfNotExists(boolean autoCreateUserIfNotExists) {
        this.autoCreateUserIfNotExists = autoCreateUserIfNotExists;
    }

    @Override
    public void setMessageSource(MessageSource messageSource) {
        this.messages = new MessageSourceAccessor(messageSource);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (AppleAppAttestAttestationAuthenticationToken.class.isAssignableFrom(authentication));
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
                AppleAppAttestAttestationAuthenticationProvider.this.logger
                        .debug("Failed to authenticate since user account is locked");
                throw new LockedException(AppleAppAttestAttestationAuthenticationProvider.this.messages
                        .getMessage("AbstractUserDetailsAuthenticationProvider.locked", "User account is locked"));
            }
            if (!user.isEnabled()) {
                AppleAppAttestAttestationAuthenticationProvider.this.logger
                        .debug("Failed to authenticate since user account is disabled");
                throw new DisabledException(AppleAppAttestAttestationAuthenticationProvider.this.messages
                        .getMessage("AbstractUserDetailsAuthenticationProvider.disabled", "User is disabled"));
            }
            if (!user.isAccountNonExpired()) {
                AppleAppAttestAttestationAuthenticationProvider.this.logger
                        .debug("Failed to authenticate since user account has expired");
                throw new AccountExpiredException(AppleAppAttestAttestationAuthenticationProvider.this.messages
                        .getMessage("AbstractUserDetailsAuthenticationProvider.expired", "User account has expired"));
            }
        }
    }

    private class DefaultPostAuthenticationChecks implements UserDetailsChecker {

        @Override
        public void check(UserDetails user) {
            if (!user.isAccountNonLocked()) {
                AppleAppAttestAttestationAuthenticationProvider.this.logger
                        .debug("Failed to authenticate since user account is locked");
                throw new LockedException(AppleAppAttestAttestationAuthenticationProvider.this.messages
                        .getMessage("AbstractUserDetailsAuthenticationProvider.locked", "User account is locked"));
            }
            if (!user.isEnabled()) {
                AppleAppAttestAttestationAuthenticationProvider.this.logger
                        .debug("Failed to authenticate since user account is disabled");
                throw new DisabledException(AppleAppAttestAttestationAuthenticationProvider.this.messages
                        .getMessage("AbstractUserDetailsAuthenticationProvider.disabled", "User is disabled"));
            }
            if (!user.isAccountNonExpired()) {
                AppleAppAttestAttestationAuthenticationProvider.this.logger
                        .debug("Failed to authenticate since user account has expired");
                throw new AccountExpiredException(AppleAppAttestAttestationAuthenticationProvider.this.messages
                        .getMessage("AbstractUserDetailsAuthenticationProvider.expired", "User account has expired"));
            }
            if (!user.isCredentialsNonExpired()) {
                AppleAppAttestAttestationAuthenticationProvider.this.logger
                        .debug("Failed to authenticate since user account credentials have expired");
                throw new CredentialsExpiredException(AppleAppAttestAttestationAuthenticationProvider.this.messages
                        .getMessage("AbstractUserDetailsAuthenticationProvider.credentialsExpired",
                                "User credentials have expired"));
            }
        }
    }
}
