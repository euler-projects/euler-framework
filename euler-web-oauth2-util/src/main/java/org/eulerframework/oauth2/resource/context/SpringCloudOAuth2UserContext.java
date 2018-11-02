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

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import java.util.Map;

/**
 * 适用于通过euler-web-oauth2-server构建的验证服务器的/oauth/user_info获取用户信息的Spring Cloud项目
 * 
 * @author cFrost
 *
 */
public class SpringCloudOAuth2UserContext {

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

        if(OAuth2Authentication.class.isAssignableFrom(principal.getClass())) {
            rawPrincipal = (Map<String, Object>) ((OAuth2Authentication)principal).getPrincipal();
        } else {
            rawPrincipal = (Map<String, Object>) principal;
        }

        EulerOAuth2UserDetails oauth2User = new EulerOAuth2UserDetails();
        oauth2User.setUserId((String) rawPrincipal.get("userId"));
        oauth2User.setUsername((String) rawPrincipal.get("username"));
        oauth2User.setAccountNonExpired((boolean) rawPrincipal.get("accountNonExpired"));
        oauth2User.setAccountNonLocked((boolean) rawPrincipal.get("accountNonLocked"));
        oauth2User.setCredentialsNonExpired((boolean) rawPrincipal.get("credentialsNonExpired"));
        oauth2User.setEnabled((boolean) rawPrincipal.get("enabled"));
        return oauth2User;
    }
}
