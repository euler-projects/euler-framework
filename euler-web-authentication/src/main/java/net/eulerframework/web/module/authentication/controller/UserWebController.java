/**
 * 
 */
package net.eulerframework.web.module.authentication.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import net.eulerframework.common.util.StringUtils;
import net.eulerframework.web.core.annotation.WebController;
import net.eulerframework.web.core.base.controller.JspSupportWebController;
import net.eulerframework.web.module.authentication.enums.ResetPasswordType;
import net.eulerframework.web.module.authentication.exception.InvalidEmailResetTokenException;
import net.eulerframework.web.module.authentication.exception.InvalidSMSResetCodeException;
import net.eulerframework.web.module.authentication.exception.UserInfoCheckWebException;
import net.eulerframework.web.module.authentication.exception.UserNotFoundException;
import net.eulerframework.web.module.authentication.service.PasswordService;
import net.eulerframework.web.module.authentication.service.UserRegistService;

/**
 * @author cFrost
 *
 */
@WebController
@RequestMapping("/")
public class UserWebController extends JspSupportWebController {

    @Resource
    private UserRegistService userRegistService;
    @Resource
    private PasswordService passwordService;

    @RequestMapping(value = "signin", method = RequestMethod.GET)
    public String login() {
        return this.display("signin");
    }

    @RequestMapping(value = "signup", method = RequestMethod.GET)
    public String signup() {
        return this.display("signup");
    }

    @RequestMapping(value = "signup", method = RequestMethod.POST)
    public String litesignup(@RequestParam String username, @RequestParam String password) {
        this.userRegistService.signUp(username, password);
        return this.success();
    }
    
    @RequestMapping(value = "reset-password", method = RequestMethod.GET)
    public String resetPassword(
            @RequestParam(required = true) ResetPasswordType type,
            @RequestParam(required = false) String token) {
        if (ResetPasswordType.EMAIL.equals(type)) {
            if (StringUtils.isNull(token))
                return this.display("reset-password-email-collector");

            try {
                this.passwordService.analyzeUserIdFromEmailResetToken(token);
                this.getRequest().setAttribute("token", token);
                this.getRequest().setAttribute("type", "email");
                return this.display("reset-password-new-password");
            } catch (InvalidEmailResetTokenException e) {
                this.logger.debug("resetPassword error", e);
                return this.notfound();
            }
        } else if (ResetPasswordType.SMS.equals(type)) {
            return this.display("reset-password-sms-collector");
        } else {
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
            @RequestParam(required = true) String token, 
            @RequestParam(required = true) String password) {

        try {
            if (ResetPasswordType.EMAIL.equals(type)) {
                this.passwordService.resetPasswordByEmailResetToken(token, password);
                return this.success();
            } else if (ResetPasswordType.SMS.equals(type)) {
                this.passwordService.resetPasswordBySMSResetCode(token, password);
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
