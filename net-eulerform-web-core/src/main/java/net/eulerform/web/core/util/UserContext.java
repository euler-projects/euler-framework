package net.eulerform.web.core.util;

import net.eulerform.web.core.security.authentication.entity.User;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

public class UserContext {
    
    private static UserDetailsService userDetailsService;
    
    public static void setUserDetailsService(UserDetailsService userDetailsService) {
        UserContext.userDetailsService = userDetailsService;
    }

    private final static User ANONYMOUS_USER;
    private final static String ANONYMOUS_USERNAME="anonymousUser";
    
    static {
        ANONYMOUS_USER = new User();
        ANONYMOUS_USER.setId(ANONYMOUS_USERNAME);
        ANONYMOUS_USER.setUsername(ANONYMOUS_USERNAME);
        ANONYMOUS_USER.setAuthorities(null);
        ANONYMOUS_USER.setAccountNonExpired(false);
        ANONYMOUS_USER.setAccountNonLocked(false);
        ANONYMOUS_USER.setEnabled(false);
        ANONYMOUS_USER.setCredentialsNonExpired(false);
    }
    
    public static User getCurrentUser() {
        try {
            SecurityContext context = SecurityContextHolder.getContext();
            if(context != null && context.getAuthentication() != null){
                Object principal = context.getAuthentication().getPrincipal();
            
                if(principal.getClass().isAssignableFrom(User.class))
                    return (User)context.getAuthentication().getPrincipal();
                
                if(principal.getClass().isAssignableFrom(String.class) && (!ANONYMOUS_USERNAME.equalsIgnoreCase((String) principal))) {
                    User user = (User) userDetailsService.loadUserByUsername((String) principal);
                    user.eraseCredentials();
                    return user;
                }
            }
        } catch (Exception e) {
            //DO NOTHING
        }
        return ANONYMOUS_USER;
    }
}