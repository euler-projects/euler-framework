package net.eulerframework.web.module.authentication.controller.admin.user;

import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import net.eulerframework.web.core.annotation.AdminWebController;
import net.eulerframework.web.core.base.controller.AjaxSupportWebController;
import net.eulerframework.web.core.base.request.PageQueryRequest;
import net.eulerframework.web.core.base.request.easyuisupport.EasyUiQueryReqeuset;
import net.eulerframework.web.core.base.response.easyuisupport.EasyUIAjaxResponse;
import net.eulerframework.web.core.base.response.easyuisupport.EasyUIPageResponse;
import net.eulerframework.web.core.exception.web.DefaultAjaxException;
import net.eulerframework.web.module.authentication.entity.Authority;
import net.eulerframework.web.module.authentication.entity.Group;
import net.eulerframework.web.module.authentication.entity.User;
import net.eulerframework.web.module.authentication.exception.UserNotFoundException;
import net.eulerframework.web.module.authentication.service.AuthorityService;
import net.eulerframework.web.module.authentication.service.UserService;

@AdminWebController
@RequestMapping("security")
public class SecurityWebController_ajax extends AjaxSupportWebController {
    

	@Resource
	private UserService userService;
	@Resource
	private UserDetailsService userDetailsService;
	@Resource
	private AuthorityService authorityService;
    /*=============== user page =================*/
    
    @RequestMapping(value ="findUserByPage_ajax")
    @ResponseBody
    public EasyUIPageResponse<User> findUserByPage(){
        return this.userService.findUserByPage(new EasyUiQueryReqeuset(this.getRequest()));
    }
    
    @ResponseBody
    @RequestMapping(value="findAllGroups_ajax")
    public List<Group> findAllGroups() {
        return this.authorityService.findAllGroups();
    }

    @ResponseBody
    @RequestMapping(value="loadUser_ajax", method = RequestMethod.POST)
    public EasyUIAjaxResponse<User> loadUser(@RequestParam String userId) {
        return new EasyUIAjaxResponse<>(this.userService.loadUser(userId));
    }
    
    @ResponseBody
    @RequestMapping(value="addUser_ajax", method = RequestMethod.POST)
    public EasyUIAjaxResponse<String> addUser(User user, @RequestParam String groupId) {
        this.userService.save(user);
        try {
            this.userService.addGroup(user.getId(), groupId);
        } catch (UserNotFoundException e) {
            throw new DefaultAjaxException(e.getMessage(), e);
        }
        return EasyUIAjaxResponse.SUCCESS_RESPONSE;
    }
    
    @ResponseBody
    @RequestMapping(value="updateUser_ajax", method = RequestMethod.POST)
    public EasyUIAjaxResponse<String> updateUser(User user) {
        try {
            this.userService.updateUsername(user.getId(), user.getUsername());
            this.userService.updateFullname(user.getId(), user.getFullName());
            this.userService.updateMobile(user.getId(), user.getMobile());
            this.userService.updateEmail(user.getId(), user.getEmail());
            this.userService.updateStatus(user.getId(), user.isEnabled());
            //this.userService.removeAllAndAddGroup(user.getId(), groupId);
        } catch (UserNotFoundException e) {
            throw new DefaultAjaxException(e.getMessage(), e);
        }
        return EasyUIAjaxResponse.SUCCESS_RESPONSE;
    }
    
    @ResponseBody
    @RequestMapping(value="resetPassword_ajax", method = RequestMethod.POST)
    public EasyUIAjaxResponse<String> resetPassword(
            @RequestParam String userId, 
            @RequestParam String password) {
        try {
            this.userService.updateUserPasswordWithoutCheck(userId, password);
        } catch (UserNotFoundException e) {
            throw new DefaultAjaxException(e.getMessage(), e);
        }
        return EasyUIAjaxResponse.SUCCESS_RESPONSE;
    }
    
    /*=============== group page =================*/
    
    @RequestMapping(value ="findGroupByPage")
    @ResponseBody
    public EasyUIPageResponse<Group> findGroupByPage() {
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
    public EasyUIPageResponse<Authority> findAuthorityByPage() {
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
