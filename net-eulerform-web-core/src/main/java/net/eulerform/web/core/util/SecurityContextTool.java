package net.eulerform.web.core.util;

import net.eulerform.web.core.security.authentication.entity.User;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityContextTool {
    
    public static SecurityContext getSecurityContext() {
        return SecurityContextHolder.getContext();
    }

    public static Authentication getAuthentication() {
        return SecurityContextTool.getSecurityContext().getAuthentication();
    }
    
    public static User getCurrentUser() {
        try {
            return (User)SecurityContextTool.getAuthentication().getPrincipal();
        } catch (Exception e){
            return null;
        }
    }

}
