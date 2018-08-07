package net.eulerframework.web.module.authentication.controller;

import javax.annotation.Resource;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import net.eulerframework.web.core.annotation.JspController;
import net.eulerframework.web.core.base.controller.JspSupportWebController;
import net.eulerframework.web.module.authentication.conf.SecurityConfig;
import net.eulerframework.web.module.authentication.service.UserRegistService;
import net.eulerframework.web.module.authentication.util.Captcha;

/**
 * @author cFrost
 *
 */
@JspController
@RequestMapping("/")
public class AuthenticationJspController extends JspSupportWebController {
    @RequestMapping(value = "signin", method = RequestMethod.GET)
    public String login() {
        return this.display("signin");
    }

    @Resource
    private UserRegistService userRegistService;

    @RequestMapping(value = "signup", method = RequestMethod.GET)
    public String signup(@RequestParam(name = "t", required = false) String userProfileType) {
        if(SecurityConfig.isSignUpEnabled()) {
            if(StringUtils.hasText(userProfileType)) {
                return this.display("signup-" + userProfileType);
            }
            return this.display("signup");
        } else {
            return this.notfound();
        }
    }

    @RequestMapping(value = "signup", method = RequestMethod.POST)
    public String litesignup(
            @RequestParam String username, 
            @RequestParam(required = false) String email, 
            @RequestParam(required = false) String mobile, 
            @RequestParam String password) {
        if(SecurityConfig.isSignUpEnabled()) {
            if(SecurityConfig.isSignUpEnableCaptcha()) {
                Captcha.validCaptcha(this.getRequest());
            }
            
            this.userRegistService.signUp(username, email, mobile, password);
            return this.success();
        } else {
            return this.notfound();
        }
    }
}
