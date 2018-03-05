/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.eulerframework.web.module.authentication.extend;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.util.StringUtils;

/**
 * <tt>AuthenticationFailureHandler</tt> which performs a redirect to the value of the
 * {@link #setDefaultFailureUrl defaultFailureUrl} property when the
 * <tt>onAuthenticationFailure</tt> method is called. If the property has not been set it
 * will send a 401 response to the client, with the error message from the
 * <tt>AuthenticationException</tt> which caused the failure.
 * <p>
 * If the {@code useForward} property is set, a {@code RequestDispatcher.forward} call
 * will be made to the destination instead of a redirect.
 *
 * @author Luke Taylor
 * @since 3.0
 */
public class EulerUrlAuthenticationFailureHandler implements
		AuthenticationFailureHandler {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private String failureParam="authentication_failed";
	private boolean forwardToDestination = false;
	private boolean allowSessionCreation = true;
	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

	public EulerUrlAuthenticationFailureHandler() {
	}

	public EulerUrlAuthenticationFailureHandler(String failureParam) {
		setFailureParam(failureParam);
	}

	/**
	 * Performs the redirect or forward to the {@code defaultFailureUrl} if set, otherwise
	 * returns a 401 error code.
	 * <p>
	 * If redirecting or forwarding, {@code saveException} will be called to cache the
	 * exception for use in the target view.
	 */
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

		if (forwardToDestination) {
			logger.debug("Forwarding to " + redirectUrl);

			request.getRequestDispatcher(redirectUrl)
					.forward(request, response);
		}
		else {
			logger.debug("Redirecting to " + redirectUrl);
			redirectStrategy.sendRedirect(request, response, redirectUrl);
		}
	}

	/**
	 * Caches the {@code AuthenticationException} for use in view rendering.
	 * <p>
	 * If {@code forwardToDestination} is set to true, request scope will be used,
	 * otherwise it will attempt to store the exception in the session. If there is no
	 * session and {@code allowSessionCreation} is {@code true} a session will be created.
	 * Otherwise the exception will not be stored.
	 */
	protected final void saveException(HttpServletRequest request,
			AuthenticationException exception) {
		if (forwardToDestination) {
			request.setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, exception);
		}
		else {
			HttpSession session = request.getSession(false);

			if (session != null || allowSessionCreation) {
				request.getSession().setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION,
						exception);
			}
		}
	}

	/**
	 * The URL which will be used as the failure destination.
	 *
	 * @param defaultFailureUrl the failure URL, for example "/loginFailed.jsp".
	 */
	public void setFailureParam(String defaultFailureParam) {
//		Assert.isTrue(UrlUtils.isValidRedirectUrl(defaultFailureUrl), "'"
//				+ defaultFailureUrl + "' is not a valid redirect URL");
		this.failureParam = defaultFailureParam;
	}

	protected boolean isUseForward() {
		return forwardToDestination;
	}

	/**
	 * If set to <tt>true</tt>, performs a forward to the failure destination URL instead
	 * of a redirect. Defaults to <tt>false</tt>.
	 */
	public void setUseForward(boolean forwardToDestination) {
		this.forwardToDestination = forwardToDestination;
	}

	/**
	 * Allows overriding of the behaviour when redirecting to a target URL.
	 */
	public void setRedirectStrategy(RedirectStrategy redirectStrategy) {
		this.redirectStrategy = redirectStrategy;
	}

	protected RedirectStrategy getRedirectStrategy() {
		return redirectStrategy;
	}

	protected boolean isAllowSessionCreation() {
		return allowSessionCreation;
	}

	public void setAllowSessionCreation(boolean allowSessionCreation) {
		this.allowSessionCreation = allowSessionCreation;
	}
}
