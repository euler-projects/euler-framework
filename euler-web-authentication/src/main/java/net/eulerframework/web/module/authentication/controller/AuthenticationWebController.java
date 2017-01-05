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
import net.eulerframework.web.core.exception.BadRequestException;
import net.eulerframework.web.module.authentication.entity.User;
import net.eulerframework.web.module.authentication.service.IAuthenticationService;

/**
 * @author cFrost
 *
 */
@WebController
@Scope("prototype")
@RequestMapping("/")
public class AuthenticationWebController extends AbstractWebController {
    
    @Resource private IAuthenticationService authenticationService;
    
    @RequestMapping(value = { "signin" }, method = RequestMethod.GET)
    public String login()
    {
        return this.display("signin");
    }
    
    @RequestMapping(value = { "signup" }, method = RequestMethod.GET)
    public String signup()
    {
        return this.display("signup");
    }
    @RequestMapping(value = { "simpleSignup" }, method = RequestMethod.POST)
    public String simpleSignup( @Valid User user)
    {
        try {
        String userId = this.authenticationService.signUp(user);
        
        if(!StringTool.isNull(userId)) {
            user.setPassword(null);
            this.getRequest().setAttribute("user", user);
            return this.display(WebConfig.getSignUpSuccessPage());
        } else
            throw new BadRequestException("用户注册失败");
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
            return this.display(WebConfig.getSignUpFailPage());         
        }
    }

}
