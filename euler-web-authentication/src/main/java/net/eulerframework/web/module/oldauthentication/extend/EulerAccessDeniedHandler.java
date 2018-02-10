package net.eulerframework.web.module.oldauthentication.extend;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;

import net.eulerframework.web.util.ServletUtils;

public class EulerAccessDeniedHandler extends AccessDeniedHandlerImpl {
    
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException,
            ServletException {
        System.out.println("!!!" + ServletUtils.getRealIP(request));
        super.handle(request, response, accessDeniedException);
    }

}
