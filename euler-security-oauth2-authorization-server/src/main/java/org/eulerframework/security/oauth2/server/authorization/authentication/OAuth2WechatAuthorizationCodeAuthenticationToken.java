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
package org.eulerframework.security.oauth2.server.authorization.authentication;

import org.eulerframework.security.oauth2.core.EulerAuthorizationGrantType;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OAuth2WechatAuthorizationCodeAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {
    private final String wechatAuthorizationCode;
    private final Set<String> scopes;

    public OAuth2WechatAuthorizationCodeAuthenticationToken(
            String wechatAuthorizationCode,
            Authentication clientPrincipal,
            @Nullable Set<String> scopes,
            @Nullable Map<String, Object> additionalParameters) {
        super(EulerAuthorizationGrantType.WECHAT_AUTHORIZATION_CODE, clientPrincipal, additionalParameters);
        this.wechatAuthorizationCode = wechatAuthorizationCode;
        this.scopes = Collections.unmodifiableSet(
                scopes != null ?
                        new HashSet<>(scopes) :
                        Collections.emptySet());
    }

    public String getWechatAuthorizationCode() {
        return wechatAuthorizationCode;
    }

    public Set<String> getScopes() {
        return scopes;
    }
}
