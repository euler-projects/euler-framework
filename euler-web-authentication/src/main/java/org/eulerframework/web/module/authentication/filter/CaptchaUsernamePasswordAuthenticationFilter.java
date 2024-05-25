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
package org.eulerframework.web.module.authentication.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;

import org.eulerframework.constant.EulerSysAttributes;
import org.eulerframework.web.module.authentication.util.Captcha;

public class CaptchaUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    
    public static final String EULER_SECURITY_FORM_SMS_CODE_KEY = "smsCode";

    private String smsCodeParameter = EULER_SECURITY_FORM_SMS_CODE_KEY;
    
    private boolean enableCaptcha = true;
    
    public void setEnableCaptcha(boolean enableCaptcha) {
        this.enableCaptcha = enableCaptcha;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
            HttpServletResponse response) throws AuthenticationException {
        String realCaptcha= "";
        String userCaptcha="";
        
        if(enableCaptcha) {
            realCaptcha = Captcha.getRealCaptcha(request);
            userCaptcha = request.getParameter("captcha");            
        }
        
        if(!enableCaptcha || (StringUtils.hasText(realCaptcha) && realCaptcha.equalsIgnoreCase(userCaptcha))) {
            Authentication result = super.attemptAuthentication(request, response);
            request.getSession().setAttribute(EulerSysAttributes.USER_INFO.value(), result.getPrincipal());
            return result;
        } else {
            throw new InternalAuthenticationServiceException("captcha_error");
        }
    }

    @Override
    protected String obtainPassword(HttpServletRequest request) {
        String password = super.obtainPassword(request);
        
        if(password == null) {
            password = request.getParameter(smsCodeParameter);
        }
        
        return password;
    }

}
