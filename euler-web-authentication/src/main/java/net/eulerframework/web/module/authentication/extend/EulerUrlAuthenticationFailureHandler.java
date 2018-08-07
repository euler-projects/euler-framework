package net.eulerframework.web.module.authentication.extend;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.util.StringUtils;

public class EulerUrlAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private String failureParam="authentication_failed";

	public EulerUrlAuthenticationFailureHandler() {
	}

	public EulerUrlAuthenticationFailureHandler(String failureParam) {
		setFailureParam(failureParam);
	}

	@Override
	public void onAuthenticationFailure(HttpServletRequest request,
			HttpServletResponse response, AuthenticationException exception)
			throws IOException, ServletException {

		saveException(request, exception);
		
		String requestUrl = request.getRequestURI().substring(request.getContextPath().length());
        String queryString = request.getQueryString();
        
        if(StringUtils.hasText(queryString)) { 
            if(!queryString.endsWith(failureParam)) {
                queryString += ("&" + failureParam);
            }
        } else {
            queryString = failureParam;
        }
        
        String redirectUrl = requestUrl + "?" + queryString;

		if (isUseForward()) {
			logger.debug("Forwarding to " + redirectUrl);

			request.getRequestDispatcher(redirectUrl)
					.forward(request, response);
		}
		else {
			logger.debug("Redirecting to " + redirectUrl);
			getRedirectStrategy().sendRedirect(request, response, redirectUrl);
		}
	}

    public void setFailureParam(String failureParam) {
        this.failureParam = failureParam;
    }
	
}
