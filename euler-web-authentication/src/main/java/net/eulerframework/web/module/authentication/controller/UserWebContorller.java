package net.eulerframework.web.module.authentication.controller;

import javax.annotation.Resource;
import javax.validation.Valid;

import net.eulerframework.web.core.annotation.WebController;
import net.eulerframework.web.core.base.controller.AbstractWebController;

import org.springframework.context.annotation.Scope;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import net.eulerframework.web.core.base.exception.NotFoundException;
import net.eulerframework.web.module.authentication.entity.User;
import net.eulerframework.web.module.authentication.service.IUserService;

@WebController
@Scope("prototype")
@RequestMapping("/authentication")
public class UserWebContorller extends AbstractWebController {

	@Resource
	private IUserService userService;

    @RequestMapping(value = { "/signin" }, method = RequestMethod.GET)
    public String login()
    {
        return "/authentication/signin";
    }
    
    @RequestMapping(value = { "/signup" }, method = RequestMethod.GET)
    public String signup()
    {
        return "/authentication/signup";
    }
    
    @ResponseBody
    @RequestMapping(value = { "/signup" }, method = RequestMethod.POST)
    public void signup(@Valid User user) {
        this.userService.createUser(user);
    }
    
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
            throw new NotFoundException();
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
