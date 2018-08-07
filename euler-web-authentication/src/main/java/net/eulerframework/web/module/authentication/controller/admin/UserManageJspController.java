package net.eulerframework.web.module.authentication.controller.admin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import net.eulerframework.web.core.annotation.JspController;
import net.eulerframework.web.core.base.controller.JspSupportWebController;

/**
 * @author cFrost
 *
 */
@JspController
@RequestMapping(path = "authentication/user")
public class UserManageJspController extends JspSupportWebController {
    
    public UserManageJspController() {
        this.setWebControllerName("authentication/user");
    }
    
    @RequestMapping(path = "userManage", method = RequestMethod.GET)
    public String userManage() {
        return this.display("userManage");
    }

}
