/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2013-2018 cFrost.sun(孙宾, SUN BIN) 
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * For more information, please visit the following website
 * 
 * https://eulerproject.io
 * https://cfrost.net
 */
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
