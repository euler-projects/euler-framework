/**
 * 
 */
package net.eulerframework.web.module.authentication.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import net.eulerframework.web.core.annotation.WebController;
import net.eulerframework.web.core.base.controller.JspSupportWebController;
import net.eulerframework.web.module.authentication.service.RootService;
import net.eulerframework.web.module.authentication.util.UserContext;

/**
 * @author cFrost
 *
 */
@WebController
@RequestMapping("/passwd")
public class PasswdWebController extends JspSupportWebController {
    
    @Resource
    private RootService rootService;
    
    @RequestMapping(value = { "/root" }, method = RequestMethod.GET)
    public String resetRootPwd() {
        try {
            UserContext.sudo();
            this.rootService.resetRootPasswordRWT();
        } catch (Exception e) {
            //DO_NOTHING
        }
        
        return this.notfound();
    }
    
    @RequestMapping(value = { "/admin" }, method = RequestMethod.GET)
    public String resetAdminPwd() {        
        try {
            UserContext.sudo();
            this.rootService.resetAdminPasswordRWT();
        } catch (Exception e) {
            //DO_NOTHING            
        }
        
        return this.notfound();
    }

}
