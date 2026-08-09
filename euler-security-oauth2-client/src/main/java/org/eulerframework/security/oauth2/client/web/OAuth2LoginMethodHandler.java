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
package org.eulerframework.security.oauth2.client.web;

import jakarta.servlet.http.HttpServletRequest;
import org.eulerframework.security.web.login.LoginMethod;
import org.eulerframework.security.web.login.LoginMethodDispatch;
import org.eulerframework.security.web.login.LoginMethodHandler;
import org.eulerframework.security.web.login.RegisteredLoginMethod;
import org.eulerframework.security.web.login.RegisteredOAuth2LoginMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * {@link LoginMethodHandler} for {@code type: oauth2}: offers a
 * registered login method as a "Sign in with IdP" option, provided the
 * referenced {@code ClientRegistration} resolves.
 *
 * <p>Provider resolution: the registration's declared
 * {@link RegisteredOAuth2LoginMethod#getProvider() provider}, else its
 * {@code identity-type}. Registration ID resolution: the declared
 * {@link RegisteredOAuth2LoginMethod#getOauthClientRegistrationId()
 * registration id}, else the provider (no fallback to the declaration
 * key).
 *
 * <p>Dispatch redirects to the authorization endpoint of the resolved
 * registration. The endpoint URL is not published to clients; a client
 * either posts to the login-method dispatch endpoint or initiates the
 * flow on its own.
 *
 * <p>The cross-type policy fields ({@code identity-type},
 * just-in-time provisioning) are consumed by
 * {@code OAuth2LoginPrincipalPromotingSuccessHandler}, not here.
 */
public class OAuth2LoginMethodHandler implements LoginMethodHandler {

    private static final String TYPE = RegisteredOAuth2LoginMethod.TYPE;

    /**
     * Attribute key the resolved provider is published under.
     */
    public static final String ATTR_PROVIDER = "provider";

    public static final String DEFAULT_AUTHORIZATION_REQUEST_BASE_URI =
            OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final String authorizationRequestBaseUri;

    public OAuth2LoginMethodHandler(ClientRegistrationRepository clientRegistrationRepository) {
        this(clientRegistrationRepository, DEFAULT_AUTHORIZATION_REQUEST_BASE_URI);
    }

    public OAuth2LoginMethodHandler(ClientRegistrationRepository clientRegistrationRepository,
                                    String authorizationRequestBaseUri) {
        Assert.notNull(clientRegistrationRepository, "clientRegistrationRepository is required");
        Assert.hasText(authorizationRequestBaseUri, "authorizationRequestBaseUri is required");
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.authorizationRequestBaseUri = authorizationRequestBaseUri.endsWith("/")
                ? authorizationRequestBaseUri.substring(0, authorizationRequestBaseUri.length() - 1)
                : authorizationRequestBaseUri;
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public LoginMethod describe(RegisteredLoginMethod method) {
        String identityType = method.getIdentityType();
        if (identityType == null || identityType.isEmpty()) {
            this.logger.warn("Login method '{}' (type=oauth2) has no identity-type; skipping.",
                    method.getName());
            return null;
        }

        RegisteredOAuth2LoginMethod oauth2Method = (RegisteredOAuth2LoginMethod) method;
        String provider = resolveProvider(oauth2Method);
        String registrationId = resolveRegistrationId(oauth2Method, provider);

        if (this.clientRegistrationRepository.findByRegistrationId(registrationId) == null) {
            this.logger.warn("Login method '{}' references OAuth client registration '{}' "
                    + "which is not defined under "
                    + "spring.security.oauth2.client.registration.*; skipping.",
                    method.getName(), registrationId);
            return null;
        }

        return LoginMethod.withType(TYPE)
                .name(method.getName())
                .primary(method.isPrimary())
                .attribute(ATTR_PROVIDER, provider)
                .build();
    }

    @Override
    public LoginMethodDispatch dispatch(RegisteredLoginMethod method, HttpServletRequest request) {
        RegisteredOAuth2LoginMethod oauth2Method = (RegisteredOAuth2LoginMethod) method;
        String provider = resolveProvider(oauth2Method);
        String registrationId = resolveRegistrationId(oauth2Method, provider);
        return LoginMethodDispatch.redirect(this.authorizationRequestBaseUri + "/" + registrationId);
    }

    /**
     * Resolves the provider a registration federates to: its declared
     * value, else the identity type.
     */
    public static String resolveProvider(RegisteredOAuth2LoginMethod method) {
        if (StringUtils.hasText(method.getProvider())) {
            return method.getProvider();
        }
        return method.getIdentityType();
    }

    /**
     * Resolves the {@code ClientRegistration} a registration uses: its
     * declared value, else the provider. Never falls back to the
     * declaration key.
     */
    public static String resolveRegistrationId(RegisteredOAuth2LoginMethod method, String provider) {
        if (StringUtils.hasText(method.getOauthClientRegistrationId())) {
            return method.getOauthClientRegistrationId();
        }
        return provider;
    }
}
