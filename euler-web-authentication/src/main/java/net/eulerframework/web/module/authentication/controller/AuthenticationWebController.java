/**
 * 
 */
package net.eulerframework.web.module.authentication.controller;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import net.eulerframework.common.util.StringTool;
import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.annotation.WebController;
import net.eulerframework.web.core.base.controller.AbstractWebController;
import net.eulerframework.web.module.authentication.entity.User;
import net.eulerframework.web.module.authentication.exception.UserChangePasswordException;
import net.eulerframework.web.module.authentication.exception.UserSignUpException;
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
        try {
            String userId = this.authenticationService.signUp(user);

            if (!StringTool.isNull(userId)) {
                user.setPassword(null);
                this.getRequest().setAttribute("user", user);
                return this.display(WebConfig.getSignUpSuccessPage());
            } else
                throw new UserSignUpException(UserSignUpException.INFO.UNKNOWN_USER_SIGNUP_ERROR.toString());
        } catch (UserSignUpException e) {
            this.getRequest().setAttribute("errorMsg", e.getLocalizedMessage());
            return this.display(WebConfig.getSignUpFailPage());
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
            this.getRequest().setAttribute("errorMsg", UserSignUpException.INFO.UNKNOWN_USER_SIGNUP_ERROR.toString());
            return this.display(WebConfig.getSignUpFailPage());
        }
    }
    

    @RequestMapping(value = "changePasswd", method = RequestMethod.POST)
    public String changePasswd(String oldPassword, String newPassword) throws UserChangePasswordException {
        try {
            this.authenticationService.changePassword(oldPassword, newPassword);
        } catch (UserChangePasswordException e) {
            return this.error(e.getLocalizedMessage());
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
            return this.error(UserChangePasswordException.INFO.UNKNOWN_CHANGE_PASSWD_ERROR.toString());
        }
        
        return this.success(null);
    }

}
