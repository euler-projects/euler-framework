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

import net.eulerframework.web.config.ProjectMode;
import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.annotation.WebController;
import net.eulerframework.web.core.base.controller.AbstractWebController;
import net.eulerframework.web.module.authentication.entity.User;
import net.eulerframework.web.module.authentication.exception.InvalidEmailResetTokenException;
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
    
    @RequestMapping(value = "resetPasswordByEmail", method = RequestMethod.POST)
    public String resetPasswordByEmail(@RequestParam String email) {        
        this.authenticationService.passwdResetEmailGen(email);        
        return this.success("RESET_LNIK_WILL_BE_SENT_IF_YOU_INPUT_A_RIGHT_EMAIL");
    }
    
    @RequestMapping(value = "resetPassword", method = RequestMethod.GET)
    public String resetPasswordPage(
            @RequestParam String type,
            @RequestParam String token) {
        if("email".equalsIgnoreCase(type)) {
            try {
                this.authenticationService.checkEmailResetToken(token);
            } catch (InvalidEmailResetTokenException e) {

                if(WebConfig.getProjectMode().equals(ProjectMode.DEVELOP) ||
                        WebConfig.getProjectMode().equals(ProjectMode.DEBUG)) {
                    this.logger.error("resetPassword error" ,e);
                } else {
                    //DO_NOTHING            
                }
                
                return this.notfound();
            }
        }
        
        return this.success("RESET_LNIK_WILL_BE_SENT_IF_YOU_INPUT_A_RIGHT_EMAIL");
    }
    
    @RequestMapping(value = "resetPassword", method = RequestMethod.POST)
    public String resetPassword(
            @RequestParam String type,
            @RequestParam String token,
            @RequestParam String password) {
        if("email".equalsIgnoreCase(type)) {
            try {
                this.authenticationService.resetPasswordByEmailResetToken(token, password);
            } catch (InvalidEmailResetTokenException | UserNotFoundException e) {

                if(WebConfig.getProjectMode().equals(ProjectMode.DEVELOP) ||
                        WebConfig.getProjectMode().equals(ProjectMode.DEBUG)) {
                    this.logger.error("resetPassword error" ,e);
                } else {
                    //DO_NOTHING            
                }
                
                return this.notfound();
            }
        }
        
        return this.success("PASSWORD_HAS_CHANGE");
    }

}
