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

import javax.annotation.Resource;

import org.eulerframework.common.base.log.LogSupport;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpoint;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author cFrost
 *
 */
@FrameworkEndpoint
@RequestMapping("oauth/tokens")
@ResponseBody
public class TokensEndpoint extends LogSupport {
    
    @Resource 
    private TokenStore tokenStore;
    
    @GetMapping(value = "client/{clientId}}")
    public Collection<OAuth2AccessToken> userInfo(
            @PathVariable("clientId") String clientId) {
        return this.tokenStore.findTokensByClientId(clientId);
    }
    
    @GetMapping(value = "client/{clientId}/user/{username}")
    public Collection<OAuth2AccessToken> userInfo(
            @PathVariable("clientId") String clientId, 
            @PathVariable("username") String username) {
        return this.tokenStore.findTokensByClientIdAndUserName(clientId, username);
    }
    
    @DeleteMapping(value = "{accessTokens}")
    public void delToken(@PathVariable("accessTokens") String[] accessTokens) {
        for(String accessToken : accessTokens) {
            OAuth2AccessToken oauth2AccessToken = this.tokenStore.readAccessToken(accessToken);
            if(oauth2AccessToken != null) {
                this.tokenStore.removeAccessToken(oauth2AccessToken);
                this.logger.info("OAuth token was removed: {}", accessToken);
            } else {
                this.logger.info("OAuth token was not found: {}", accessToken);
            }
        }
    }
}
