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

import org.eulerframework.security.web.endpoint.user.login.LoginMethodTypeHandler;
import org.eulerframework.security.web.endpoint.user.login.LoginMethodView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * {@link LoginMethodTypeHandler} for {@code type: oauth2}: turns a
 * declaration under
 * {@code euler.security.web.login-methods.<name>} into a
 * "Sign in with &lt;IdP&gt;" button by resolving the referenced
 * {@link ClientRegistration} from
 * {@link ClientRegistrationRepository}.
 *
 * <h2>Supported {@code properties}</h2>
 * <ul>
 *   <li>{@link #PROP_OAUTH_CLIENT_REGISTRATION_ID}
 *       (default: the login-method key) &mdash; must equal the
 *       {@code registrationId} of an entry under
 *       {@code spring.security.oauth2.client.registration.<key>}. No
 *       field overriding &mdash; this is a pure reference.</li>
 *   <li>{@link #PROP_DISPLAY_NAME} (default:
 *       {@link ClientRegistration#getClientName()}) &mdash; label
 *       rendered next to the {@code _SIGN_IN_WITH} i18n message.</li>
 *   <li>{@link #PROP_ICON_CLASS} (default:
 *       {@code btn-oauth2-<oauthClientRegistrationId>}) &mdash; CSS
 *       class hook for provider artwork.</li>
 * </ul>
 * The other well-known properties ({@code auto-create-user},
 * {@code default-authorities}, {@code identity-type}) are login-side
 * policy and are consumed by
 * {@code OAuth2LoginPrincipalPromotingSuccessHandler} rather than by
 * this handler.
 */
public class OAuth2LoginMethodTypeHandler implements LoginMethodTypeHandler {

    /** Discriminator value under {@code login-methods.<name>.type}. */
    public static final String TYPE = LoginMethodView.TYPE_OAUTH2;

    /**
     * Property key whose value equals the
     * {@code registrationId} of the referenced
     * {@code spring.security.oauth2.client.registration.<key>} entry.
     */
    public static final String PROP_OAUTH_CLIENT_REGISTRATION_ID = "oauth-client-registration-id";

    /** Property key for the login-page display label override. */
    public static final String PROP_DISPLAY_NAME = "display-name";

    /** Property key for the login-page icon CSS class override. */
    public static final String PROP_ICON_CLASS = "icon-class";

    /** Base URI for authorization requests, matches Spring Security's default. */
    public static final String DEFAULT_AUTHORIZATION_REQUEST_BASE_URI =
            OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final String authorizationRequestBaseUri;

    public OAuth2LoginMethodTypeHandler(ClientRegistrationRepository clientRegistrationRepository) {
        this(clientRegistrationRepository, DEFAULT_AUTHORIZATION_REQUEST_BASE_URI);
    }

    public OAuth2LoginMethodTypeHandler(ClientRegistrationRepository clientRegistrationRepository,
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
    public LoginMethodView toView(String name, Map<String, Object> properties) {
        String oauthClientRegistrationId = asString(properties, PROP_OAUTH_CLIENT_REGISTRATION_ID);
        if (oauthClientRegistrationId == null || oauthClientRegistrationId.isEmpty()) {
            // Fall back to the login-method key: the common case is
            // "the login method name IS the registrationId".
            oauthClientRegistrationId = name;
        }
        ClientRegistration registration =
                this.clientRegistrationRepository.findByRegistrationId(oauthClientRegistrationId);
        if (registration == null) {
            this.logger.warn("Login method '{}' references OAuth client registration '{}' "
                    + "which is not defined under "
                    + "spring.security.oauth2.client.registration.*; the login button will not be rendered.",
                    name, oauthClientRegistrationId);
            return null;
        }
        String displayName = asString(properties, PROP_DISPLAY_NAME);
        if (displayName == null || displayName.isEmpty()) {
            displayName = registration.getClientName();
            if (displayName == null || displayName.isEmpty()) {
                displayName = oauthClientRegistrationId;
            }
        }
        String iconClass = asString(properties, PROP_ICON_CLASS);
        if (iconClass == null || iconClass.isEmpty()) {
            iconClass = "btn-oauth2-" + oauthClientRegistrationId;
        }
        return LoginMethodView.builder(LoginMethodView.TYPE_OAUTH2)
                .id(oauthClientRegistrationId)
                .href(this.authorizationRequestBaseUri + "/" + oauthClientRegistrationId)
                .displayName(displayName)
                .iconClass(iconClass)
                .build();
    }

    private static String asString(Map<String, Object> properties, String key) {
        if (properties == null) {
            return null;
        }
        Object value = properties.get(key);
        return value == null ? null : value.toString();
    }
}
