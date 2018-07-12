/**
 * 
 */
package net.eulerframework.web.module.authentication.controller.ajax;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import net.eulerframework.common.util.CommonUtils;
import net.eulerframework.web.core.annotation.AjaxController;
import net.eulerframework.web.core.base.controller.ApiSupportWebController;
import net.eulerframework.web.module.authentication.util.Captcha;

/**
 * 验证码验证接口
 * @author cFrost
 *
 */
@AjaxController
@RequestMapping("/")
public class CaptchaAjaxController extends ApiSupportWebController {
    
    @RequestMapping(path="validCaptcha", method = RequestMethod.GET)
    public void validCaptcha(@RequestParam String captcha) {
        CommonUtils.sleep(1);
        Captcha.validCaptcha(captcha, this.getRequest());
    }
}
