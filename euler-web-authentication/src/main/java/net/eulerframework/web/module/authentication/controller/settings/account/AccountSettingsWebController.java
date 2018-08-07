package net.eulerframework.web.module.authentication.controller.settings.account;

import javax.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import net.eulerframework.web.core.annotation.JspController;
import net.eulerframework.web.core.base.controller.JspSupportWebController;
import net.eulerframework.web.module.authentication.context.UserContext;
import net.eulerframework.web.module.authentication.service.PasswordService;

/**
 * @author cFrost
 *
 */
@JspController
@RequestMapping("/settings/account")
public class AccountSettingsWebController extends JspSupportWebController {
    
    public AccountSettingsWebController() {
        super();
        this.setWebControllerName("settings/account");
    }

    @Resource
    private PasswordService passwordService;    

    @RequestMapping(value = "change-password", method = RequestMethod.GET)
    public String changePassword() {
        return this.display("change-password");
    }

    @RequestMapping(value = "change-password", method = RequestMethod.POST)
    public String changePassword(
            @RequestParam(required = true) String oldPassword, 
            @RequestParam(required = true) String newPassword) {
        String userId = UserContext.getCurrentUser().getUserId().toString();
        this.passwordService.updatePassword(userId, oldPassword, newPassword);
        return this.success();
    }
}
