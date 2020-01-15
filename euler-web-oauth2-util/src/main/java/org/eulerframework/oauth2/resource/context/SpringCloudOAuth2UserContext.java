/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eulerframework.oauth2.resource.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * 适用于通过euler-web-oauth2-server构建的验证服务器的/oauth/user_info获取用户信息的Spring Cloud项目
 * 
 * @author cFrost
 *
 */
public class SpringCloudOAuth2UserContext {

    private final static Logger LOGGER = LoggerFactory.getLogger(SpringCloudOAuth2UserContext.class);

    public static EulerOAuth2UserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        try {
            return extracttPrincipal(((OAuth2Authentication) authentication).getUserAuthentication().getPrincipal());
        } catch (Exception e) {
            throw new RuntimeException("Some exception was thrown, Only Euler Web OAuth2 Authentication is supported. Exception: " + e.getMessage(), e);
        }
    }
    
    public static EulerOAuth2UserDetails extracttPrincipal(Object principal) {

        Map<String, Object> rawPrincipal;
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        if(OAuth2Authentication.class.isAssignableFrom(principal.getClass())) {
            OAuth2Authentication oAuth2Authentication = (OAuth2Authentication)principal;
            rawPrincipal = (Map<String, Object>) oAuth2Authentication.getPrincipal();
            grantedAuthorities = oAuth2Authentication.getAuthorities();
        } else {
            rawPrincipal = (Map<String, Object>) principal;
            LOGGER.warn("Unknown Principal, no authorities was added to EulerOAuth2UserDetails, type: {}, text: {}", rawPrincipal.getClass().getName(), rawPrincipal);
        }

        EulerOAuth2UserDetails oauth2User = new EulerOAuth2UserDetails();
        oauth2User.setUserId((String) rawPrincipal.get("userId"));
        oauth2User.setUsername((String) rawPrincipal.get("username"));
        oauth2User.setAccountNonExpired((boolean) rawPrincipal.get("accountNonExpired"));
        oauth2User.setAccountNonLocked((boolean) rawPrincipal.get("accountNonLocked"));
        oauth2User.setCredentialsNonExpired((boolean) rawPrincipal.get("credentialsNonExpired"));
        oauth2User.setEnabled((boolean) rawPrincipal.get("enabled"));
        oauth2User.setAuthorities(grantedAuthorities);
        return oauth2User;
    }
}
