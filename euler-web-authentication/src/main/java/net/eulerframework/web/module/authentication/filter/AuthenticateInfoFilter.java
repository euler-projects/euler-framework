package net.eulerframework.web.module.authentication.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.eulerframework.web.module.authentication.util.UserContext;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.filter.OncePerRequestFilter;

import net.eulerframework.web.module.authentication.entity.User;

public class AuthenticateInfoFilter extends OncePerRequestFilter {
    
    private String[] excludeServletPath;

    public void setExcludeServletPath(String[] excludeServletPath) {
        this.excludeServletPath = excludeServletPath;
    }

    public AuthenticateInfoFilter() {
        
    }
    
    public AuthenticateInfoFilter(String... excludeServletPath) {
        this.excludeServletPath = excludeServletPath;        
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String servletPath = request.getServletPath();
        
        if(excludeServletPath  != null) {
            for(String each : excludeServletPath) {
                if(servletPath.endsWith(each)){
                    filterChain.doFilter(request, response);
                    return;
                }
            }
        }
        
        User curUser = UserContext.getCurrentUser();
        request.setAttribute("currentUser", curUser);
        ThreadContext.put("username", curUser.getUsername());
        filterChain.doFilter(request, response);
    }

}
