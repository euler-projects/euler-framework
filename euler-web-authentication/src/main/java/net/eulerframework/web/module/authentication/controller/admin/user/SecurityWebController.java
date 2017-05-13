package net.eulerframework.web.module.authentication.controller.admin.user;

import javax.annotation.Resource;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import net.eulerframework.web.core.annotation.WebController;
import net.eulerframework.web.core.base.controller.JspSupportWebController;
import net.eulerframework.web.module.authentication.service.AuthorityService;
import net.eulerframework.web.module.authentication.service.UserService;

@WebController
@RequestMapping("security")
public class SecurityWebController extends JspSupportWebController {
    

	@Resource
	private UserService userService;
	@Resource
	private UserDetailsService userDetailsService;
	@Resource
	private AuthorityService authorityService;

    @RequestMapping(value ="user",method=RequestMethod.GET)
    public String user(){
        return this.display("user");
    }
    
    @RequestMapping(value ="group",method=RequestMethod.GET)
    public String group(){
        return this.display("group");
    }
    
    @RequestMapping(value ="authority",method=RequestMethod.GET)
    public String authority(){
        return this.display("authority");
    }
}
