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
package org.eulerframework.web.module.authentication.controller.ajax;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import org.eulerframework.common.util.StringUtils;
import org.eulerframework.web.core.annotation.AjaxController;
import org.eulerframework.web.core.annotation.ApiEndpoint;
import org.eulerframework.web.core.base.controller.ApiSupportWebController;
import org.eulerframework.web.core.exception.web.PageNotFoundException;
import org.eulerframework.web.core.exception.web.WebException;
import org.eulerframework.web.module.authentication.conf.SecurityConfig;
import org.eulerframework.web.module.authentication.exception.NotSupportRobotCheckRequestException;
import org.eulerframework.web.module.authentication.exception.RobotRequestException;
import org.eulerframework.web.module.authentication.service.RobotCheckService;
import org.eulerframework.web.module.authentication.service.SmsCodeValidator;
import org.eulerframework.web.module.authentication.service.SmsCodeValidator.BizCode;
import org.eulerframework.web.module.authentication.service.UserRegistService;
import org.eulerframework.web.module.authentication.util.UserDataValidator;

/**
 * 用于验证用户信息是否符合要求
 * 
 * @author cFrost
 *
 */
@ApiEndpoint
@AjaxController
@RequestMapping("/")
public class SignUpAjaxController extends ApiSupportWebController {

    @Resource
    private UserRegistService userRegistService;
    @Autowired(required = false)
    private List<RobotCheckService> robotCheckServices;
    @Autowired
    private SmsCodeValidator smsCodeValidator;

    @RequestMapping(path = "validUsername", method = RequestMethod.GET)
    public void validUsername(@RequestParam String username) {
        if (SecurityConfig.isSignUpEnabled()) {
            UserDataValidator.validUsername(username);
        } else {
            throw new PageNotFoundException();
        }
    }

    @RequestMapping(path = "validEmail", method = RequestMethod.GET)
    public void validEmail(@RequestParam String email) {
        if (SecurityConfig.isSignUpEnabled()) {
            UserDataValidator.validEmail(email);
        } else {
            throw new PageNotFoundException();
        }
    }

    @RequestMapping(path = "validMobile", method = RequestMethod.GET)
    public void validMobile(@RequestParam String mobile) {
        if (SecurityConfig.isSignUpEnabled()) {
            UserDataValidator.validMobile(mobile);
        } else {
            throw new PageNotFoundException();
        }
    }

    @RequestMapping(path = "validPassword", method = RequestMethod.GET)
    public void validPassword(@RequestParam String password) {
        if (SecurityConfig.isSignUpEnabled()) {
            UserDataValidator.validPassword(password);
        } else {
            throw new PageNotFoundException();
        }
    }

    @RequestMapping(path = "robotCheck", method = RequestMethod.GET)
    public void robotCheck() {
        if (SecurityConfig.isSignUpEnabled()) {
            this.isRobotRequest(this.getRequest());
        } else {
            throw new PageNotFoundException();
        }
    }
    
    @RequestMapping(value = "sendSmsCode", method = RequestMethod.POST) 
    public void sendSmsCode(@RequestParam String mobile, @RequestParam BizCode bizCode) {
        this.smsCodeValidator.sendSmsCode(mobile, bizCode);
    }
    
    @RequestMapping(path = "validSmsCode", method = RequestMethod.GET)
    public void validSmsCode(@RequestParam String mobile, @RequestParam String smsCode, @RequestParam BizCode bizCode) {
        if(this.smsCodeValidator == null) {
            throw new WebException("sms code was disabled");
        }
        this.smsCodeValidator.check(mobile, smsCode, bizCode);
    }

    @RequestMapping(
            value = "signup", 
            method = RequestMethod.POST, 
            consumes = {
                    MediaType.APPLICATION_FORM_URLENCODED_VALUE, 
                    MediaType.MULTIPART_FORM_DATA_VALUE })
    public String litesignup(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email, 
            @RequestParam(required = false) String mobile,
            @RequestParam(required = false) String smsCode,
            @RequestParam(required = false) String password, 
            @RequestParam Map<String, Object> extraData) {
        if (SecurityConfig.isSignUpEnabled()) {
            this.isRobotRequest(this.getRequest());
            
            if(this.smsCodeValidator.isEnabled()) {
                this.smsCodeValidator.check(mobile, smsCode, BizCode.SIGN_UP);
                if(StringUtils.isEmpty(password)) {
                    /*
                     * TODO: 短信验证码注册采用随机密码的方式不完美, 
                     * 因为这样一来使用验证码注册的用户想设置密码只能通过重置密码功能进行, 
                     * 体验不完美, 应该采用可以识别出密码没有被设置的方案,
                     * 但这样子又要考虑如何识别空密码的问题, 需要好好设计, 避免产生漏洞
                     */
                    this.logger.info("Sms Code is enabled, use random password when parameter 'password' is not presen");
                    password = StringUtils.randomString(16);
                }
            }
                
            Assert.hasText(password, "Required String parameter 'password' is not present");

            if (extraData != null) {
                extraData.remove("username");
                extraData.remove("email");
                extraData.remove("mobile");
                extraData.remove("password");
            }

            if (extraData == null || extraData.isEmpty()) {
                return this.userRegistService.signUp(username, email, mobile, password).getUserId();
            } else {
                return this.userRegistService.signUp(username, email, mobile, password, extraData).getUserId();
            }
        } else {
            throw new PageNotFoundException();
        }
    }

    @RequestMapping(
            value = "signup", 
            method = RequestMethod.POST, 
            consumes = { 
                    MediaType.APPLICATION_JSON_VALUE,
                    MediaType.APPLICATION_JSON_UTF8_VALUE })
    public String signupJson(@RequestBody Map<String, Object> data) {
        String username = (String) data.get("username");
        String email = (String) data.get("email");
        String mobile = (String) data.get("mobile");
        String smsCode = (String) data.get("smsCode");
        String password = (String) data.get("password");

        data.remove("username");
        data.remove("email");
        data.remove("mobile");
        data.remove("smsCode");
        data.remove("password");

        return this.litesignup(username, email, mobile, smsCode, password, data);
    }

    /**
     * 检测是否是机器人请求
     * 
     * 可存在多个机器人检测实现类，检测策略为只要有一个判定不是机器人即检测通过，如果没有实现类则关闭机器人检测功能
     * 
     * @param request
     *            请求对象
     */
    private void isRobotRequest(HttpServletRequest request) {
        if (this.robotCheckServices != null) {
            for (RobotCheckService robotCheckService : this.robotCheckServices) {
                try {
                    if (!robotCheckService.isRobot(request)) {
                        return;
                    }
                } catch (NotSupportRobotCheckRequestException e) {
                    // DO_NOTHING
                }
            }

            throw new RobotRequestException();
        }
    }
}
