package net.eulerform.web.core.security.rest;

import java.util.List;

import javax.annotation.Resource;

import net.eulerform.web.core.base.entity.RetResult;
import net.eulerform.web.core.base.rest.BaseRest;
import net.eulerform.web.core.security.authentication.entity.Authority;
import net.eulerform.web.core.security.authentication.entity.UrlMatcher;
import net.eulerform.web.core.security.authentication.entity.User;
import net.eulerform.web.core.security.authentication.service.IAuthorityService;
import net.eulerform.web.core.security.authentication.service.IUserService;

import org.springframework.context.annotation.Scope;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Scope("prototype")
@RequestMapping("/security")
public class SecurityRest extends BaseRest {
    
    @Resource
    private IUserService userService;
    @Resource
    private IAuthorityService authorityService;
    
    @ResponseBody
    @RequestMapping(value = { "/createUser" }, method = RequestMethod.POST)
    public void createUser(@ModelAttribute( "user" ) User user){
        this.userService.createUser(user.getUsername(), user.getPassword());
    }
    
    @ResponseBody
    @RequestMapping(value = { "/createAuthority" }, method = RequestMethod.POST)
    public void createAuthority(@ModelAttribute( "authority" ) Authority authority){
        this.authorityService.createAuthority(authority.getAuthority(), authority.getDescription());
    }

    @ResponseBody
    @RequestMapping(value = { "/createUrlMatcher" }, method = RequestMethod.POST)
    public void createUrlMatcher(@ModelAttribute( "urlMatcher" ) UrlMatcher urlMatcher){
        this.authorityService.createUrlMatcher(urlMatcher.getUrlMatcher(), urlMatcher.getOrder());
    }
    
    @ResponseBody
    @RequestMapping(value = { "/getUser/all" }, method = RequestMethod.GET)
    public RetResult<User> getUserAll(){
        List<User> allUsers = this.userService.findAllUsers(true);
        RetResult<User> ret = new RetResult<User>(allUsers);
        ret.setReturnFlag(RetResult.SUCCESS);
        return ret;
    }
    
    @ResponseBody
    @RequestMapping(value = { "/getUser/current" }, method = RequestMethod.GET)
    public RetResult<User> getUserCurrent(@AuthenticationPrincipal User user){
        RetResult<User> ret = new RetResult<User>(user);
        ret.setReturnFlag(RetResult.SUCCESS);
        return ret;
    }
}
