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
package org.eulerframework.web.module.oauth2.endpoint;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eulerframework.web.module.authentication.context.UserContext.UnAuthenticatedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpoint;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import org.eulerframework.web.module.authentication.context.UserContext;
import org.eulerframework.web.module.authentication.principal.EulerUserDetails;
import org.eulerframework.web.module.oauth2.vo.OAuth2User;
import org.eulerframework.web.module.oauth2.vo.UserInfo;

import javax.annotation.Resource;

/**
 * @author cFrost
 */
@FrameworkEndpoint
@RequestMapping("oauth/user_info")
@ResponseBody
public class UserInfoEndpoint {
    @Resource
    private TokenStore tokenStore;

    @GetMapping
    public UserInfo userInfo(
            @RequestParam(required = false, defaultValue = "false") boolean showExtData
    ) {
        EulerUserDetails userDetails = UserContext.getCurrentUser();
        OAuth2User user = new OAuth2User();
        user.setUserId(userDetails.getUserId());
        user.setUsername(userDetails.getUsername());
        user.setAccountNonExpired(userDetails.isAccountNonExpired());
        user.setAccountNonLocked(userDetails.isAccountNonLocked());
        user.setCredentialsNonExpired(userDetails.isCredentialsNonExpired());
        user.setEnabled(userDetails.isEnabled());

        Set<String> authority = Optional.ofNullable(userDetails.getAuthorities()).orElse(new HashSet<>()).stream()
                .map(each -> each.getAuthority()).collect(Collectors.toSet());

        UserInfo userInfo = new UserInfo();
        userInfo.setUser(user);
        userInfo.setAuthority(authority);

        if (showExtData) {
            SecurityContext securityContext = SecurityContextHolder.getContext();
            if(securityContext != null) {
                Authentication authentication = securityContext.getAuthentication();
                if(authentication != null && OAuth2Authentication.class.isAssignableFrom(authentication.getClass())) {
                    OAuth2Authentication oAuth2Authentication = (OAuth2Authentication) authentication;

                    userInfo.setToken(oAuth2Authentication);
                }
            }
        }

        return userInfo;
    }
}
