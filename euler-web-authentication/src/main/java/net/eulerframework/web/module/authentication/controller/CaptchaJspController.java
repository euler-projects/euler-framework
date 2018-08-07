package net.eulerframework.web.module.authentication.controller;

import java.io.IOException;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import net.eulerframework.web.core.annotation.JspController;
import net.eulerframework.web.core.base.controller.JspSupportWebController;
import net.eulerframework.web.module.authentication.util.Captcha;

/**
 * @author cFrost
 *
 */
@JspController
@RequestMapping("captcha")
public class CaptchaJspController extends JspSupportWebController {
    
    @RequestMapping(path = "simple", method = RequestMethod.GET)
    @ResponseBody
    public void captcha() throws IOException {
        Captcha c = new Captcha();
        c.getRandcode(this.getRequest(), this.getResponse());
    }
}
