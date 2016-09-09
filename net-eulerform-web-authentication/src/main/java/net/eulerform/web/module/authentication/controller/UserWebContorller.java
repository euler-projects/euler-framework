package net.eulerform.web.module.authentication.controller;

import javax.annotation.Resource;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import net.eulerform.web.core.annotation.WebController;
import net.eulerform.web.core.base.controller.DefaultWebController;
import net.eulerform.web.core.base.exception.NotFoundException;
import net.eulerform.web.module.authentication.service.IUserService;

@WebController
@Scope("prototype")
@RequestMapping("/authentication")
public class UserWebContorller extends DefaultWebController {

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
    
    @ResponseBody
    @RequestMapping(value = { "/signUp" }, method = RequestMethod.POST)
    public void signUp(String username, String password) {
        this.userService.createUser(username, password);
    }
}
