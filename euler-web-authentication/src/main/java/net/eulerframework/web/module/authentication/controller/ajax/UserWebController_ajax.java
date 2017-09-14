/**
 * 
 */
package net.eulerframework.web.module.authentication.controller.ajax;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import net.eulerframework.web.core.annotation.AjaxWebController;
import net.eulerframework.web.core.base.controller.AjaxSupportWebController;
import net.eulerframework.web.core.base.response.easyuisupport.EasyUIAjaxResponse;
import net.eulerframework.web.module.authentication.service.PasswordService;

/**
 * @author cFrost
 *
 */
@AjaxWebController
@RequestMapping("/")
public class UserWebController_ajax extends AjaxSupportWebController {

    @Resource
    private PasswordService passwordService;  

    @ResponseBody
    @RequestMapping(value = "getPasswordResetSMS", method = RequestMethod.POST)
    public EasyUIAjaxResponse<String> getPasswordResetSMS(@RequestParam String mobile) {
        this.passwordService.passwdResetSMSGen(mobile);
        return EasyUIAjaxResponse.SUCCESS_RESPONSE;
    }

}
