/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2013-2018 cFrost.sun(孙宾, SUN BIN) 
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
 * For more information, please visit the following website
 * 
 * https://eulerproject.io
 * https://cfrost.net
 */
package net.eulerframework.web.module.authentication.extend;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.web.filter.CorsFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.base.response.RedirectResponse;

/**
 * @author cFrost
 *
 */
public class EulerLoginUrlAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {

    private ObjectMapper objectMapper;
    private CorsFilter corsFilter;

    /**
     * @param loginFormUrl
     */
    public EulerLoginUrlAuthenticationEntryPoint(String loginFormUrl, ObjectMapper objectMapper, CorsFilter corsFilter) {
        super(loginFormUrl);
        this.objectMapper = objectMapper;
        this.corsFilter = corsFilter;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        
        if (request.getRequestURI().startsWith(request.getContextPath() + "/ajax") 
                || request.getRequestURI().startsWith(
                        request.getContextPath() + WebConfig.getAdminRootPath() + "/ajax"
                        )
                ){
            
            this.corsFilter.doFilter(request, response, new FilterChain() {

                @Override
                public void doFilter(ServletRequest request, ServletResponse response)
                        throws IOException, ServletException {
                    // TODO Auto-generated method stub
                    
                }
                
            });
            
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            
            response.getOutputStream().print( 
                    this.objectMapper.writeValueAsString(
                            new RedirectResponse(
                                    this.buildRedirectUrlToLoginPage(request, response, authException)
                                    )
                            )
                    );
        } else {
            super.commence(request, response, authException);
        }
    }
}
