/*
 * Copyright 2013-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eulerframework.security.config.annotation.web.configurers.oauth2.server.authorization;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.web.OAuth2ClientAuthenticationFilter;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.DelegatingAuthenticationConverter;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Configurer for OAuth 2.0 Client Authentication on an endpoint of this framework's own. It builds the
 * same {@link OAuth2ClientAuthenticationFilter} that Spring's {@code OAuth2ClientAuthenticationConfigurer}
 * does, but differs in two deliberate ways:
 * <ul>
 *   <li>the {@link RequestMatcher} is {@linkplain #requestMatcher(RequestMatcher) supplied by the caller}
 *       rather than fixed to the token-facing endpoints, so it can guard a custom endpoint such as the
 *       authorization challenge endpoint; and</li>
 *   <li>it contributes <b>no</b> default {@link AuthenticationConverter}s or {@link AuthenticationProvider}s
 *       &mdash; both are entirely {@linkplain #authenticationConverter(AuthenticationConverter) supplied by
 *       the caller}.</li>
 * </ul>
 * The second point is what keeps this from colliding with Spring's client authentication: the providers
 * live in the authorization server's shared {@link AuthenticationManager}, so re-registering Spring's
 * defaults here would duplicate them. A caller that only needs, say, attestation-based client
 * authentication adds just that converter and relies on the provider already registered for the token
 * endpoint; the resulting filter authenticates through the same shared manager.
 *
 * @see OAuth2ClientAuthenticationFilter
 * @see AuthorizationChallengeEndpointConfigurer
 */
public final class EulerOAuth2ClientAuthenticationConfigurer extends AbstractEulerOAuth2Configurer {

	private RequestMatcher requestMatcher;

	private final List<AuthenticationConverter> authenticationConverters = new ArrayList<>();

	private Consumer<List<AuthenticationConverter>> authenticationConvertersConsumer = (authenticationConverters) -> {
	};

	private final List<AuthenticationProvider> authenticationProviders = new ArrayList<>();

	private Consumer<List<AuthenticationProvider>> authenticationProvidersConsumer = (authenticationProviders) -> {
	};

	private AuthenticationSuccessHandler authenticationSuccessHandler;

	private AuthenticationFailureHandler errorResponseHandler;

	/**
	 * Restrict for internal use only.
	 * @param objectPostProcessor an {@code ObjectPostProcessor}
	 */
	EulerOAuth2ClientAuthenticationConfigurer(ObjectPostProcessor<Object> objectPostProcessor) {
		super(objectPostProcessor);
	}

	/**
	 * Sets the {@link RequestMatcher} that decides which requests this client authentication applies to.
	 * This is a point of departure from Spring's {@code OAuth2ClientAuthenticationConfigurer}, which fixes
	 * the matcher to its token-facing endpoints.
	 * @param requestMatcher the {@link RequestMatcher} for the endpoint(s) to protect
	 * @return the {@link EulerOAuth2ClientAuthenticationConfigurer} for further configuration
	 */
	public EulerOAuth2ClientAuthenticationConfigurer requestMatcher(RequestMatcher requestMatcher) {
		Assert.notNull(requestMatcher, "requestMatcher cannot be null");
		this.requestMatcher = requestMatcher;
		return this;
	}

	/**
	 * Adds an {@link AuthenticationConverter} used when attempting to extract client
	 * credentials from {@link HttpServletRequest} to an instance of
	 * {@link OAuth2ClientAuthenticationToken} used for authenticating the client.
	 * @param authenticationConverter an {@link AuthenticationConverter} used when
	 * attempting to extract client credentials from {@link HttpServletRequest}
	 * @return the {@link EulerOAuth2ClientAuthenticationConfigurer} for further configuration
	 */
	public EulerOAuth2ClientAuthenticationConfigurer authenticationConverter(
			AuthenticationConverter authenticationConverter) {
		Assert.notNull(authenticationConverter, "authenticationConverter cannot be null");
		this.authenticationConverters.add(authenticationConverter);
		return this;
	}

	/**
	 * Sets the {@code Consumer} providing access to the {@code List} of (externally) added
	 * {@link #authenticationConverter(AuthenticationConverter) AuthenticationConverter}'s allowing the
	 * ability to add, remove, or customize a specific {@link AuthenticationConverter}.
	 * @param authenticationConvertersConsumer the {@code Consumer} providing access to the {@code List} of
	 * added {@link AuthenticationConverter}'s
	 * @return the {@link EulerOAuth2ClientAuthenticationConfigurer} for further configuration
	 */
	public EulerOAuth2ClientAuthenticationConfigurer authenticationConverters(
			Consumer<List<AuthenticationConverter>> authenticationConvertersConsumer) {
		Assert.notNull(authenticationConvertersConsumer, "authenticationConvertersConsumer cannot be null");
		this.authenticationConvertersConsumer = authenticationConvertersConsumer;
		return this;
	}

