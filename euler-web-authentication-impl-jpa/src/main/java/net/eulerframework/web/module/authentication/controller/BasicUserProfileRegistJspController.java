package net.eulerframework.web.module.authentication.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import net.eulerframework.web.core.annotation.JspController;
import net.eulerframework.web.core.base.controller.JspSupportWebController;
import net.eulerframework.web.module.authentication.conf.SecurityConfig;
import net.eulerframework.web.module.authentication.entity.BasicUserProfile;
import net.eulerframework.web.module.authentication.service.UserRegistService;
import net.eulerframework.web.module.authentication.util.Captcha;

/**
 * @author cFrost
 *
 */
@JspController
@RequestMapping("/")
public class BasicUserProfileRegistJspController extends JspSupportWebController {

    @Resource
    private UserRegistService userRegistService;
    
    public BasicUserProfileRegistJspController() {
        this.setWebControllerName("authentication");
    }

    @RequestMapping(value = "signup-basic", method = RequestMethod.POST)
    public String basicSignup(
            @RequestParam String username, 
            @RequestParam(required = false) String email, 
            @RequestParam(required = false) String mobile, 
            @RequestParam String password,
            BasicUserProfile basicUserProfile) {
        if(SecurityConfig.isSignUpEnabled()) {
            if(SecurityConfig.isSignUpEnableCaptcha()) {
                Captcha.validCaptcha(this.getRequest());
            }
            
            this.userRegistService.signUp(username, email, mobile, password, basicUserProfile);
            return this.success();
        } else {
            return this.notfound();
        }
    }

}
