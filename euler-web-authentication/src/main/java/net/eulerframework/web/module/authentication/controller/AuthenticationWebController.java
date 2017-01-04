/**
 * 
 */
package net.eulerframework.web.module.authentication.controller;

import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import net.eulerframework.web.core.annotation.WebController;
import net.eulerframework.web.core.base.controller.AbstractWebController;

/**
 * @author cFrost
 *
 */
@WebController
@Scope("prototype")
@RequestMapping("/")
public class AuthenticationWebController extends AbstractWebController {
    
    @RequestMapping(value = { "signin" }, method = RequestMethod.GET)
    public String login()
    {
        return this.display("signin");
    }
    
    @RequestMapping(value = { "signup" }, method = RequestMethod.GET)
    public String signup()
    {
        return this.display("signup");
    }

}
