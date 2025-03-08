/*
 * Copyright 2013-2024 the original author or authors.
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
package org.eulerframework.web.module.authentication.provider;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.eulerframework.common.util.StringUtils;
import org.eulerframework.web.module.authentication.conf.SecurityConfig;
import org.eulerframework.web.module.authentication.entity.EulerUserEntity;
import org.eulerframework.web.module.authentication.principal.EulerUserDetails;
import org.eulerframework.web.module.authentication.service.SmsCodeValidator;
import org.eulerframework.web.module.authentication.service.SmsCodeValidator.BizCode;
import org.eulerframework.web.module.authentication.service.SmsCodeValidator.InvalidSmsCodeException;
import org.eulerframework.web.module.authentication.service.UserRegistService;

/**
 * @author cFrost
 *
 */
public class SmsCodeAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {
    
    private SmsCodeValidator smsCodeValidator;

    private UserDetailsService userDetailsService;
    
    private UserRegistService userRegistService;

    public void setSmsCodeValidator(SmsCodeValidator smsCodeValidator) {
        this.smsCodeValidator = smsCodeValidator;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return smsCodeValidator != null && super.supports(authentication);
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
            UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        if (authentication.getCredentials() == null) {
            logger.debug("Authentication failed: no credentials provided");

            throw new BadCredentialsException(messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.badCredentials",
                    "Bad credentials"));
        }

        String phone = authentication.getPrincipal().toString();
        String presentedPassword = authentication.getCredentials().toString();
        
        try {
            this.smsCodeValidator.check(phone, presentedPassword, BizCode.SIGN_IN);
        } catch (InvalidSmsCodeException e) {
            logger.debug("Authentication failed: sms code does not match stored value");

            throw new BadCredentialsException(messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.badCredentials",
                    "Bad credentials"));
        }
    }

    @Override
    protected final UserDetails retrieveUser(String username,
            UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException {
        try {
            UserDetails loadedUser = this.getUserDetailsService().loadUserByUsername(username);
            if (loadedUser == null) {
                throw new InternalAuthenticationServiceException(
                        "UserDetailsService returned null, which is an interface contract violation");
            }
            return loadedUser;
        }
        catch (UsernameNotFoundException ex) {
            
            if(SecurityConfig.isEnablePhoneAutoSignup()) {
                String phone = authentication.getPrincipal().toString();
                String presentedPassword = authentication.getCredentials().toString();
                
                try {
                    this.smsCodeValidator.check(phone, presentedPassword, BizCode.SIGN_IN);
                    /*
                     * TODO: 此处代码与SignUpAjaxController重复, 需改进
                     */
                    String password = StringUtils.randomString(16);
                    EulerUserEntity eulerUserEntity = this.userRegistService.signUp(null, null, phone, password);
                    return new EulerUserDetails(eulerUserEntity);
                } catch (InvalidSmsCodeException e) {
                    throw ex;
                }
                
            } else {
                mitigateAgainstTimingAttack(authentication);
                throw ex;
            }
            
        }
        catch (InternalAuthenticationServiceException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex);
        }
    }

    private void mitigateAgainstTimingAttack(UsernamePasswordAuthenticationToken authentication) {
        String phone = authentication.getPrincipal().toString();
        if (authentication.getCredentials() != null) {
            String presentedSmsCode = authentication.getCredentials().toString();
            this.smsCodeValidator.check(phone, presentedSmsCode, BizCode.SIGN_IN);
        }
    }

    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    protected UserDetailsService getUserDetailsService() {
        return userDetailsService;
    }

    public UserRegistService getUserRegistService() {
        return userRegistService;
    }

    public void setUserRegistService(UserRegistService userRegistService) {
        this.userRegistService = userRegistService;
    }

}
