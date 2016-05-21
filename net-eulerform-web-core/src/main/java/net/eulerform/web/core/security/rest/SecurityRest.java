package net.eulerform.web.core.security.rest;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import net.eulerform.web.core.base.entity.RetResult;
import net.eulerform.web.core.base.entity.RetStatus;
import net.eulerform.web.core.base.rest.BaseRest;
import net.eulerform.web.core.security.authentication.entity.Authority;
import net.eulerform.web.core.security.authentication.entity.UrlMatcher;
import net.eulerform.web.core.security.authentication.entity.User;
import net.eulerform.web.core.security.authentication.service.IAuthorityService;
import net.eulerform.web.core.security.authentication.service.IUserService;

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
    public RetResult<String> createUser(@ModelAttribute( "user" ) User user){
        this.userService.createUser(user.getUsername(), user.getPassword());
        return new RetResult<String>(RetStatus.SUCCESS);
    }
    
    @ResponseBody
    @RequestMapping(value = { "/createAuthority" }, method = RequestMethod.POST)
    public RetResult<String> createAuthority(@ModelAttribute( "authority" ) Authority authority){
        this.authorityService.createAuthority(authority.getAuthority(), authority.getDescription());
        return new RetResult<String>(RetStatus.SUCCESS);
    }

    @ResponseBody
    @RequestMapping(value = { "/createUrlMatcher" }, method = RequestMethod.POST)
    public RetResult<String> createUrlMatcher(@ModelAttribute( "urlMatcher" ) UrlMatcher urlMatcher){
        this.authorityService.createUrlMatcher(urlMatcher.getUrlMatcher(), urlMatcher.getOrder());
        return new RetResult<String>(RetStatus.SUCCESS);
    }
    
    @ResponseBody
    @RequestMapping(value = { "/getUser/all" }, method = RequestMethod.GET)
    public RetResult<User> getUserAll(){
        List<User> allUsers = this.userService.findAllUsers(true);
        RetResult<User> ret = new RetResult<User>(allUsers);
        ret.setReturnStatus(RetStatus.SUCCESS);
        return ret;
    }
    
    @ResponseBody
    @RequestMapping(value = { "/getUser/current" }, method = RequestMethod.GET)
    public RetResult<User> getUserCurrent(@AuthenticationPrincipal User user){
        RetResult<User> ret = new RetResult<User>(user);
        ret.setReturnStatus(RetStatus.SUCCESS);
        return ret;
    }
}
