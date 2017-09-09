package net.eulerframework.web.module.authentication.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.eulerframework.web.core.filter.ExcluedableFilter;
import net.eulerframework.web.module.authentication.context.UserContext;
import net.eulerframework.web.module.authentication.entity.User;

public class UserInfoFilter extends ExcluedableFilter {
    
    public UserInfoFilter() {
        super();
        
    }
    
    public UserInfoFilter(String... excludeServletPath) {
        super(excludeServletPath);       
    }

    @Override
    protected void doFilterIn(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {        
        User curUser = UserContext.getCurrentUser();
        request.setAttribute("__USERINFO", curUser);
        request.setAttribute("__USER_ID", curUser.getId());
        request.setAttribute("__USER_NAME", curUser.getUsername());
        
        filterChain.doFilter(request, response);
    }

}
