/**
 * 
 */
package net.eulerframework.web.module.oldauthentication.controller;

import javax.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import net.eulerframework.web.core.annotation.JspController;
import net.eulerframework.web.core.base.controller.JspSupportWebController;
import net.eulerframework.web.module.oldauthentication.service.IAuthenticationService;

/**
 * @author cFrost
 *
 */
@JspController
@RequestMapping("/settings/account")
public class AccountSettingsJspController extends JspSupportWebController {
    
    public AccountSettingsJspController() {
        this.setWebControllerName("settings/account");
    }

    @Resource
    private IAuthenticationService authenticationService;    

    @RequestMapping(value = "change-password", method = RequestMethod.GET)
    public String changePassword() {
        return this.display("change-password");
    }

    @RequestMapping(value = "change-password", method = RequestMethod.POST)
    public String changePassword(
            @RequestParam(required = true) String oldPassword, 
            @RequestParam(required = true) String newPassword) {
        this.authenticationService.changePassword(oldPassword, newPassword);
        return this.success();
    }
}
