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
package org.eulerframework.web.module.oauth2.endpoint;

import org.eulerframework.web.module.authentication.context.UserContext;
import org.eulerframework.web.module.authentication.entity.EulerUserEntity;
import org.eulerframework.web.module.authentication.principal.EulerUserDetails;
import org.eulerframework.web.module.authentication.service.EulerUserEntityService;
import org.eulerframework.web.module.oauth2.vo.OAuth2User;
import org.eulerframework.web.module.oauth2.vo.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.annotation.Resource;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author cFrost
 */
@FrameworkEndpoint
@RequestMapping("oauth/user_info")
@ResponseBody
public class UserInfoEndpoint {
    @Autowired(required = false)
    private EulerUserEntityService eulerUserEntityService;

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

        if (showExtData && this.eulerUserEntityService != null) {
            EulerUserEntity eulerUserEntity = this.eulerUserEntityService.loadUserByUserId(user.getUserId().toString());
            if(eulerUserEntity != null) {
                user.setEmail(eulerUserEntity.getEmail());
                user.setMobile(eulerUserEntity.getMobile());
            }
        }

        return userInfo;
    }
}
