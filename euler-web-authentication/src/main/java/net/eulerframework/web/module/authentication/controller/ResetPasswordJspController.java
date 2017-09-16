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
 * https://github.com/euler-form/web-form
 * https://cfrost.net
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
import net.eulerframework.web.module.authentication.exception.InvalidSMSResetCodeException;
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
        } else if (ResetPasswordType.SMS.equals(type)) {
            if (StringUtils.isNull(pin))
                return this.display("reset-password-sms-collector");

            try {
                this.passwordService.analyzeUserIdFromSMSResetPin(pin);
                this.getRequest().setAttribute("pin", pin);
                this.getRequest().setAttribute("type", ResetPasswordType.SMS);
                return this.display("reset-password-new-password");
            } catch (InvalidSMSResetCodeException e) {
                this.logger.debug("resetPassword error", e);
                return this.notfound();
            }
        } else {
            return this.display("reset-password");
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
                this.passwordService.resetPasswordBySMSResetPin(pin, password);
                return this.success();
            } else {
                return this.notfound();
            }
        } catch (InvalidEmailResetTokenException | UserNotFoundException | InvalidSMSResetCodeException | UserInfoCheckWebException e) {
            this.logger.debug("resetPassword error", e);
            return this.notfound();
        }

    }
}
