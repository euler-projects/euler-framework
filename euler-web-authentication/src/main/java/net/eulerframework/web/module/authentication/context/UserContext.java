/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2013-2017 cFrost.sun(孙宾, SUN BIN) 
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * For more information, please visit the following website
 * 
 * https://eulerproject.io
 * https://github.com/euler-form/web-form
 * https://cfrost.net
 */
package net.eulerframework.web.module.authentication.context;

import java.util.Collection;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import net.eulerframework.cache.inMemoryCache.DefaultObjectCache;
import net.eulerframework.cache.inMemoryCache.ObjectCachePool;
import net.eulerframework.common.util.Assert;
import net.eulerframework.web.module.authentication.conf.SecurityConfig;
import net.eulerframework.web.module.authentication.entity.EulerUserDetails;
import net.eulerframework.web.module.authentication.service.EulerUserDetailsService;

public class UserContext {

    private final static DefaultObjectCache<String, EulerUserDetails> USER_CACHE = ObjectCachePool
            .generateDefaultObjectCache(SecurityConfig.getUserContextCacheLife());

    /**
     * 没有验证信息异常
     * @author cFrost
     */
    public static class UnAuthenticatedException extends RuntimeException {
        private UnAuthenticatedException() {
            super("No user was Authenticated.");
        }
    }
    
    /**
    * 验证主体信息异常
    * @author cFrost
    */
   public static class PrincipalException extends RuntimeException {
       private PrincipalException(String message) {
           super(message);
       }
   }
    
    /**
     * 当试图从一个携带client_credentials模式token的请求获取用户信息时，会抛出此异常
     * @author cFrost
     */
    public static class ClientCredentialsOAuth2AuthenticationException extends RuntimeException {
        private ClientCredentialsOAuth2AuthenticationException() {
            super("An OAuth client request which was granted with client_credentials mode has no user information.");
        }
    }

    private static EulerUserDetailsService userDetailsServicel;
    
    public static void setUserDetailsServicel(EulerUserDetailsService userDetailsServicel) {
        UserContext.userDetailsServicel = userDetailsServicel;
    }

    /**
     * 获取当前用户
     * @return 当前用户
     * @throws UnAuthenticatedException 没有用户认证信息
     * @throws ClientCredentialsOAuth2AuthenticationException 当前处在授权模式为client credentials的OAuth请求中
     * @throws PrincipalException 不支持的验证主体
     */
    public static EulerUserDetails getCurrentUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null)
            throw new UnAuthenticatedException();

        Authentication authentication = context.getAuthentication();
        if (authentication == null)
            throw new UnAuthenticatedException();

        if (isOAuth2Authentication(authentication)) {
            authentication = getOAuth2UserAuthentication(authentication);
        }

        Object principal = authentication.getPrincipal();

        // EulerUserDetails principal
        if (EulerUserDetails.class.isAssignableFrom(principal.getClass())) {
            EulerUserDetails user = (EulerUserDetails) principal;
            USER_CACHE.put(user.getUsername(), user);
            return user;
        }

        // Other UserDetails principal
        if (UserDetails.class.isAssignableFrom(principal.getClass())) {
            UserDetails userDetails = (UserDetails) principal;
            String username = userDetails.getUsername();
            return USER_CACHE.get(username, key -> {
                EulerUserDetails eulerUserDetails = userDetailsServicel.loadUserByUsername(key);
                eulerUserDetails.eraseCredentials();
                return eulerUserDetails;
            });
        }

        throw new PrincipalException("Unsupported principal type.");
    }
    
    private static boolean isOAuth2Authentication(Authentication authentication) {
        return OAuth2Authentication.class.isAssignableFrom(authentication.getClass());
    }
    
    private static Authentication getOAuth2UserAuthentication(Authentication authentication) {
        Assert.isTrue(isOAuth2Authentication(authentication));
        
        OAuth2Authentication oauth2Authentication = (OAuth2Authentication) authentication;
        Authentication oauth2UserAuthentication = oauth2Authentication.getUserAuthentication();

        if (oauth2UserAuthentication == null) {
            throw new ClientCredentialsOAuth2AuthenticationException();
        }
        
        return oauth2UserAuthentication;
    }
    
    /**
     * 提权模式, 仅提升用户权限, 不会改变用户其他信息
     * @throws UnAuthenticatedException 当试图在匿名请求时使用sudo，会抛出此异常
     */
    public static void sudo() throws UnAuthenticatedException {
        EulerUserDetails currentUser = getCurrentUser();
        
        if(currentUser == null) {
            throw new UnAuthenticatedException();
        }
        
        EulerUserDetails root = userDetailsServicel.loadUserByUsername(EulerUserDetails.ROOT_USERNAME);
        changeSecurityContext(currentUser, root.getAuthorities());
    }

    /**
     * 切换用户, 完全改变用户信息, 包括用户名
     * @param username 要切换的用户名
     * @throws UsernameNotFoundException 要切换的用户不存在
     */
    public static void su(String username) {
        EulerUserDetails suUser = userDetailsServicel.loadUserByUsername(username);
        changeSecurityContext(suUser, suUser.getAuthorities());
    }
    
    /**
     * 切换为root
     */
    public static void su() {
        su(EulerUserDetails.ROOT_USERNAME);
    }
    
    private static void changeSecurityContext(EulerUserDetails user, Collection<? extends GrantedAuthority> authorities) {
        UsernamePasswordAuthenticationToken systemToken = new UsernamePasswordAuthenticationToken(user, null,
                authorities);
        systemToken.setDetails(user);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(systemToken);
    }
}
