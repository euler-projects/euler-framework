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
package org.eulerframework.web.module.oauth2.controller.admin.ajax;

import org.eulerframework.common.base.log.LogSupport;
import org.eulerframework.web.core.annotation.AjaxController;
import org.eulerframework.web.module.oauth2.endpoint.TokensEndpoint;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpoint;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.annotation.Resource;
import java.util.Collection;

/**
 * @author cFrost
 *
 */
@AjaxController
@RequestMapping("oauth/tokens")
public class TokenManageAjaxController extends LogSupport {
    
    @Resource 
    private TokensEndpoint tokensEndpoint;
    
    @GetMapping(value = "client/{clientId}")
    public Collection<OAuth2AccessToken> findTokensByClientId(
            @PathVariable("clientId") String clientId) {
        return this.tokensEndpoint.findTokensByClientId(clientId);
    }
    
    @GetMapping(value = "client/{clientId}/user/{username}")
    public Collection<OAuth2AccessToken> findTokensByClientIdAndUserName(
            @PathVariable("clientId") String clientId, 
            @PathVariable("username") String username) {
        return this.tokensEndpoint.findTokensByClientIdAndUserName(clientId, username);
    }
    
    @DeleteMapping(value = "{accessTokens}")
    public void removeAccessTokens(@PathVariable("accessTokens") String[] accessTokens) {
        this.tokensEndpoint.removeAccessTokens(accessTokens);
    }
}
