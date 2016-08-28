package net.eulerform.web.module.oauth2.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import net.eulerform.web.core.annotation.RestEndpoint;
import net.eulerform.web.core.base.controller.AbstractRestEndpoint;
import net.eulerform.web.module.authentication.entity.Authority;
import net.eulerform.web.module.authentication.entity.User;
import net.eulerform.web.module.authentication.util.UserContext;


@RestEndpoint
@Scope("prototype")
@RequestMapping("/oauth2")
public class Oauth2LocalRestEndpoint extends AbstractRestEndpoint {
	
    @ResponseBody
    @RequestMapping(value = "/authority", method = RequestMethod.GET)
    public Map<String, ?> authority() {
        Map<String, Object> result = new HashMap<>();
        
        User currentUser = UserContext.getCurrentUser();
        
        if(currentUser == null || User.ANONYMOUS_USER.equals(currentUser)){
            result.put("user_name", User.ANONYMOUS_USER.getUsername());
            return result;
        }
        
        Set<Authority> authorities = currentUser.getAuthorities();
        List<String> authoritieStrs = new ArrayList<>();
        for(Authority each : authorities) {
            authoritieStrs.add(each.getAuthority());
        }
        
        if(!authoritieStrs.isEmpty())
            result.put("authorities", authoritieStrs);
        result.put("user_name", currentUser.getUsername());
        return result;
    }
}
