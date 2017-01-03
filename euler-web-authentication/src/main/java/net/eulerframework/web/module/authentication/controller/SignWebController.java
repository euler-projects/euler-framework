/**
 * 
 */
package net.eulerframework.web.module.authentication.controller;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import net.eulerframework.web.core.annotation.WebController;
import net.eulerframework.web.core.base.controller.AbstractWebController;
import net.eulerframework.web.module.authentication.entity.User;
import net.eulerframework.web.module.authentication.service.IRootService;
import net.eulerframework.web.module.authentication.service.IUserService;
import net.eulerframework.web.module.authentication.util.UserContext;

/**
 * @author cFrost
 *
 */
@WebController
@Scope("prototype")
@RequestMapping("/authentication")
public class SignWebController extends AbstractWebController {

    @Resource
    private IUserService userService;
    @Resource
    private IRootService rootService;
    
    @RequestMapping(value = { "/signin" }, method = RequestMethod.GET)
    public String login()
    {
        return "/authentication/signin";
    }
    
    @RequestMapping(value = { "/signup" }, method = RequestMethod.GET)
    public String signup()
    {
        return "/default/authentication/signup";
    }
    
    @ResponseBody
    @RequestMapping(value = { "/signup" }, method = RequestMethod.POST)
    public void signup(@Valid User user) {
        this.userService.createUser(user);
    }
    
    @RequestMapping(value = { "/passwd-root" }, method = RequestMethod.POST)
    public void resetRootPwd() {
        UserContext.sudo();
        this.rootService.resetRootPasswordRWT();
    }
    
    @RequestMapping(value = { "/passwd-admin" }, method = RequestMethod.POST)
    public void resetAdminPwd() {
        UserContext.sudo();
        this.rootService.resetAdminPasswordRWT();
    }

}
