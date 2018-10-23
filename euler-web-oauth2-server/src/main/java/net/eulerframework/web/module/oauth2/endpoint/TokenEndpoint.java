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
package net.eulerframework.web.module.oauth2.endpoint;

import java.util.Collection;
import javax.annotation.Resource;

import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpoint;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author cFrost
 *
 */
@FrameworkEndpoint
public class TokenEndpoint {
    
    @Resource 
    private TokenStore tokenStore;
    
    @GetMapping(value = "oauth/api/token/{username}")
    @ResponseBody
    public Collection<OAuth2AccessToken> userInfo(@PathVariable String username) {
        return this.tokenStore.findTokensByClientIdAndUserName("default", username);
    }
    
    @DeleteMapping(value = "oauth/api/token/{access_token}")
    @ResponseBody
    public void delToken(@PathVariable("access_token") String accessToken) {
        OAuth2AccessToken oauth2AccessToken = this.tokenStore.readAccessToken(accessToken);
        this.tokenStore.removeAccessToken(oauth2AccessToken);
    }
}
