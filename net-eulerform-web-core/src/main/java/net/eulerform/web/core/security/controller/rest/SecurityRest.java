package net.eulerform.web.core.security.controller.rest;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import net.eulerform.web.core.base.controller.rest.BaseRest;
import net.eulerform.web.core.base.entity.RestResponseEntity;
import net.eulerform.web.core.base.entity.RestResponseStatus;
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
    public RestResponseEntity<String> createUser(User user){
        this.userService.createUser(user.getUsername(), user.getPassword());
        return new RestResponseEntity<String>(RestResponseStatus.SUCCESS);
    }
    
    @ResponseBody
    @RequestMapping(value = { "/createAuthority" }, method = RequestMethod.POST)
    public RestResponseEntity<String> createAuthority(Authority authority){
        this.authorityService.createAuthority(authority.getAuthority(), authority.getDescription());
        return new RestResponseEntity<String>(RestResponseStatus.SUCCESS);
    }

    @ResponseBody
    @RequestMapping(value = { "/createUrlMatcher" }, method = RequestMethod.POST)
    public RestResponseEntity<String> createUrlMatcher(UrlMatcher urlMatcher){
        this.authorityService.createUrlMatcher(urlMatcher.getUrlMatcher(), urlMatcher.getOrder());
        return new RestResponseEntity<String>(RestResponseStatus.SUCCESS);
    }
    
    @ResponseBody
    @RequestMapping(value = { "/getUser/all" }, method = RequestMethod.GET)
    public RestResponseEntity<User> getUserAll(){
        List<User> allUsers = this.userService.findAllUsers(true);
        RestResponseEntity<User> restResponseEntity = new RestResponseEntity<User>(allUsers);
        restResponseEntity.setStatus(RestResponseStatus.SUCCESS);
        return restResponseEntity;
    }
    
    @ResponseBody
    @RequestMapping(value = { "/getUser/current" }, method = RequestMethod.GET)
    public RestResponseEntity<User> getUserCurrent(@AuthenticationPrincipal User user){
        RestResponseEntity<User> restResponseEntity = new RestResponseEntity<User>(user);
        restResponseEntity.setStatus(RestResponseStatus.SUCCESS);
        return restResponseEntity;
    }
}
