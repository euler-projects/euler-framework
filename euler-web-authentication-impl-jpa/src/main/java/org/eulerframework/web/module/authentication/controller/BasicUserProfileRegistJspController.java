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
package org.eulerframework.web.module.authentication.controller;

import jakarta.annotation.Resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import org.eulerframework.web.core.annotation.JspController;
import org.eulerframework.web.core.base.controller.JspSupportWebController;
import org.eulerframework.web.module.authentication.conf.SecurityConfig;
import org.eulerframework.web.module.authentication.entity.BasicUserProfile;
import org.eulerframework.web.module.authentication.service.UserRegistService;
import org.eulerframework.web.module.authentication.util.Captcha;

/**
 * @author cFrost
 *
 */
@JspController
@RequestMapping("/")
public class BasicUserProfileRegistJspController extends JspSupportWebController {

    @Resource
    private UserRegistService userRegistService;
    
    public BasicUserProfileRegistJspController() {
        this.setWebControllerName("authentication");
    }

    @RequestMapping(value = "signup-basic", method = RequestMethod.POST)
    public String basicSignup(
            @RequestParam String username, 
            @RequestParam(required = false) String email, 
            @RequestParam(required = false) String phone, 
            @RequestParam String password,
            BasicUserProfile basicUserProfile) {
        if(SecurityConfig.isSignUpEnabled()) {
            if(SecurityConfig.isSignUpEnableCaptcha()) {
                Captcha.validCaptcha(this.getRequest());
            }
            
            this.userRegistService.signUp(username, email, phone, password, basicUserProfile);
            return this.success();
        } else {
            return this.notfound();
        }
    }

}
