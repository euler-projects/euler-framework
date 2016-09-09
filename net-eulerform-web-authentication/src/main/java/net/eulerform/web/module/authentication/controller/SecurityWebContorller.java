package net.eulerform.web.module.authentication.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.context.annotation.Scope;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import net.eulerform.common.util.BeanTool;
import net.eulerform.web.core.annotation.WebController;
import net.eulerform.web.core.base.controller.DefaultWebController;
import net.eulerform.web.core.base.exception.NotFoundException;
import net.eulerform.web.core.base.exception.ResourceExistException;
import net.eulerform.web.core.base.request.QueryRequest;
import net.eulerform.web.core.base.response.PageResponse;
import net.eulerform.web.module.authentication.entity.Authority;
import net.eulerform.web.module.authentication.entity.Group;
import net.eulerform.web.module.authentication.entity.User;
import net.eulerform.web.module.authentication.service.IAuthorityService;
import net.eulerform.web.module.authentication.service.IUserService;

@WebController
@Scope("prototype")
@RequestMapping("/authentication")
public class SecurityWebContorller extends DefaultWebController {

	@Resource
	private IUserService userService;
	@Resource
	private UserDetailsService userDetailsService;
	@Resource
	private IAuthorityService authorityService;

    @RequestMapping(value = { "/signin" }, method = RequestMethod.GET)
    public String login()
    {
        return "/authentication/signin";
    }
    @RequestMapping(value = { "/signup" }, method = RequestMethod.GET)
    public String signup()
    {
        return "/authentication/signup";
    }
    @RequestMapping(value = { "/forgotpasswd" }, method = RequestMethod.GET)
    public String forgotpasswd()
    {
        return "/authentication/forgotpasswd";
    }
    
    @RequestMapping(value = { "/resetPasswd/{userId}/{resetToken}" }, method = RequestMethod.GET)
    public String resetPasswdPage(
            @PathVariable("userId") String userId, 
            @PathVariable("resetToken") String resetToken,
            Model model)
    {
        try {
            this.userService.checkResetTokenRT(userId, resetToken);
        } catch (UsernameNotFoundException e) {
            throw new NotFoundException();
        }
        model.addAttribute("userId", userId);
        model.addAttribute("resetToken", resetToken);
        return "/authentication/resetPasswd";
    }

    @RequestMapping(value = { "/resetPasswd" }, method = RequestMethod.POST)
    public String resetPasswd(
            String userId, 
            String resetToken,
            String pwd)
    {
        this.userService.resetUserPasswordWithResetTokenRWT(userId, pwd, resetToken);
        return "/authentication/login";
    }
    
    @RequestMapping(value = { "/applyResetPasswd" }, method = RequestMethod.POST)
    public String applyResetPasswd(String email)
    {
        this.userService.forgotPasswordRWT(email);
        return "/authentication/signin";
    }
    
    @ResponseBody
    @RequestMapping(value = { "/signUp" }, method = RequestMethod.POST)
    public void signUp(String username, String password) {
        this.userService.createUser(username, password);
    }
    
    
    
    
    
    
    
    
    
    @RequestMapping(value ="/user",method=RequestMethod.GET)
    public String user(){
        return "/authentication/user";
    }
    
    @RequestMapping(value ="/group",method=RequestMethod.GET)
    public String group(){
        return "/authentication/group";
    }
    
    @RequestMapping(value ="/authority",method=RequestMethod.GET)
    public String authority(){
        return "/authentication/authority";
    }
    
    @RequestMapping(value ="/authorize",method=RequestMethod.GET)
    public String authorize(){
        return "/authentication/authorize";
    }
    
    @ResponseBody
    @RequestMapping(value ="/findUserByPage")
    public PageResponse<User> findUserByPage(HttpServletRequest request, String page, String rows) {
        QueryRequest queryRequest = new QueryRequest(request);
        
        int pageIndex = Integer.parseInt(page);
        int pageSize = Integer.parseInt(rows);
        return this.userService.findUserByPage(queryRequest, pageIndex, pageSize);
    }
    
    @ResponseBody
    @RequestMapping(value ="/findGroupByPage")
    public PageResponse<Group> findGroupByPage(HttpServletRequest request, String page, String rows) {
        QueryRequest queryRequest = new QueryRequest(request);
        
        int pageIndex = Integer.parseInt(page);
        int pageSize = Integer.parseInt(rows);
        return this.authorityService.findGroupByPage(queryRequest, pageIndex, pageSize);
    }

