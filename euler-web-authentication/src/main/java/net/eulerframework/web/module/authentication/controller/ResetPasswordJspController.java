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
package net.eulerframework.web.module.authentication.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import net.eulerframework.common.util.StringUtils;
import net.eulerframework.web.core.annotation.JspController;
import net.eulerframework.web.core.base.controller.JspSupportWebController;
import net.eulerframework.web.module.authentication.enums.ResetPasswordType;
import net.eulerframework.web.module.authentication.exception.InvalidEmailResetTokenException;
import net.eulerframework.web.module.authentication.exception.InvalidSmsResetPinException;
import net.eulerframework.web.module.authentication.exception.UserInfoCheckWebException;
import net.eulerframework.web.module.authentication.exception.UserNotFoundException;
import net.eulerframework.web.module.authentication.service.PasswordService;

/**
 * @author cFrost
 *
 */
@JspController
@RequestMapping("/")
public class ResetPasswordJspController extends JspSupportWebController {
    @Resource
    private PasswordService passwordService;
    
    @RequestMapping(value = "reset-password", method = RequestMethod.GET)
    public String resetPassword(
            @RequestParam(required = false) ResetPasswordType type,
            @RequestParam(required = false) String token,
            @RequestParam(required = false) String pin) {
        if (ResetPasswordType.EMAIL.equals(type)) {
            return this.resetPasswordByEmail(token);
        } else if (ResetPasswordType.SMS.equals(type)) {
            return this.resetPasswordBySms(pin);
        } else {
            return this.display("reset-password");
        }
    }
    
    private String resetPasswordByEmail(String token) {
        if (StringUtils.isNull(token))
            return this.display("reset-password-email-collector");

        try {
            this.passwordService.analyzeUserIdFromEmailResetToken(token);
            this.getRequest().setAttribute("token", token);
            this.getRequest().setAttribute("type", ResetPasswordType.EMAIL);
            return this.display("reset-password-new-password");
        } catch (InvalidEmailResetTokenException e) {
            this.logger.debug("resetPassword error", e);
            return this.notfound();
        }        
    }
    
    private String resetPasswordBySms(String pin) {
        if (StringUtils.isNull(pin))
            return this.display("reset-password-sms-collector");

        try {
            this.passwordService.analyzeUserIdFromSmsResetPin(pin);
            this.getRequest().setAttribute("pin", pin);
            this.getRequest().setAttribute("type", ResetPasswordType.SMS);
            return this.display("reset-password-new-password");
        } catch (InvalidSmsResetPinException e) {
            this.logger.debug("resetPassword error", e);
            return this.notfound();
        }        
    }

    @RequestMapping(value = "reset-password-email", method = RequestMethod.POST)
    public String getPasswordResetEmail(@RequestParam String email) {
        this.passwordService.passwdResetEmailGen(email);
        return this.display("reset-password-email-sent");
    }

    @RequestMapping(value = "reset-password", method = RequestMethod.POST)
    public String resetPassword(
            @RequestParam(required = true) ResetPasswordType type, 
            @RequestParam(required = false) String token, 
            @RequestParam(required = false) String pin, 
            @RequestParam(required = true) String password) {

        try {
            if (ResetPasswordType.EMAIL.equals(type)) {
                this.passwordService.resetPasswordByEmailResetToken(token, password);
                return this.success();
            } else if (ResetPasswordType.SMS.equals(type)) {
                this.passwordService.resetPasswordBySmsResetPin(pin, password);
                return this.success();
            } else {
                return this.notfound();
            }
        } catch (InvalidEmailResetTokenException | UserNotFoundException | InvalidSmsResetPinException e) {
            this.logger.debug("resetPassword error", e);
            return this.notfound();
        } catch (UserInfoCheckWebException e) {
            this.logger.debug("resetPassword error", e);
            throw e;
        }

    }
}
