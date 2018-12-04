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
package org.eulerframework.oauth2.resource.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 用户OAuth Resource Server把用户的权限信息替换成本地的内容
 * @author cFrost
 *
 */
public class Oauth2LocalAuthorityFilter extends OncePerRequestFilter {
    
    private UserDetailsService userDetailsService;
    
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        SecurityContext context = SecurityContextHolder.getContext();
        
        OAuth2Authentication oauthAuthentication = (OAuth2Authentication)context.getAuthentication();
        
        if(oauthAuthentication!=null && oauthAuthentication.isAuthenticated() && oauthAuthentication.getUserAuthentication() != null) {
            
            UserDetails userDetails;
            try {
                userDetails = this.userDetailsService.loadUserByUsername((String) oauthAuthentication.getUserAuthentication().getPrincipal());
            } catch (UsernameNotFoundException e) {
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                response.getOutputStream().write("{\"error\":\"unauthorized\",\"error_description\":\"user not exists\"}".getBytes());
                return;
            }
            OAuth2Request storedRequest = oauthAuthentication.getOAuth2Request();
            UsernamePasswordAuthenticationToken userAuthentication = (UsernamePasswordAuthenticationToken) oauthAuthentication.getUserAuthentication();
    
            if(userAuthentication != null){            
                UsernamePasswordAuthenticationToken fixedUserAuthentication = new UsernamePasswordAuthenticationToken(
                        userAuthentication.getPrincipal(), userAuthentication.getCredentials(),
                        userDetails.getAuthorities());
                fixedUserAuthentication.setDetails(userAuthentication.getDetails());
                
                OAuth2Authentication fixedOauthAuthentication = new OAuth2Authentication(storedRequest, fixedUserAuthentication);
                context.setAuthentication(fixedOauthAuthentication); 
            }
        }
        filterChain.doFilter(request, response);
    }

}
