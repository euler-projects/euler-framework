package net.eulerframework.web.module.oldauthentication.extend;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import net.eulerframework.web.util.ServletUtils;

public class EulerUrlAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        System.out.println("!!!" + ServletUtils.getRealIP(request));
        super.onAuthenticationFailure(request, response, exception);
    }

}
