/**
 * 
 */
package net.eulerframework.web.module.authentication.controller.ajax;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import net.eulerframework.common.util.CommonUtils;
import net.eulerframework.web.core.annotation.AjaxController;
import net.eulerframework.web.core.base.controller.AjaxSupportWebController;
import net.eulerframework.web.core.exception.web.PageNotFoundException;
import net.eulerframework.web.module.authentication.conf.SecurityConfig;
import net.eulerframework.web.module.authentication.service.UserRegistService;
import net.eulerframework.web.module.authentication.util.Captcha;
import net.eulerframework.web.module.authentication.util.UserDataValidator;

/**
 * 用于验证用户信息是否符合要求
 * @author cFrost
 *
 */
@AjaxController
@RequestMapping("/")
public class AuthenticationAjaxController extends AjaxSupportWebController {

    @Resource
    private UserRegistService userRegistService;

    @RequestMapping(path="validUsername", method = RequestMethod.GET)
    public void validUsername(@RequestParam String username) {
        UserDataValidator.validUsername(username);
    }

    @RequestMapping(path="validEmail", method = RequestMethod.GET)
    public void validEmail(@RequestParam String email) {
        UserDataValidator.validEmail(email);
    }

    @RequestMapping(path="validMobile", method = RequestMethod.GET)
    public void validMobile(@RequestParam String mobile) {
        UserDataValidator.validMobile(mobile);
    }

    @RequestMapping(path="validPassword", method = RequestMethod.GET)
    public void validPassword(@RequestParam String password) {
        UserDataValidator.validPassword(password);
    }

    @RequestMapping(path="validCaptcha", method = RequestMethod.GET)
    public void validCaptcha(@RequestParam String captcha) {
        CommonUtils.sleep(1);
        Captcha.validCaptcha(captcha, this.getRequest());
    }

    @RequestMapping(
            value = "signup", 
            method = RequestMethod.POST, 
            consumes = {
                    MediaType.APPLICATION_FORM_URLENCODED_VALUE, 
                    MediaType.MULTIPART_FORM_DATA_VALUE})
    public String litesignup(
            @RequestParam String username, 
            @RequestParam(required = false) String email, 
            @RequestParam(required = false) String mobile, 
            @RequestParam String password,
            @RequestParam Map<String, Object> extraData) {
        if(SecurityConfig.isSignUpEnabled()) {
            if(SecurityConfig.isSignUpEnableCaptcha()) {
                Captcha.validCaptcha(this.getRequest());
            }
            
            if(extraData != null) {
                extraData.remove("username");
                extraData.remove("email");
                extraData.remove("mobile");
                extraData.remove("password");
            }
            
            if(extraData == null || extraData.isEmpty()) {
                return this.userRegistService.signUp(username, email, mobile, password).getUserId();
            } else {
                return this.userRegistService.signUp(username, email, mobile, password, extraData).getUserId();
            }
        } else {
            throw new PageNotFoundException();
        }
    }
    
    @RequestMapping(
            value = "signup", 
            method = RequestMethod.POST, 
            consumes = {
                    MediaType.APPLICATION_JSON_VALUE, 
                    MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String signupJson( @RequestBody Map<String, Object> data) {
        String username = (String) data.get("username");
        Assert.hasText(username, "Required String parameter 'username' is not present");
        String password = (String) data.get("password");
        Assert.hasText(password, "Required String parameter 'password' is not present");
        
        String email = (String) data.get("email");
        String mobile = (String) data.get("mobile");

        data.remove("username");
        data.remove("email");
        data.remove("mobile");
        data.remove("password");
        
        return this.litesignup(username, email, mobile, password, data);
    }

}
