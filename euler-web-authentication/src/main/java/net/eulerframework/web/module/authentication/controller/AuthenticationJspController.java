/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2013-2017 cFrost.sun(孙宾, SUN BIN) 
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
 * https://github.com/euler-projects/euler-framework
 * https://cfrost.net
 */
package net.eulerframework.web.module.authentication.controller;

import javax.annotation.Resource;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import net.eulerframework.web.core.annotation.JspController;
import net.eulerframework.web.core.base.controller.JspSupportWebController;
import net.eulerframework.web.module.authentication.conf.SecurityConfig;
import net.eulerframework.web.module.authentication.service.UserRegistService;
import net.eulerframework.web.module.authentication.util.Captcha;

/**
 * @author cFrost
 *
 */
@JspController
@RequestMapping("/")
public class AuthenticationJspController extends JspSupportWebController {
    @RequestMapping(value = "signin", method = RequestMethod.GET)
    public String login() {
        return this.display("signin");
    }

    @Resource
    private UserRegistService userRegistService;

    @RequestMapping(value = "signup", method = RequestMethod.GET)
    public String signup(@RequestParam(name = "t", required = false) String userProfileType) {
        if(SecurityConfig.isSignUpEnabled()) {
            if(StringUtils.hasText(userProfileType)) {
                return this.display("signup-" + userProfileType);
            }
            return this.display("signup");
        } else {
            return this.notfound();
        }
    }

    @RequestMapping(value = "signup", method = RequestMethod.POST)
    public String litesignup(
            @RequestParam String username, 
            @RequestParam(required = false) String email, 
            @RequestParam(required = false) String mobile, 
            @RequestParam String password) {
        if(SecurityConfig.isSignUpEnabled()) {
            Captcha.validCaptcha(this.getRequest());
            this.userRegistService.signUp(username, email, mobile, password);
            return this.success();
        } else {
            return this.notfound();
        }
    }
}
