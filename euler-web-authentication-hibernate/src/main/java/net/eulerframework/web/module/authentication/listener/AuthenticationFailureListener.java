package net.eulerframework.web.module.authentication.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import net.eulerframework.web.util.ServletUtils;

//@Component  
public class AuthenticationFailureListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {
    @Override  
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent authenticationFailureBadCredentialsEvent) {  
        authenticationFailureBadCredentialsEvent.getAuthentication().getPrincipal().toString();  
        System.out.println(ServletUtils.getRealIP());
    }  
}  
