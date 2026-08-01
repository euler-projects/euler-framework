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
import org.eulerframework.security.web.endpoint.user.login.LoginMethod;
import org.eulerframework.security.web.endpoint.user.login.LoginMethodDispatch;
import org.eulerframework.security.web.endpoint.user.login.LoginMethodHandler;
import org.eulerframework.security.web.endpoint.user.login.RegisteredLoginMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * {@link LoginMethodHandler} for {@code type: oauth2}: offers a
 * registered login method as a "Sign in with IdP" option, provided the
 * referenced {@code ClientRegistration} resolves.
 *
 * <p>Provider resolution: {@code properties.provider} explicit value,
 * else {@code identity-type}. Registration ID resolution:
 * {@code properties.oauth-client-registration-id} explicit value, else
 * provider (no fallback to key).
 *
 * <p>Dispatch redirects to the authorization endpoint of the resolved
 * registration. The endpoint URL is not published to clients; a client
 * either posts to the login-method dispatch endpoint or initiates the
 * flow on its own.
 *
 * <p>The top-level policy fields ({@code identity-type},
 * {@code jit-provisioning}) are consumed by
 * {@code OAuth2LoginPrincipalPromotingSuccessHandler}, not here.
 */
public class OAuth2LoginMethodHandler implements LoginMethodHandler {

    /** The {@code type} value served by this handler. */
    public static final String TYPE = "oauth2";

    public static final String PROP_OAUTH_CLIENT_REGISTRATION_ID = "oauth-client-registration-id";

    /**
     * Property key overriding the provider, and the attribute key it is
     * published under.
     */
    public static final String PROP_PROVIDER = "provider";

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

    /**
     * Derives the default name from the resolved provider, allowing
     * several oauth2 registrations (one per provider) without declared
     * names.
     */
    @Override
    public String resolveName(RegisteredLoginMethod method) {
        return resolveProvider(method);
    }

    @Override
    public LoginMethod describe(String name, RegisteredLoginMethod method) {
        String identityType = method.getIdentityType();
        if (identityType == null || identityType.isEmpty()) {
            this.logger.warn("Login method '{}' (type=oauth2) has no identity-type; skipping.", name);
            return null;
        }

        String provider = resolveProvider(method);
        String registrationId = resolveRegistrationId(method, provider);

        if (this.clientRegistrationRepository.findByRegistrationId(registrationId) == null) {
            this.logger.warn("Login method '{}' references OAuth client registration '{}' "
                    + "which is not defined under "
                    + "spring.security.oauth2.client.registration.*; skipping.",
                    name, registrationId);
            return null;
        }

        return LoginMethod.withType(TYPE)
                .name(name)
                .primary(method.isPrimary())
                .attribute(PROP_PROVIDER, provider)
                .build();
    }

    @Override
    public LoginMethodDispatch dispatch(String name, RegisteredLoginMethod method, HttpServletRequest request) {
        String provider = resolveProvider(method);
        String registrationId = resolveRegistrationId(method, provider);
        return LoginMethodDispatch.redirect(this.authorizationRequestBaseUri + "/" + registrationId);
    }

    private String resolveProvider(RegisteredLoginMethod method) {
        String explicit = asString(method.getProperties(), PROP_PROVIDER);
        if (explicit != null && !explicit.isEmpty()) {
            return explicit;
        }
        return method.getIdentityType();
    }

    private String resolveRegistrationId(RegisteredLoginMethod method, String provider) {
        String explicit = asString(method.getProperties(), PROP_OAUTH_CLIENT_REGISTRATION_ID);
        if (explicit != null && !explicit.isEmpty()) {
            return explicit;
        }
        return provider;
    }

    private static String asString(Map<String, Object> properties, String key) {
        if (properties == null) {
            return null;
        }
        Object value = properties.get(key);
        return value == null ? null : value.toString();
    }
}