    @ResponseBody
    @RequestMapping(value ="/findAuthorityByPage")
    public PageResponse<Authority> findAuthorityByPage(HttpServletRequest request, String page, String rows) {
        QueryRequest queryRequest = new QueryRequest(request);
        
        int pageIndex = Integer.parseInt(page);
        int pageSize = Integer.parseInt(rows);
        return this.authorityService.findAuthorityByPage(queryRequest, pageIndex, pageSize);
    }

    @ResponseBody
    @RequestMapping(value = { "/saveUser" }, method = RequestMethod.POST)
    public void saveUser(User user) {
        BeanTool.clearEmptyProperty(user);
        if(user.getId() == null) {
            try {
                User tmp = (User) this.userDetailsService.loadUserByUsername(user.getUsername());
                if(tmp.isEnabled())
                    throw new ResourceExistException("User Existed!");
                user.setId(tmp.getId());
            } catch (UsernameNotFoundException e) {
                // DO Nothing
            }
        }
        this.userService.saveUser(user);
    }

    @ResponseBody
    @RequestMapping(value = { "/resetUserPassword" }, method = RequestMethod.POST)
    public void resetUserPassword(String id, String password) {
        this.userService.resetUserPasswordRWT(id, password);
    }

    @ResponseBody
    @RequestMapping(value = { "/saveGroup" }, method = RequestMethod.POST)
    public void saveGroup(Group group) {
        this.authorityService.saveGroup(group);
    }

    @ResponseBody
    @RequestMapping(value = { "/saveAuthority" }, method = RequestMethod.POST)
    public void saveAuthority(@Valid Authority authority) {
        this.authorityService.saveAuthority(authority);
    }
    
    @ResponseBody
    @RequestMapping(value ="/saveUserGroups", method = RequestMethod.POST)
    public void savaUserGroups(@RequestParam String userId, @RequestParam String groupIds) {
        String[] idArray = groupIds.trim().replace(" ", "").split(";");
        List<Group> groups = this.authorityService.findGroupByIds(idArray);
        this.userService.saveUserGroups(userId, groups);
    }
    
    @ResponseBody
    @RequestMapping(value ="/saveGroupAuthorities", method = RequestMethod.POST)
    public void saveGroupAuthorities(@RequestParam String groupId, @RequestParam String athorityIds) {
        String[] idArray = athorityIds.trim().replace(" ", "").split(";");
        List<Authority> authorities = this.authorityService.findAuthorityByIds(idArray);
        this.authorityService.saveGroupAuthorities(groupId, authorities);
    }
    
    @ResponseBody
    @RequestMapping(value ="/enableUsers", method = RequestMethod.POST)
    public void enableUsers(@RequestParam String ids) {
        String[] idArray = ids.trim().replace(" ", "").split(";");
        this.userService.enableUsersRWT(idArray);
    }
    
    @ResponseBody
    @RequestMapping(value ="/disableUsers", method = RequestMethod.POST)
    public void disableUsers(@RequestParam String ids) {
        String[] idArray = ids.trim().replace(" ", "").split(";");
        this.userService.disableUsersRWT(idArray);
    }
    
    @ResponseBody
    @RequestMapping(value ="/deleteUsers", method = RequestMethod.POST)
    public void deleteUsers(@RequestParam String ids) {
        String[] idArray = ids.trim().replace(" ", "").split(";");
        this.userService.deleteUsers(idArray);
    }
    
    @ResponseBody
    @RequestMapping(value ="/deleteGroups", method = RequestMethod.POST)
    public void deleteGroups(@RequestParam String ids) {
        String[] idArray = ids.trim().replace(" ", "").split(";");
        this.authorityService.deleteGroups(idArray);
    }
    
    @ResponseBody
    @RequestMapping(value ="/deleteAuthorities", method = RequestMethod.POST)
    public void deleteAuthorities(@RequestParam String ids) {
        String[] idArray = ids.trim().replace(" ", "").split(";");
        this.authorityService.deleteAuthorities(idArray);
    }
    
    @ResponseBody
    @RequestMapping(value="/findAllGroups")
    public List<Group> findAllGroups() {
        return this.authorityService.findAllGroups();
    }
    
    @ResponseBody
    @RequestMapping(value="/findAllAuthorities")
    public List<Authority> findAllAuthorities() {
        return this.authorityService.findAllAuthorities();
    }
}