	/**
	 * Adds an {@link AuthenticationProvider} used for authenticating an
	 * {@link OAuth2ClientAuthenticationToken}. Only needed when the provider is not already registered in
	 * the shared {@link AuthenticationManager}; a provider contributed elsewhere (e.g. for the token
	 * endpoint) is reused as-is.
	 * @param authenticationProvider an {@link AuthenticationProvider} used for
	 * authenticating an {@link OAuth2ClientAuthenticationToken}
	 * @return the {@link EulerOAuth2ClientAuthenticationConfigurer} for further configuration
	 */
	public EulerOAuth2ClientAuthenticationConfigurer authenticationProvider(AuthenticationProvider authenticationProvider) {
		Assert.notNull(authenticationProvider, "authenticationProvider cannot be null");
		this.authenticationProviders.add(authenticationProvider);
		return this;
	}

	/**
	 * Sets the {@code Consumer} providing access to the {@code List} of (externally) added
	 * {@link #authenticationProvider(AuthenticationProvider) AuthenticationProvider}'s allowing the ability
	 * to add, remove, or customize a specific {@link AuthenticationProvider}.
	 * @param authenticationProvidersConsumer the {@code Consumer} providing access to the {@code List} of
	 * added {@link AuthenticationProvider}'s
	 * @return the {@link EulerOAuth2ClientAuthenticationConfigurer} for further configuration
	 */
	public EulerOAuth2ClientAuthenticationConfigurer authenticationProviders(
			Consumer<List<AuthenticationProvider>> authenticationProvidersConsumer) {
		Assert.notNull(authenticationProvidersConsumer, "authenticationProvidersConsumer cannot be null");
		this.authenticationProvidersConsumer = authenticationProvidersConsumer;
		return this;
	}

	/**
	 * Sets the {@link AuthenticationSuccessHandler} used for handling a successful client
	 * authentication and associating the {@link OAuth2ClientAuthenticationToken} to the
	 * {@link SecurityContext}.
	 * @param authenticationSuccessHandler the {@link AuthenticationSuccessHandler} used
	 * for handling a successful client authentication
	 * @return the {@link EulerOAuth2ClientAuthenticationConfigurer} for further configuration
	 */
	public EulerOAuth2ClientAuthenticationConfigurer authenticationSuccessHandler(
			AuthenticationSuccessHandler authenticationSuccessHandler) {
		this.authenticationSuccessHandler = authenticationSuccessHandler;
		return this;
	}

	/**
	 * Sets the {@link AuthenticationFailureHandler} used for handling a failed client
	 * authentication and returning the {@link OAuth2Error Error Response}.
	 * @param errorResponseHandler the {@link AuthenticationFailureHandler} used for
	 * handling a failed client authentication
	 * @return the {@link EulerOAuth2ClientAuthenticationConfigurer} for further configuration
	 */
	public EulerOAuth2ClientAuthenticationConfigurer errorResponseHandler(
			AuthenticationFailureHandler errorResponseHandler) {
		this.errorResponseHandler = errorResponseHandler;
		return this;
	}

	@Override
	void init(HttpSecurity httpSecurity) {
		Assert.notNull(this.requestMatcher, "requestMatcher must be configured");
		// No default providers: register only what the caller contributed. Providers already registered in
		// the shared AuthenticationManager (e.g. the App Attest provider contributed for the token endpoint)
		// are reused by the filter and must not be re-registered here.
		List<AuthenticationProvider> authenticationProviders = new ArrayList<>(this.authenticationProviders);
		this.authenticationProvidersConsumer.accept(authenticationProviders);
		authenticationProviders.forEach(
				(authenticationProvider) -> httpSecurity.authenticationProvider(postProcess(authenticationProvider)));
	}

	@Override
	void configure(HttpSecurity httpSecurity) {
		AuthenticationManager authenticationManager = httpSecurity.getSharedObject(AuthenticationManager.class);
		OAuth2ClientAuthenticationFilter clientAuthenticationFilter = new OAuth2ClientAuthenticationFilter(
				authenticationManager, this.requestMatcher);
		// No default converters: the filter recognizes exactly the credentials the caller contributed.
		List<AuthenticationConverter> authenticationConverters = new ArrayList<>(this.authenticationConverters);
		this.authenticationConvertersConsumer.accept(authenticationConverters);
		Assert.notEmpty(authenticationConverters, "At least one authenticationConverter must be configured");
		clientAuthenticationFilter
			.setAuthenticationConverter(new DelegatingAuthenticationConverter(authenticationConverters));
		if (this.authenticationSuccessHandler != null) {
			clientAuthenticationFilter.setAuthenticationSuccessHandler(this.authenticationSuccessHandler);
		}
		if (this.errorResponseHandler != null) {
			clientAuthenticationFilter.setAuthenticationFailureHandler(this.errorResponseHandler);
		}
		httpSecurity.addFilterAfter(postProcess(clientAuthenticationFilter),
				AbstractPreAuthenticatedProcessingFilter.class);
	}

	@Override
	RequestMatcher getRequestMatcher() {
		return this.requestMatcher;
	}

}
