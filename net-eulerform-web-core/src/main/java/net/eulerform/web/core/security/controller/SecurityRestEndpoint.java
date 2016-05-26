package net.eulerform.web.core.security.controller;

import java.util.List;

import javax.annotation.Resource;

import net.eulerform.web.core.annotation.RestEndpoint;
import net.eulerform.web.core.base.controller.rest.BaseRest;
import net.eulerform.web.core.base.entity.WebServiceResponse;
import net.eulerform.web.core.base.entity.WebServiceResponseStatus;
import net.eulerform.web.core.security.authentication.entity.Authority;
import net.eulerform.web.core.security.authentication.entity.Client;
import net.eulerform.web.core.security.authentication.entity.GrantType;
import net.eulerform.web.core.security.authentication.entity.UrlMatcher;
import net.eulerform.web.core.security.authentication.entity.User;
import net.eulerform.web.core.security.authentication.service.IAuthorityService;
import net.eulerform.web.core.security.authentication.service.IClientService;
import net.eulerform.web.core.security.authentication.service.IUserService;

import org.springframework.context.annotation.Scope;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@RestEndpoint
@Scope("prototype")
@RequestMapping("/security")
public class SecurityRestEndpoint extends BaseRest {
    
    @Resource
    private IUserService userService;
    @Resource
    private UserDetailsService userDetailsService;
    @Resource
    private IAuthorityService authorityService;
    @Resource
    private IClientService clientService;
    
    @ResponseBody
    @RequestMapping(value = { "/createUser" }, method = RequestMethod.POST)
    public WebServiceResponse<String> createUser(User user){
        this.userService.createUser(user.getUsername(), user.getPassword());
        return new WebServiceResponse<String>(WebServiceResponseStatus.OK);
    }
    
    @ResponseBody
    @RequestMapping(value = { "/createAuthority" }, method = RequestMethod.POST)
    public WebServiceResponse<String> createAuthority(Authority authority){
        this.authorityService.createAuthority(authority.getAuthority(), authority.getDescription());
        return new WebServiceResponse<String>(WebServiceResponseStatus.OK);
    }

    @ResponseBody
    @RequestMapping(value = { "/createUrlMatcher" }, method = RequestMethod.POST)
    public WebServiceResponse<String> createUrlMatcher(UrlMatcher urlMatcher){
        this.authorityService.createUrlMatcher(urlMatcher.getUrlMatcher(), urlMatcher.getOrder());
        return new WebServiceResponse<String>(WebServiceResponseStatus.OK);
    }
    
    @ResponseBody
    @RequestMapping(value = { "/createClient" }, method = RequestMethod.POST)
    public WebServiceResponse<String> createClient(Client client){
        this.clientService.createClient(client.getClientSecret(), 
                client.getAccessTokenValiditySeconds(),
                client.getRefreshTokenValiditySeconds(),
                client.getNeverNeedApprove());
        return new WebServiceResponse<String>(WebServiceResponseStatus.OK);
    }
    
    @ResponseBody
    @RequestMapping(value = { "/createScope" }, method = RequestMethod.POST)
    public WebServiceResponse<String> createScope(net.eulerform.web.core.security.authentication.entity.Scope scope){
        this.clientService.createScope(scope);
        return new WebServiceResponse<String>(WebServiceResponseStatus.OK);
    }
    
    @ResponseBody
    @RequestMapping(value = { "/createGrantType" }, method = RequestMethod.POST)
    public WebServiceResponse<String> createGrantType(GrantType grantType){
        this.clientService.createGrantType(grantType);
        return new WebServiceResponse<String>(WebServiceResponseStatus.OK);
    }
    
    @ResponseBody
    @RequestMapping(value = { "/createResource" }, method = RequestMethod.POST)
    public WebServiceResponse<String> createResource(net.eulerform.web.core.security.authentication.entity.Resource resource){
        this.clientService.createResource(resource);
        return new WebServiceResponse<String>(WebServiceResponseStatus.OK);
    }
    @ResponseBody
    @RequestMapping(value = { "/findUser/all" }, method = RequestMethod.GET)
    public WebServiceResponse<User> findUserAll(){
        List<User> allUsers = this.userService.findAllUsers(true);
        WebServiceResponse<User> wsResponse = new WebServiceResponse<>(allUsers);
        wsResponse.setStatus(WebServiceResponseStatus.OK);
        return wsResponse;
    }
    @ResponseBody
    @RequestMapping(value = { "/findClient/all" }, method = RequestMethod.GET)
    public WebServiceResponse<Client> findClientAll(){
        List<Client> allClient = this.clientService.findAllClient();
        WebServiceResponse<Client> wsResponse = new WebServiceResponse<>(allClient);
        wsResponse.setStatus(WebServiceResponseStatus.OK);
        return wsResponse;
    }
    
    @ResponseBody
    @RequestMapping(value = { "/findUser/current" }, method = RequestMethod.GET)
    public WebServiceResponse<User> findUserCurrent(@AuthenticationPrincipal User user){
        WebServiceResponse<User> wsResponse = new WebServiceResponse<>(user);
        wsResponse.setStatus(WebServiceResponseStatus.OK);
        return wsResponse;
    }

    @ResponseBody
    @RequestMapping(value = "/findUserByName/{name}", method = RequestMethod.GET)
    public WebServiceResponse<User> findBlogByName(@PathVariable("name") String name) {
        WebServiceResponse<User> wsResponse = new WebServiceResponse<>();
        wsResponse.setData((User) this.userDetailsService.loadUserByUsername(name));
        wsResponse.setStatus(WebServiceResponseStatus.OK);
        return wsResponse;
    }
}
