package net.eulerframework.web.module.authentication.controller.admin;

import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.context.annotation.Scope;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import net.eulerframework.web.core.annotation.AdminWebController;
import net.eulerframework.web.core.base.controller.JspSupportWebController;
import net.eulerframework.web.core.base.request.PageQueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.module.authentication.entity.Authority;
import net.eulerframework.web.module.authentication.entity.Group;
import net.eulerframework.web.module.authentication.entity.User;
import net.eulerframework.web.module.authentication.service.AuthorityService;
import net.eulerframework.web.module.authentication.service.UserService;

@AdminWebController
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

    /*=============== user page =================*/
    
    @RequestMapping(value ="findUserByPage")
    @ResponseBody
    public PageResponse<User> findUserByPage(){
        return this.userService.findUserByPage(new PageQueryRequest(this.getRequest(), "page", "rows"));
    }
    
    @ResponseBody
    @RequestMapping(value="findAllGroups")
    public List<Group> findAllGroups() {
        return this.authorityService.findAllGroups();
    }
    
    /*=============== group page =================*/
    
    @RequestMapping(value ="findGroupByPage")
    @ResponseBody
    public PageResponse<Group> findGroupByPage() {
        return this.authorityService.findGroupByPage(new PageQueryRequest(this.getRequest(), "page", "rows"));
    }
    
    @ResponseBody
    @RequestMapping(value="/findAllAuthorities")
    public List<Authority> findAllAuthorities() {
        return this.authorityService.findAllAuthorities();
    }
    
    @ResponseBody
    @RequestMapping(value ="/saveGroupAuthorities", method = RequestMethod.POST)
    public void saveGroupAuthorities(@RequestParam String groupId, @RequestParam String athorityIds) {
        String[] idArray = athorityIds.trim().replace(" ", "").split(";");
        List<Authority> authorities = this.authorityService.findAuthorityByIds(idArray);
        this.authorityService.saveGroupAuthorities(groupId, authorities);
    }

    @ResponseBody
    @RequestMapping(value = { "/saveGroup" }, method = RequestMethod.POST)
    public void saveGroup(Group group) {
        this.authorityService.saveGroup(group);
    }
    
    @ResponseBody
    @RequestMapping(value ="/deleteGroups", method = RequestMethod.POST)
    public void deleteGroups(@RequestParam String ids) {
        String[] idArray = ids.trim().replace(" ", "").split(";");
        this.authorityService.deleteGroups(idArray);
    }
    
    /*=============== authority page =================*/

    @ResponseBody
    @RequestMapping(value ="/findAuthorityByPage")
    public PageResponse<Authority> findAuthorityByPage() {
        return this.authorityService.findAuthorityByPage(new PageQueryRequest(this.getRequest(), "page", "rows"));
    }

    @ResponseBody
    @RequestMapping(value = { "/saveAuthority" }, method = RequestMethod.POST)
    public void saveAuthority(@Valid Authority authority) {
        this.authorityService.saveAuthority(authority);
    }
    
    @ResponseBody
    @RequestMapping(value ="/deleteAuthorities", method = RequestMethod.POST)
    public void deleteAuthorities(@RequestParam String ids) {
        String[] idArray = ids.trim().replace(" ", "").split(";");
        this.authorityService.deleteAuthorities(idArray);
    }
}
