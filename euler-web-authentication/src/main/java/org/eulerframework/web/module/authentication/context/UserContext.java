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
package org.eulerframework.web.module.authentication.context;

import java.util.Collection;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.eulerframework.cache.inMemoryCache.DefaultObjectCache;
import org.eulerframework.cache.inMemoryCache.ObjectCachePool;
import org.eulerframework.web.module.authentication.conf.SecurityConfig;
import org.eulerframework.web.module.authentication.entity.EulerUserEntity;
import org.eulerframework.web.module.authentication.exception.UserNotFoundException;
import org.eulerframework.web.module.authentication.principal.EulerUserDetails;
import org.eulerframework.web.module.authentication.service.EulerUserDetailsService;
import org.eulerframework.web.module.authentication.service.EulerUserEntityService;

public class UserContext {

    private final static DefaultObjectCache<String, EulerUserDetails> USER_CACHE = ObjectCachePool
            .generateDefaultObjectCache(SecurityConfig.getUserContextCacheLife());
    private final static DefaultObjectCache<String, EulerUserEntity> USER_ENTITY_CACHE = ObjectCachePool
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
    private static EulerUserEntityService userEntityService;
    
    public static void setUserDetailsServicel(EulerUserDetailsService userDetailsServicel) {
        UserContext.userDetailsServicel = userDetailsServicel;
    }
    
    public static void setEulerUserEntityService(EulerUserEntityService userEntityService) {
        UserContext.userEntityService = userEntityService;
    }
    
    /**
     * 获取当前用户实体
     * @return 当前用户实体
     */
    public static EulerUserEntity getCurrentUserEntity() {
        return USER_ENTITY_CACHE.get(getCurrentUser().getUserId().toString(), userId -> {
            try {
                EulerUserEntity entity = userEntityService.loadUserByUserId(userId);
                entity.eraseCredentials();
                return entity;
            } catch (UserNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
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

//        if (isOAuth2Authentication(authentication)) {
//            authentication = getOAuth2UserAuthentication(authentication);
//        }

        Object principal = authentication.getPrincipal();

        // EulerUserDetails principal
        if (EulerUserDetails.class.isAssignableFrom(principal.getClass())) {
            EulerUserDetails user = (EulerUserDetails) principal;
            USER_CACHE.put(user.getUsername(), user);
            return user;
        }
        
        // String principal
        if(principal instanceof String) {
            String username = (String)principal;
            return loadEulerUserDetails(username);
        }

        // Other UserDetails principal
        if (UserDetails.class.isAssignableFrom(principal.getClass())) {
            UserDetails userDetails = (UserDetails) principal;
            String username = userDetails.getUsername();
            return loadEulerUserDetails(username);
        }

        throw new PrincipalException("Unsupported principal type.");
    }
    
    private static EulerUserDetails loadEulerUserDetails(String username) {
        return USER_CACHE.get(username, key -> {
            EulerUserDetails eulerUserDetails = userDetailsServicel.loadUserByUsername(key);
            eulerUserDetails.eraseCredentials();
            return eulerUserDetails;
        });
    }
    
//    private static boolean isOAuth2Authentication(Authentication authentication) {
//        return OAuth2Authentication.class.isAssignableFrom(authentication.getClass());
//    }
//    
//    private static Authentication getOAuth2UserAuthentication(Authentication authentication) {
//        Assert.isTrue(isOAuth2Authentication(authentication));
//        
//        OAuth2Authentication oauth2Authentication = (OAuth2Authentication) authentication;
//        Authentication oauth2UserAuthentication = oauth2Authentication.getUserAuthentication();
//
//        if (oauth2UserAuthentication == null) {
//            throw new ClientCredentialsOAuth2AuthenticationException();
//        }
//        
//        return oauth2UserAuthentication;
//    }
    
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
