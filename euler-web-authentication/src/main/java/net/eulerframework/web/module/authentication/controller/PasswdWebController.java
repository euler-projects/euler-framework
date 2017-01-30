/**
 * 
 */
package net.eulerframework.web.module.authentication.controller;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import net.eulerframework.web.core.annotation.WebController;
import net.eulerframework.web.core.base.controller.AbstractWebController;
import net.eulerframework.web.module.authentication.service.RootService;
import net.eulerframework.web.module.authentication.util.UserContext;

/**
 * @author cFrost
 *
 */
@WebController
@Scope("prototype")
@RequestMapping("/passwd")
public class PasswdWebController extends AbstractWebController {
    
    @Resource
    private RootService rootService;
    
    @RequestMapping(value = { "/root" }, method = RequestMethod.GET)
    public void resetRootPwd() {
        UserContext.sudo();
        this.rootService.resetRootPasswordRWT();
    }
    
    @RequestMapping(value = { "/admin" }, method = RequestMethod.GET)
    public void resetAdminPwd() {
        UserContext.sudo();
        this.rootService.resetAdminPasswordRWT();
    }

}
