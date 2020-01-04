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
package org.eulerframework.web.module.oauth2.vo;

import org.springframework.security.oauth2.common.OAuth2AccessToken;

import java.util.Collection;

/**
 * @author cFrost
 *
 */
public class UserInfo {
    private OAuth2User user;
    private Collection<String> authority;
    private OAuth2AccessToken token;

    public OAuth2User getUser() {
        return user;
    }

    public void setUser(OAuth2User user) {
        this.user = user;
    }

    public Collection<String> getAuthority() {
        return authority;
    }

    public void setAuthority(Collection<String> authority) {
        this.authority = authority;
    }

    public OAuth2AccessToken getToken() {
        return token;
    }

    public void setToken(OAuth2AccessToken token) {
        this.token = token;
    }
}
