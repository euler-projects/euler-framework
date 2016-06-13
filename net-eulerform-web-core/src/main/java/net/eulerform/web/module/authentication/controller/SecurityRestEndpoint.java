package net.eulerform.web.module.authentication.controller;

import java.util.List;

import javax.annotation.Resource;

import net.eulerform.web.core.annotation.RestEndpoint;
import net.eulerform.web.core.base.controller.BaseRest;
import net.eulerform.web.core.base.entity.WebServiceResponse;
import net.eulerform.web.module.authentication.entity.Authority;
import net.eulerform.web.module.authentication.entity.Client;
import net.eulerform.web.module.authentication.entity.UrlMatcher;
import net.eulerform.web.module.authentication.entity.User;
import net.eulerform.web.module.authentication.entity.Group;
import net.eulerform.web.module.authentication.service.IAuthorityService;
import net.eulerform.web.module.authentication.service.IClientService;
import net.eulerform.web.module.authentication.service.IUserService;

import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
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
    @RequestMapping(value = { "/group" }, method = RequestMethod.POST)
    public WebServiceResponse<String> createGroup(Group group) {
        this.authorityService.createGroup(group.getName(), group.getDescription());
        return new WebServiceResponse<String>(HttpStatus.OK);
    }
    
	@ResponseBody
	@RequestMapping(value = { "/user" }, method = RequestMethod.POST)
	public WebServiceResponse<String> createUser(User user) {
		this.userService.createUser(user.getUsername(), user.getPassword());
		return new WebServiceResponse<String>(HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = { "/authority" }, method = RequestMethod.POST)
	public WebServiceResponse<String> createAuthority(Authority authority) {
		this.authorityService.createAuthority(authority.getAuthority(), authority.getDescription());
		return new WebServiceResponse<String>(HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = { "/urlMatcher" }, method = RequestMethod.POST)
	public WebServiceResponse<String> createUrlMatcher(UrlMatcher urlMatcher) {
		this.authorityService.createUrlMatcher(urlMatcher.getUrlMatcher(), urlMatcher.getOrder());
		return new WebServiceResponse<String>(HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = { "/client" }, method = RequestMethod.POST)
	public WebServiceResponse<String> createClient(Client client) {
		this.clientService.createClient(client.getClientSecret(), client.getAccessTokenValiditySeconds(),
				client.getRefreshTokenValiditySeconds(), client.getNeverNeedApprove());
		return new WebServiceResponse<String>(HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = { "/scope" }, method = RequestMethod.POST)
	public WebServiceResponse<String> createScope(net.eulerform.web.module.authentication.entity.Scope scope) {
		this.clientService.createScope(scope);
		return new WebServiceResponse<String>(HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = { "/resource" }, method = RequestMethod.POST)
	public WebServiceResponse<String> createResource(
			net.eulerform.web.module.authentication.entity.Resource resource) {
		this.clientService.createResource(resource);
		return new WebServiceResponse<String>(HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = { "/user/all" }, method = RequestMethod.GET)
	public WebServiceResponse<User> findUserAll() {
		List<User> allUsers = this.userService.findAllUsers();
		return new WebServiceResponse<>(allUsers);
	}

	@ResponseBody
	@RequestMapping(value = { "/client/all" }, method = RequestMethod.GET)
	public WebServiceResponse<Client> findClientAll() {
		List<Client> allClient = this.clientService.findAllClient();
		return new WebServiceResponse<>(allClient);
	}

	@ResponseBody
	@RequestMapping(value = { "/user/current" }, method = RequestMethod.GET)
	public WebServiceResponse<User> findUserCurrent(@AuthenticationPrincipal User user) {
		return new WebServiceResponse<>(user);
	}

	@ResponseBody
	@RequestMapping(value = "/user/{name}", method = RequestMethod.GET)
	public WebServiceResponse<User> findBlogByName(@PathVariable("name") String name) {
		User user = (User) this.userDetailsService.loadUserByUsername(name);
		return new WebServiceResponse<>(user);
	}
}
