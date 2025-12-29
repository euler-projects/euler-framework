package org.eulerframework.security.authentication;

import org.eulerframework.common.http.*;
import org.eulerframework.common.util.json.JacksonUtils;
import org.eulerframework.security.core.userdetails.EulerWechatUserDetailsService;
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
import org.springframework.security.core.userdetails.*;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;


public class WechatAuthorizationCodeAuthenticationProvider
        implements AuthenticationProvider, MessageSourceAware {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private EulerWechatUserDetailsService wechatUserDetailsService;

    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    private boolean forcePrincipalAsString = false;

    protected boolean autoCreateUserIfNotExists = false;

    private UserDetailsChecker postAuthenticationChecks = new DefaultPostAuthenticationChecks();

    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    private final HttpTemplate httpTemplate = new JdkHttpClientTemplate();

    private final String code2SessionEndpoint;
    private final String appid;
    private final String secret;

    public WechatAuthorizationCodeAuthenticationProvider(String code2SessionEndpoint, String appid, String secret) {
        Assert.hasText(code2SessionEndpoint, "code2SessionEndpoint must not be empty");
        Assert.hasText(appid, "appid must not be empty");
        Assert.hasText(secret, "secret must not be empty");
        this.code2SessionEndpoint = code2SessionEndpoint;
        this.appid = appid;
        this.secret = secret;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(WechatAuthorizationCodeAuthenticationToken.class, authentication,
                () -> "Only WechatAuthorizationCodeAuthenticationToken is supported");
        WechatAuthorizationCodeAuthenticationToken token = (WechatAuthorizationCodeAuthenticationToken) authentication;

        // fetch WechatUser with jscode2session
        WechatUser wechatUser = new WechatUser();
        try {
            String wechatAuthorizationCode = (String) token.getCredentials();
            HttpRequest.UriBuilderSupportBuilder requestBuilder = (HttpRequest.UriBuilderSupportBuilder) HttpRequest.get(this.code2SessionEndpoint);
            requestBuilder.query("grant_type", "authorization_code");
            requestBuilder.query("appid", this.appid);
            requestBuilder.query("secret", this.secret);
            requestBuilder.query("js_code", wechatAuthorizationCode);
            HttpRequest httpRequest = requestBuilder.build();

            String resp;
            try (HttpResponse response = httpTemplate.execute(httpRequest);
                 InputStream in = (InputStream) response.getBody().getContent()) {
                byte[] data = in.readAllBytes();
                resp = new String(data, StandardCharsets.UTF_8);
            }

            Jscode2sessionReosponse jscode2sessionReosponse;
            try {
                jscode2sessionReosponse = JacksonUtils.readValue(resp, Jscode2sessionReosponse.class);
            } catch (Exception e) {
                throw new IOException("Deserialize code2Session response failed, the original response is " + resp, e);
            }

            if (jscode2sessionReosponse.getErrcode() == null || jscode2sessionReosponse.getErrcode().equals(0)) {
                wechatUser.setOpenId(jscode2sessionReosponse.getOpenid());
                wechatUser.setUnionId(jscode2sessionReosponse.getUnionid());
                this.logger.info("✨✨✨WechatAuthorizationCode validation success, sessionKey: {}", jscode2sessionReosponse.getSession_key());
            } else {
                throw new AuthenticationServiceException(String.format("Wechat API code2Session request failed, errorCode: %d, errorMessage: %s",
                        jscode2sessionReosponse.getErrcode(),
                        jscode2sessionReosponse.getErrmsg()));
            }
        } catch (Exception e) {
            this.logger.warn("❌❌❌WechatAuthorizationCode validation failed.", e);
            wechatUser.setOpenId("anonymous");
        }

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
        WechatAuthorizationCodeAuthenticationToken result = WechatAuthorizationCodeAuthenticationToken.authenticated(principal,
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
        return (WechatAuthorizationCodeAuthenticationToken.class.isAssignableFrom(authentication));
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
                WechatAuthorizationCodeAuthenticationProvider.this.logger
                        .debug("Failed to authenticate since user account is locked");
                throw new LockedException(WechatAuthorizationCodeAuthenticationProvider.this.messages
                        .getMessage("AbstractUserDetailsAuthenticationProvider.locked", "User account is locked"));
            }
            if (!user.isEnabled()) {
                WechatAuthorizationCodeAuthenticationProvider.this.logger
                        .debug("Failed to authenticate since user account is disabled");
                throw new DisabledException(WechatAuthorizationCodeAuthenticationProvider.this.messages
                        .getMessage("AbstractUserDetailsAuthenticationProvider.disabled", "User is disabled"));
            }
            if (!user.isAccountNonExpired()) {
                WechatAuthorizationCodeAuthenticationProvider.this.logger
                        .debug("Failed to authenticate since user account has expired");
                throw new AccountExpiredException(WechatAuthorizationCodeAuthenticationProvider.this.messages
                        .getMessage("AbstractUserDetailsAuthenticationProvider.expired", "User account has expired"));
            }
            if (!user.isCredentialsNonExpired()) {
                WechatAuthorizationCodeAuthenticationProvider.this.logger
                        .debug("Failed to authenticate since user account credentials have expired");
                throw new CredentialsExpiredException(WechatAuthorizationCodeAuthenticationProvider.this.messages
                        .getMessage("AbstractUserDetailsAuthenticationProvider.credentialsExpired",
                                "User credentials have expired"));
            }
        }
    }

    public static class Jscode2sessionReosponse {
        private String session_key;//	会话密钥
        private String unionid;//	用户在开放平台的唯一标识符，若当前小程序已绑定到微信开放平台帐号下会返回，详见 UnionID 机制说明。
        private String openid;//	用户唯一标识
        private Integer errcode;//	错误码，请求失败时返回
        private String errmsg;//	错误信息，请求失败时返回

        public String getSession_key() {
            return session_key;
        }

        public void setSession_key(String session_key) {
            this.session_key = session_key;
        }

        public String getUnionid() {
            return unionid;
        }

        public void setUnionid(String unionid) {
            this.unionid = unionid;
        }

        public String getOpenid() {
            return openid;
        }

        public void setOpenid(String openid) {
            this.openid = openid;
        }

        public Integer getErrcode() {
            return errcode;
        }

        public void setErrcode(Integer errcode) {
            this.errcode = errcode;
        }

        public String getErrmsg() {
            return errmsg;
        }

        public void setErrmsg(String errmsg) {
            this.errmsg = errmsg;
        }
    }
}
