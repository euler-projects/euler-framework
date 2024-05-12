package org.eulerframework.security.web.util.matcher;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class IgnoreRequestMatcher implements RequestMatcher {

	private final List<RequestMatcher> requestMatchers;

	/**
	 * Creates a new instance
	 * @param requestMatchers the {@link RequestMatcher} instances to try
	 */
	public IgnoreRequestMatcher(List<RequestMatcher> requestMatchers) {
		Assert.notEmpty(requestMatchers, "requestMatchers must contain a value");
		Assert.noNullElements(requestMatchers, "requestMatchers cannot contain null values");
		this.requestMatchers = requestMatchers;
	}

	/**
	 * Creates a new instance
	 * @param requestMatchers the {@link RequestMatcher} instances to try
	 */
	public IgnoreRequestMatcher(RequestMatcher... requestMatchers) {
		this(Arrays.asList(requestMatchers));
	}

	@Override
	public boolean matches(HttpServletRequest request) {
		for (RequestMatcher matcher : this.requestMatchers) {
			if (matcher.matches(request)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns a {@link MatchResult} for this {@link HttpServletRequest}. In the case of a
	 * match, request variables are any request variables from the first underlying
	 * matcher.
	 * @param request the HTTP request
	 * @return a {@link MatchResult} based on the given HTTP request
	 * @since 6.1
	 */
	@Override
	public MatchResult matcher(HttpServletRequest request) {
		for (RequestMatcher matcher : this.requestMatchers) {
			MatchResult result = matcher.matcher(request);
			if (result.isMatch()) {
				return MatchResult.notMatch();
			}
		}
		return MatchResult.match();
	}

	@Override
	public String toString() {
		return "Ignore " + this.requestMatchers;
	}

}
