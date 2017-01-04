package net.eulerframework.web.module.authentication.controller;

import javax.annotation.Resource;
import net.eulerframework.web.core.annotation.WebController;
import net.eulerframework.web.core.base.controller.AbstractWebController;
import net.eulerframework.web.core.exception.ResourceNotFoundException;

import org.springframework.context.annotation.Scope;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import net.eulerframework.web.module.authentication.service.IUserService;

@WebController
@Scope("prototype")
@RequestMapping("/authentication")
@Deprecated
public class UserWebContorller extends AbstractWebController {

	@Resource
	private IUserService userService;

    
    
    @RequestMapping(value = { "/forgotpasswd" }, method = RequestMethod.GET)
    public String forgotpasswd()
    {
        return "/authentication/forgotpasswd";
    }
    
    @RequestMapping(value = { "/resetPasswd/{userId}/{resetToken}" }, method = RequestMethod.GET)
    public String resetPasswdPage(
            @PathVariable("userId") String userId, 
            @PathVariable("resetToken") String resetToken,
            Model model)
    {
        try {
            this.userService.checkResetTokenRT(userId, resetToken);
        } catch (UsernameNotFoundException e) {
            throw new ResourceNotFoundException();
        }
        model.addAttribute("userId", userId);
        model.addAttribute("resetToken", resetToken);
        return "/authentication/resetPasswd";
    }

    @RequestMapping(value = { "/resetPasswd" }, method = RequestMethod.POST)
    public String resetPasswd(
            String userId, 
            String resetToken,
            String pwd)
    {
        this.userService.resetUserPasswordWithResetTokenRWT(userId, pwd, resetToken);
        return "/authentication/login";
    }
    
    @RequestMapping(value = { "/applyResetPasswd" }, method = RequestMethod.POST)
    public String applyResetPasswd(String email)
    {
        this.userService.forgotPasswordRWT(email);
        return "/authentication/signin";
    }
}
