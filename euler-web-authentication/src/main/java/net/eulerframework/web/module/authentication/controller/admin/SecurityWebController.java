package net.eulerframework.web.module.authentication.controller.admin;

import javax.annotation.Resource;
import net.eulerframework.web.core.base.controller.AbstractWebController;
import net.eulerframework.web.core.base.request.PageQueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;

import org.springframework.context.annotation.Scope;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import net.eulerframework.web.core.annotation.AdminWebController;
import net.eulerframework.web.module.authentication.entity.User;
import net.eulerframework.web.module.authentication.service.IAuthorityService;
import net.eulerframework.web.module.authentication.service.IUserService;

@AdminWebController
@Scope("prototype")
@RequestMapping("security")
public class SecurityWebController extends AbstractWebController {
    

	@Resource
	private IUserService userService;
	@Resource
	private UserDetailsService userDetailsService;
	@Resource
	private IAuthorityService authorityService;

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
    
    @RequestMapping(value ="findUserByPage")
    @ResponseBody
    public PageResponse<User> findUserByPage(){
        return this.userService.findUserByPage(new PageQueryRequest(this.getRequest()));
    }
}
