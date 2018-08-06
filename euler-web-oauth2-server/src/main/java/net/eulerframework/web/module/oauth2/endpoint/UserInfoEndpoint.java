/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2013-2018 Euler Project 
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
 * For more information, please visit the following websites
 * 
 * https://eulerproject.io
 */
package net.eulerframework.web.module.oauth2.endpoint;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpoint;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.eulerframework.web.module.authentication.context.UserContext;
import net.eulerframework.web.module.authentication.principal.EulerUserDetails;
import net.eulerframework.web.module.oauth2.vo.OAuth2User;
import net.eulerframework.web.module.oauth2.vo.UserInfo;

/**
 * @author cFrost
 *
 */
@FrameworkEndpoint
public class UserInfoEndpoint {
    @RequestMapping(value = "oauth/user_info")
    @ResponseBody
    public UserInfo userInfo() {
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

        return userInfo;
    }
}
