package org.eulerframework.security.authentication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eulerframework.security.core.userdetails.EulerWechatUserDetailsService;
import org.eulerframework.security.core.userdetails.UserDetailsNotFountException;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.userdetails.*;
import org.springframework.util.Assert;


public class WechatLoginCodeAuthenticationProvider
        implements AuthenticationProvider, MessageSourceAware {

    protected final Log logger = LogFactory.getLog(getClass());

    private EulerWechatUserDetailsService wechatUserDetailsService;

    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    private boolean forcePrincipalAsString = false;

    protected boolean autoCreateUserIfNotExists = false;

    private UserDetailsChecker postAuthenticationChecks = new DefaultPostAuthenticationChecks();

    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(WechatLoginCodeAuthenticationToken.class, authentication,
                () -> "Only WechatLoginCodeAuthenticationToken is supported");
        WechatLoginCodeAuthenticationToken token = (WechatLoginCodeAuthenticationToken) authentication;

        // fetch WechatUser with jscode2session
        String loginCode = (String) token.getCredentials();
        WechatUser wechatUser = new WechatUser();
        wechatUser.setOpenId("anonymous");

        UserDetails user;
        try {
            user = this.wechatUserDetailsService.loadUserByWechatUser(wechatUser);
        } catch (UserDetailsNotFountException ex) {
            this.logger.debug("Failed to find user with open ID '" + wechatUser.getOpenId() + "'");
            if (!this.autoCreateUserIfNotExists) {
                throw ex;
            }

            user = this.wechatUserDetailsService.createUser(wechatUser);
        }

        this.postAuthenticationChecks.check(user);

        Object principalToReturn = user;
        return createSuccessAuthentication(principalToReturn, authentication, user);
    }

    protected Authentication createSuccessAuthentication(Object principal, Authentication authentication,
                                                         UserDetails user) {
        // Ensure we return the original credentials the user supplied,
        // so subsequent attempts are successful even with encoded passwords.
        // Also ensure we return the original getDetails(), so that future
        // authentication events after cache expiry contain the details
        WechatLoginCodeAuthenticationToken result = WechatLoginCodeAuthenticationToken.authenticated(principal,
                authentication.getCredentials(), this.authoritiesMapper.mapAuthorities(user.getAuthorities()));
        result.setDetails(authentication.getDetails());
        this.logger.debug("Authenticated user");
        return result;
    }

    public boolean isForcePrincipalAsString() {
        return this.forcePrincipalAsString;
    }

    public boolean isAutoCreateUserIfNotExists() {
        return autoCreateUserIfNotExists;
    }

    public void setForcePrincipalAsString(boolean forcePrincipalAsString) {
        this.forcePrincipalAsString = forcePrincipalAsString;
    }

    public void setAutoCreateUserIfNotExists(boolean autoCreateUserIfNotExists) {
        this.autoCreateUserIfNotExists = autoCreateUserIfNotExists;
    }

    public void setWechatUserDetailsService(EulerWechatUserDetailsService wechatUserDetailsService) {
        this.wechatUserDetailsService = wechatUserDetailsService;
    }

    @Override
    public void setMessageSource(MessageSource messageSource) {
        this.messages = new MessageSourceAccessor(messageSource);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (WechatLoginCodeAuthenticationToken.class.isAssignableFrom(authentication));
    }

    public void setPostAuthenticationChecks(UserDetailsChecker postAuthenticationChecks) {
        this.postAuthenticationChecks = postAuthenticationChecks;
    }

    public void setAuthoritiesMapper(GrantedAuthoritiesMapper authoritiesMapper) {
        this.authoritiesMapper = authoritiesMapper;
    }

    private class DefaultPostAuthenticationChecks implements UserDetailsChecker {

        @Override
        public void check(UserDetails user) {
            if (!user.isAccountNonLocked()) {
                WechatLoginCodeAuthenticationProvider.this.logger
                        .debug("Failed to authenticate since user account is locked");
                throw new LockedException(WechatLoginCodeAuthenticationProvider.this.messages
                        .getMessage("AbstractUserDetailsAuthenticationProvider.locked", "User account is locked"));
            }
            if (!user.isEnabled()) {
                WechatLoginCodeAuthenticationProvider.this.logger
                        .debug("Failed to authenticate since user account is disabled");
                throw new DisabledException(WechatLoginCodeAuthenticationProvider.this.messages
                        .getMessage("AbstractUserDetailsAuthenticationProvider.disabled", "User is disabled"));
            }
            if (!user.isAccountNonExpired()) {
                WechatLoginCodeAuthenticationProvider.this.logger
                        .debug("Failed to authenticate since user account has expired");
                throw new AccountExpiredException(WechatLoginCodeAuthenticationProvider.this.messages
                        .getMessage("AbstractUserDetailsAuthenticationProvider.expired", "User account has expired"));
            }
            if (!user.isCredentialsNonExpired()) {
                WechatLoginCodeAuthenticationProvider.this.logger
                        .debug("Failed to authenticate since user account credentials have expired");
                throw new CredentialsExpiredException(WechatLoginCodeAuthenticationProvider.this.messages
                        .getMessage("AbstractUserDetailsAuthenticationProvider.credentialsExpired",
                                "User credentials have expired"));
            }
        }
    }
}
