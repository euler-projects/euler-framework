/**
 * 
 */
package net.eulerframework.web.module.authentication.controller.ajax;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import net.eulerframework.web.core.annotation.AjaxController;
import net.eulerframework.web.core.base.controller.AjaxSupportWebController;
import net.eulerframework.web.core.base.response.easyuisupport.EasyUIAjaxResponse;
import net.eulerframework.web.module.authentication.service.PasswordService;

/**
 * @author cFrost
 *
 */
@AjaxController
@RequestMapping("/")
public class ResetPasswordAjaxController extends AjaxSupportWebController {

    @Resource
    private PasswordService passwordService;  

    @RequestMapping(value = "reset-password-email-sms", method = RequestMethod.POST)
    public EasyUIAjaxResponse<String> getPasswordResetSMS(@RequestParam String mobile) {
        this.passwordService.passwdResetSMSGen(mobile);
        return EasyUIAjaxResponse.SUCCESS_RESPONSE;
    }

}
