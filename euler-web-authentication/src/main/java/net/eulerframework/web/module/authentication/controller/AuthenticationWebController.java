/**
 * 
 */
package net.eulerframework.web.module.authentication.controller;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import net.eulerframework.common.util.StringUtils;
import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.annotation.WebController;
import net.eulerframework.web.core.base.controller.JspSupportWebController;
import net.eulerframework.web.module.authentication.entity.User;
import net.eulerframework.web.module.authentication.exception.InvalidEmailResetTokenException;
import net.eulerframework.web.module.authentication.exception.InvalidSMSResetCodeException;
import net.eulerframework.web.module.authentication.exception.UserNotFoundException;
import net.eulerframework.web.module.authentication.service.IAuthenticationService;

/**
 * @author cFrost
 *
 */
@WebController
@RequestMapping("/")
public class AuthenticationWebController extends JspSupportWebController {

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

    @RequestMapping(value = "changePassword", method = RequestMethod.GET)
    public String changePassword() {
        return this.display("changePassword");
    }

    @RequestMapping(value = "changePassword", method = RequestMethod.POST)
    public String changePassword(
            @RequestParam(required = true) String oldPassword, 
            @RequestParam(required = true) String newPassword) {
        this.authenticationService.changePassword(oldPassword, newPassword);
        return this.success();
    }

    @RequestMapping(value = "resetPassword", method = RequestMethod.GET)
    public String resetPassword(
            @RequestParam(required = true) String type, 
            @RequestParam(required = false) String token) {
        if ("email".equalsIgnoreCase(type)) {
            if (StringUtils.isEmpty(token))
                return this.display("forgotPassword-email");

            try {
                this.authenticationService.checkEmailResetToken(token);
                this.getRequest().setAttribute("token", token);
                this.getRequest().setAttribute("type", "email");
                return this.display("resetPassword");
            } catch (InvalidEmailResetTokenException e) {

                if (WebConfig.isDebugMode()) {
                    this.logger.error("resetPassword error", e);
                } else {
                    // DO_NOTHING
                }
                return this.notfound();

            }
        } else if ("sms".equalsIgnoreCase(type)) {
            return this.display("forgotPassword-sms");
        } else {
            return this.notfound();
        }

    }

    @RequestMapping(value = "resetPassword", method = RequestMethod.POST)
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

    @RequestMapping(value = "getPasswordResetEmail", method = RequestMethod.POST)
    public String getPasswordResetEmail(@RequestParam String email) {
        this.authenticationService.passwdResetEmailGen(email);
        return this.display("forgotPassword-email-sent");
    }
}
