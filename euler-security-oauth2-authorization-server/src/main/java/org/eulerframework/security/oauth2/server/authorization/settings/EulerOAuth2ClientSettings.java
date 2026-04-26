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

package org.eulerframework.security.oauth2.server.authorization.settings;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.oauth2.server.authorization.settings.AbstractSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.ConfigurationSettingNames;
import org.springframework.util.Assert;

/**
 * Non-RFC extension settings for an OAuth 2.0 client.
 *
 * <p>These properties are <b>not</b> part of the
 * <a href="https://datatracker.ietf.org/doc/html/rfc7591">RFC 7591</a> client metadata;
 * RFC 7591 fields are exposed directly on
 * {@link org.eulerframework.security.oauth2.server.authorization.client.EulerOAuth2Client}.
 *
 * <p>RFC-related properties that have been promoted to
 * {@link org.eulerframework.security.oauth2.server.authorization.client.EulerOAuth2Client}
 * (such as {@code jwks_uri}, {@code token_endpoint_auth_signing_alg},
 * {@code tls_client_auth_subject_dn}) are <b>excluded</b> from this settings class.
 *
 * @see ClientSettings
 * @see AbstractSettings
 */
public final class EulerOAuth2ClientSettings extends AbstractSettings {

    private EulerOAuth2ClientSettings(Map<String, Object> settings) {
        super(settings);
    }

    /**
     * Returns whether the client is required to provide a proof key challenge
     * and verifier when performing the Authorization Code Grant flow.
     *
     * @return {@code true} if a proof key is required, {@code null} if unspecified
     */
    public Boolean getRequireProofKey() {
        return getSetting(ConfigurationSettingNames.Client.REQUIRE_PROOF_KEY);
    }

    /**
     * Returns whether authorization consent is required when the client requests access.
     *
     * @return {@code true} if authorization consent is required, {@code null} if unspecified
     */
    public Boolean getRequireAuthorizationConsent() {
        return getSetting(ConfigurationSettingNames.Client.REQUIRE_AUTHORIZATION_CONSENT);
    }

    /**
     * Creates an {@link EulerOAuth2ClientSettings} from a Spring {@link ClientSettings},
     * copying only Euler-specific (non-RFC) properties.
     *
     * <p>RFC properties ({@code jwks_uri}, {@code token_endpoint_auth_signing_alg},
     * {@code tls_client_auth_subject_dn}) are excluded because they have been promoted to
     * {@link org.eulerframework.security.oauth2.server.authorization.client.EulerOAuth2Client}.
     *
     * @param clientSettings the Spring client settings to read from
     * @return the Euler client settings
     */
    public static EulerOAuth2ClientSettings from(ClientSettings clientSettings) {
        Assert.notNull(clientSettings, "clientSettings cannot be null");
        return withSettings(clientSettings.getSettings()).build();
    }

    /**
     * Constructs a new {@link Builder}.
     *
     * @return the {@link Builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Constructs a new {@link Builder} with the provided settings.
     *
     * @param settings the settings to initialize the builder
     * @return the {@link Builder}
     */
    public static Builder withSettings(Map<String, Object> settings) {
        Assert.notEmpty(settings, "settings cannot be empty");
        return new Builder().settings(s -> s.putAll(settings));
    }

    /**
     * A builder for {@link EulerOAuth2ClientSettings}.
     */
    public static final class Builder extends AbstractBuilder<EulerOAuth2ClientSettings, Builder> {

        private Builder() {
        }

        /**
         * Set whether the client is required to provide a proof key challenge and verifier.
         *
         * @param requireProofKey {@code true} if required, {@code null} if unspecified
         * @return the {@link Builder} for further configuration
         */
        public Builder requireProofKey(Boolean requireProofKey) {
            if (requireProofKey != null) {
                return setting(ConfigurationSettingNames.Client.REQUIRE_PROOF_KEY, requireProofKey);
            }
            return getThis();
        }

        /**
         * Set whether authorization consent is required when the client requests access.
         *
         * @param requireAuthorizationConsent {@code true} if required, {@code null} if unspecified
         * @return the {@link Builder} for further configuration
         */
        public Builder requireAuthorizationConsent(Boolean requireAuthorizationConsent) {
            if (requireAuthorizationConsent != null) {
                return setting(ConfigurationSettingNames.Client.REQUIRE_AUTHORIZATION_CONSENT, requireAuthorizationConsent);
            }
            return getThis();
        }

        /**
         * Builds the {@link EulerOAuth2ClientSettings}.
         *
         * @return the {@link EulerOAuth2ClientSettings}
         */
        @Override
        public EulerOAuth2ClientSettings build() {
            Map<String, Object> settings = getSettings();
            if (settings.get(ConfigurationSettingNames.Client.REQUIRE_PROOF_KEY) == null) {
                setting(ConfigurationSettingNames.Client.REQUIRE_PROOF_KEY, true);
            }

            if (settings.get(ConfigurationSettingNames.Client.REQUIRE_AUTHORIZATION_CONSENT) == null) {
                setting(ConfigurationSettingNames.Client.REQUIRE_AUTHORIZATION_CONSENT, false);
            }

            // Remove RFC properties promoted to EulerOAuth2Client
            settings.remove(ConfigurationSettingNames.Client.JWK_SET_URL);
            settings.remove(ConfigurationSettingNames.Client.TOKEN_ENDPOINT_AUTHENTICATION_SIGNING_ALGORITHM);
            settings.remove(ConfigurationSettingNames.Client.X509_CERTIFICATE_SUBJECT_DN);

            return new EulerOAuth2ClientSettings(settings);
        }

    }

}
