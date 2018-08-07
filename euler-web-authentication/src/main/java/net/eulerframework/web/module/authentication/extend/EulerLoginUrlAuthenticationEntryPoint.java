package net.eulerframework.web.module.authentication.extend;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.base.response.RedirectResponse;

/**
 * @author cFrost
 *
 */
public class EulerLoginUrlAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {

    private ObjectMapper objectMapper;

    /**
     * @param loginFormUrl
     */
    public EulerLoginUrlAuthenticationEntryPoint(String loginFormUrl, ObjectMapper objectMapper) {
        super(loginFormUrl);
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        if (this.isAjaxRequest(request)) {
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            response.getOutputStream().print(this.objectMapper.writeValueAsString(
                    new RedirectResponse(this.buildRedirectUrlToLoginPage(request, response, authException))));

        } else {
            super.commence(request, response, authException);
        }
    }

    protected boolean isAjaxRequest(HttpServletRequest request) {
        return (request.getRequestURI().startsWith(request.getContextPath() + "/ajax") || request.getRequestURI()
                .startsWith(request.getContextPath() + WebConfig.getAdminRootPath() + "/ajax"));
    }
}
