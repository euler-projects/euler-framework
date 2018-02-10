/**
 * 
 */
package net.eulerframework.web.module.oldauthentication.controller;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import net.eulerframework.common.util.StringUtils;
import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.annotation.JspController;
import net.eulerframework.web.core.base.controller.JspSupportWebController;
import net.eulerframework.web.module.oldauthentication.entity.User;
import net.eulerframework.web.module.oldauthentication.exception.InvalidEmailResetTokenException;
import net.eulerframework.web.module.oldauthentication.exception.InvalidSMSResetCodeException;
import net.eulerframework.web.module.oldauthentication.exception.UserNotFoundException;
import net.eulerframework.web.module.oldauthentication.service.IAuthenticationService;

/**
 * @author cFrost
 *
 */
@JspController
@RequestMapping("/")
public class UserJspController extends JspSupportWebController {

    @Resource
    private IAuthenticationService authenticationService;

    @RequestMapping(value = "signin", method = RequestMethod.GET)
    public String login() {
        return this.display("signin");
    }

    @RequestMapping(value = "signup", method = RequestMethod.GET)
    public String signup() {
        return this.display("signup");
    }

    @RequestMapping(value = "signup", method = RequestMethod.POST)
    public String litesignup(@Valid User user) {
        this.authenticationService.signUp(user);

        return this.success();
    }

    @RequestMapping(value = "change-password", method = RequestMethod.GET)
    public String changePassword() {
        return this.display("changePassword");
    }

    @RequestMapping(value = "change-password", method = RequestMethod.POST)
    public String changePassword(
            @RequestParam(required = true) String oldPassword, 
            @RequestParam(required = true) String newPassword) {
        this.authenticationService.changePassword(oldPassword, newPassword);
        return this.success();
    }

    @RequestMapping(value = "reset-password", method = RequestMethod.GET)
    public String resetPassword(
            @RequestParam(required = true) String type, 
            @RequestParam(required = false) String token) {
        if ("email".equalsIgnoreCase(type)) {
            if (StringUtils.isNull(token))
                return this.display("reset-password-email-collector");

            try {
                this.authenticationService.checkEmailResetToken(token);
                this.getRequest().setAttribute("token", token);
                this.getRequest().setAttribute("type", "email");
                return this.display("reset-password-new-password");
            } catch (InvalidEmailResetTokenException e) {

                if (WebConfig.isDebugMode()) {
                    this.logger.error("resetPassword error", e);
                } else {
                    // DO_NOTHING
                }
                return this.notfound();

            }
        } else if ("sms".equalsIgnoreCase(type)) {
            return this.display("reset-password-sms-collector");
        } else {
            return this.notfound();
        }

    }

    @RequestMapping(value = "reset-password", method = RequestMethod.POST)
    public String resetPassword(
            @RequestParam(required = true) String type, 
            @RequestParam(required = true) String token, 
            @RequestParam(required = true) String password) {

        try {
            if ("email".equalsIgnoreCase(type)) {
                this.authenticationService.resetPasswordByEmailResetToken(token, password);
                return this.success();
            } else if ("sms".equalsIgnoreCase(type)) {
                this.authenticationService.resetPasswordBySMSResetCode(token, password);
                return this.success();
            } else {
                return this.notfound();
            }
        } catch (InvalidEmailResetTokenException | UserNotFoundException | InvalidSMSResetCodeException e) {

            if (WebConfig.isDebugMode()) {
                this.logger.error("resetPassword error", e);
            } else {
                // DO_NOTHING
            }
            return this.notfound();
        }

    }

    @RequestMapping(value = "reset-password-email", method = RequestMethod.POST)
    public String getPasswordResetEmail(@RequestParam String email) {
        this.authenticationService.passwdResetEmailGen(email);
        return this.display("reset-password-email-sent");
    }
}
