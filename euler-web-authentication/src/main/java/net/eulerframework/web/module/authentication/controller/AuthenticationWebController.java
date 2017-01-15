/**
 * 
 */
package net.eulerframework.web.module.authentication.controller;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import net.eulerframework.common.util.StringTool;
import net.eulerframework.web.config.ProjectMode;
import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.annotation.WebController;
import net.eulerframework.web.core.base.controller.AbstractWebController;
import net.eulerframework.web.core.base.response.AjaxResponse;
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
@Scope("prototype")
@RequestMapping("/")
public class AuthenticationWebController extends AbstractWebController {

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

    @RequestMapping(value = "changePasswd", method = RequestMethod.GET)
    public String changePasswd() {
        return this.display("changePasswd");
    }

    @RequestMapping(value = "litesignup", method = RequestMethod.POST)
    public String litesignup(@Valid User user) {
        this.authenticationService.signUp(user);

        this.getRequest().setAttribute("user", user);
        return this.display(WebConfig.getSignUpSuccessPage());
    }

    @RequestMapping(value = "changePasswd", method = RequestMethod.POST)
    public String changePasswd(String oldPassword, String newPassword) {

        this.authenticationService.changePassword(oldPassword, newPassword);
        return this.success();
    }

    @RequestMapping(value = "resetPassword", method = RequestMethod.GET)
    public String resetPasswordPage(@RequestParam String type, @RequestParam(required = false) String token) {
        if ("email".equalsIgnoreCase(type)) {
            if (StringTool.isNull(token))
                return this.display("getPasswordResetEmail");

            try {
                this.authenticationService.checkEmailResetToken(token);
                // this.getRequest().setAttribute("token", token);
                // this.getRequest().setAttribute("type", "email");
                return this.display("resetPassword");
            } catch (InvalidEmailResetTokenException e) {

                if (WebConfig.getProjectMode().equals(ProjectMode.DEVELOP)
                        || WebConfig.getProjectMode().equals(ProjectMode.DEBUG)) {
                    this.logger.error("resetPassword error", e);
                } else {
                    // DO_NOTHING
                }
                return this.notfound();

            }
        } else if ("sms".equalsIgnoreCase(type)) {
            return this.display("getPasswordResetSMS");
        } else {
            return this.notfound();
        }

    }

    @RequestMapping(value = "resetPasswordEmail", method = RequestMethod.POST)
    public String resetPasswordEmail(@RequestParam String email) {
        this.authenticationService.passwdResetEmailGen(email);
        return this.success("RESET_LNIK_WILL_BE_SENT_IF_YOU_INPUT_A_RIGHT_EMAIL");
    }

    @ResponseBody
    @RequestMapping(value = "resetPasswordSMS", method = RequestMethod.POST)
    public AjaxResponse<String> resetPasswordSMS(@RequestParam String mobile) {
        this.authenticationService.passwdResetSMSGen(mobile);
        return new AjaxResponse<>();
    }

    @RequestMapping(value = "resetPassword", method = RequestMethod.POST)
    public String resetPassword(@RequestParam String type, @RequestParam String token, @RequestParam String password) {

        try {
            if ("email".equalsIgnoreCase(type)) {
                this.authenticationService.resetPasswordByEmailResetToken(token, password);
                return this.success("PASSWORD_HAS_CHANGE");
            } else if ("sms".equalsIgnoreCase(type)) {
                this.authenticationService.resetPasswordBySMSResetCode(token, password);
                return this.success("PASSWORD_HAS_CHANGE");
            } else {
                return this.notfound();
            }
        } catch (InvalidEmailResetTokenException | UserNotFoundException | InvalidSMSResetCodeException e) {

            if (WebConfig.getProjectMode().equals(ProjectMode.DEVELOP)
                    || WebConfig.getProjectMode().equals(ProjectMode.DEBUG)) {
                this.logger.error("resetPassword error", e);
            } else {
                // DO_NOTHING
            }
            return this.notfound();
        }

    }

}
