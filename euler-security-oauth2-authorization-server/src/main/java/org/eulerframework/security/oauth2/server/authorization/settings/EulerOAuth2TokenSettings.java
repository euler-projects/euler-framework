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

import java.time.Duration;
import java.util.Map;

import org.springframework.security.oauth2.server.authorization.settings.AbstractSettings;
import org.springframework.security.oauth2.server.authorization.settings.ConfigurationSettingNames;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.util.Assert;

/**
 * Non-RFC extension token settings for an OAuth 2.0 client.
 *
 * <p>All time-to-live values are expressed in <b>seconds</b> for JSON-friendly serialization.
 *
 * <p>RFC-related properties that have been promoted to
 * {@link org.eulerframework.security.oauth2.server.authorization.client.EulerOAuth2Client}
 * (such as {@code id_token_signed_response_alg},
 * {@code tls_client_certificate_bound_access_tokens}) are <b>excluded</b> from this
 * settings class.
 *
 * @see TokenSettings
 * @see AbstractSettings
 */
public final class EulerOAuth2TokenSettings extends AbstractSettings {

    private EulerOAuth2TokenSettings(Map<String, Object> settings) {
        super(settings);
    }

    /**
     * Returns the time-to-live for an authorization code in seconds.
     *
     * @return seconds, or {@code null} if unspecified
     */
    public Long getAuthorizationCodeTimeToLive() {
        return getSetting(ConfigurationSettingNames.Token.AUTHORIZATION_CODE_TIME_TO_LIVE);
    }

    /**
     * Returns the time-to-live for an access token in seconds.
     *
     * @return seconds, or {@code null} if unspecified
     */
    public Long getAccessTokenTimeToLive() {
        return getSetting(ConfigurationSettingNames.Token.ACCESS_TOKEN_TIME_TO_LIVE);
    }

    /**
     * Returns the token format for an access token (e.g. "self-contained", "reference").
     *
     * @return the format string, or {@code null} if unspecified
     */
    public String getAccessTokenFormat() {
        return getSetting(ConfigurationSettingNames.Token.ACCESS_TOKEN_FORMAT);
    }

    /**
     * Returns the time-to-live for a device code in seconds.
     *
     * @return seconds, or {@code null} if unspecified
     */
    public Long getDeviceCodeTimeToLive() {
        return getSetting(ConfigurationSettingNames.Token.DEVICE_CODE_TIME_TO_LIVE);
    }

    /**
     * Returns whether refresh tokens are reused when returning the access token response.
     *
     * @return {@code true} to reuse, {@code false} to issue new, {@code null} if unspecified
     */
    public Boolean getReuseRefreshTokens() {
        return getSetting(ConfigurationSettingNames.Token.REUSE_REFRESH_TOKENS);
    }

    /**
     * Returns the time-to-live for a refresh token in seconds.
     *
     * @return seconds, or {@code null} if unspecified
     */
    public Long getRefreshTokenTimeToLive() {
        return getSetting(ConfigurationSettingNames.Token.REFRESH_TOKEN_TIME_TO_LIVE);
    }

    /**
     * Creates an {@link EulerOAuth2TokenSettings} from a Spring {@link TokenSettings},
     * copying only Euler-specific (non-RFC) properties.
     *
     * <p>RFC properties ({@code id_token_signed_response_alg},
     * {@code tls_client_certificate_bound_access_tokens}) are excluded because they have
     * been promoted to
     * {@link org.eulerframework.security.oauth2.server.authorization.client.EulerOAuth2Client}.
     *
     * @param tokenSettings the Spring token settings to read from
     * @return the Euler token settings
     */
    public static EulerOAuth2TokenSettings from(TokenSettings tokenSettings) {
        Assert.notNull(tokenSettings, "tokenSettings cannot be null");
        return withSettings(tokenSettings.getSettings()).build();
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
     * Constructs a new {@link Builder} with the provided settings,
     * removing RFC properties promoted to {@code EulerOAuth2Client}
     * and converting Spring-native types to Euler-native types
     * for properties with defined getters.
     *
     * @param settings the settings to initialize the builder
     * @return the {@link Builder}
     */
    public static Builder withSettings(Map<String, Object> settings) {
        Assert.notEmpty(settings, "settings cannot be empty");
        return new Builder().settings(s -> s.putAll(settings));
    }

    /**
     * A builder for {@link EulerOAuth2TokenSettings}.
     */
    public static final class Builder extends AbstractBuilder<EulerOAuth2TokenSettings, Builder> {

        private Builder() {
        }

        /**
         * Set the time-to-live for an authorization code in seconds.
         *
         * @param authorizationCodeTimeToLive seconds, {@code null} if unspecified
         * @return the {@link Builder} for further configuration
         */
        public Builder authorizationCodeTimeToLive(Long authorizationCodeTimeToLive) {
            if (authorizationCodeTimeToLive != null) {
                Assert.isTrue(authorizationCodeTimeToLive > 0,
                        "authorizationCodeTimeToLive must be greater than 0");
                return setting(ConfigurationSettingNames.Token.AUTHORIZATION_CODE_TIME_TO_LIVE,
                        authorizationCodeTimeToLive);
            }
            return getThis();
        }

        /**
         * Set the time-to-live for an access token in seconds.
         *
         * @param accessTokenTimeToLive seconds, {@code null} if unspecified
         * @return the {@link Builder} for further configuration
         */
        public Builder accessTokenTimeToLive(Long accessTokenTimeToLive) {
            if (accessTokenTimeToLive != null) {
                Assert.isTrue(accessTokenTimeToLive > 0,
                        "accessTokenTimeToLive must be greater than 0");
                return setting(ConfigurationSettingNames.Token.ACCESS_TOKEN_TIME_TO_LIVE,
                        accessTokenTimeToLive);
            }
            return getThis();
        }

        /**
         * Set the token format for an access token.
         *
         * @param accessTokenFormat the format string, {@code null} if unspecified
         * @return the {@link Builder} for further configuration
         */
        public Builder accessTokenFormat(String accessTokenFormat) {
            if (accessTokenFormat != null) {
                return setting(ConfigurationSettingNames.Token.ACCESS_TOKEN_FORMAT,
                        accessTokenFormat);
            }
            return getThis();
        }

        /**
         * Set the time-to-live for a device code in seconds.
         *
         * @param deviceCodeTimeToLive seconds, {@code null} if unspecified
         * @return the {@link Builder} for further configuration
         */
        public Builder deviceCodeTimeToLive(Long deviceCodeTimeToLive) {
            if (deviceCodeTimeToLive != null) {
                Assert.isTrue(deviceCodeTimeToLive > 0,
                        "deviceCodeTimeToLive must be greater than 0");
                return setting(ConfigurationSettingNames.Token.DEVICE_CODE_TIME_TO_LIVE,
                        deviceCodeTimeToLive);
            }
            return getThis();
        }

        /**
         * Set whether refresh tokens are reused when returning the access token response.
         *
         * @param reuseRefreshTokens {@code true} to reuse, {@code false} to issue new,
         *                           {@code null} if unspecified
         * @return the {@link Builder} for further configuration
         */
        public Builder reuseRefreshTokens(Boolean reuseRefreshTokens) {
            if (reuseRefreshTokens != null) {
                return setting(ConfigurationSettingNames.Token.REUSE_REFRESH_TOKENS, reuseRefreshTokens);
            }
            return getThis();
        }

        /**
         * Set the time-to-live for a refresh token in seconds.
         *
         * @param refreshTokenTimeToLive seconds, {@code null} if unspecified
         * @return the {@link Builder} for further configuration
         */
        public Builder refreshTokenTimeToLive(Long refreshTokenTimeToLive) {
            if (refreshTokenTimeToLive != null) {
                Assert.isTrue(refreshTokenTimeToLive > 0,
                        "refreshTokenTimeToLive must be greater than 0");
                return setting(ConfigurationSettingNames.Token.REFRESH_TOKEN_TIME_TO_LIVE,
                        refreshTokenTimeToLive);
            }
            return getThis();
        }

        /**
         * Builds the {@link EulerOAuth2TokenSettings}.
         *
         * @return the {@link EulerOAuth2TokenSettings}
         */
        @Override
        public EulerOAuth2TokenSettings build() {
            Map<String, Object> settings = getSettings();
            if (settings.get(ConfigurationSettingNames.Token.AUTHORIZATION_CODE_TIME_TO_LIVE) == null) {
                setting(ConfigurationSettingNames.Token.AUTHORIZATION_CODE_TIME_TO_LIVE, Duration.ofMinutes(5).getSeconds());
            } else {
                convertDurationToSeconds(settings, ConfigurationSettingNames.Token.AUTHORIZATION_CODE_TIME_TO_LIVE);
            }

            if (settings.get(ConfigurationSettingNames.Token.ACCESS_TOKEN_TIME_TO_LIVE) == null) {
                setting(ConfigurationSettingNames.Token.ACCESS_TOKEN_TIME_TO_LIVE, Duration.ofMinutes(5).getSeconds());
            } else {
                convertDurationToSeconds(settings, ConfigurationSettingNames.Token.ACCESS_TOKEN_TIME_TO_LIVE);
            }

            if (settings.get(ConfigurationSettingNames.Token.ACCESS_TOKEN_FORMAT) == null) {
                setting(ConfigurationSettingNames.Token.ACCESS_TOKEN_FORMAT, OAuth2TokenFormat.SELF_CONTAINED.getValue());
            } else {
                convertOAuth2TokenFormatToString(settings, ConfigurationSettingNames.Token.ACCESS_TOKEN_FORMAT);
            }

            if (settings.get(ConfigurationSettingNames.Token.DEVICE_CODE_TIME_TO_LIVE) == null) {
                setting(ConfigurationSettingNames.Token.DEVICE_CODE_TIME_TO_LIVE, Duration.ofMinutes(5).getSeconds());
            } else {
                convertDurationToSeconds(settings, ConfigurationSettingNames.Token.DEVICE_CODE_TIME_TO_LIVE);
            }

            if (settings.get(ConfigurationSettingNames.Token.REUSE_REFRESH_TOKENS) == null) {
                setting(ConfigurationSettingNames.Token.REUSE_REFRESH_TOKENS, true);
            }

            if (settings.get(ConfigurationSettingNames.Token.REFRESH_TOKEN_TIME_TO_LIVE) == null) {
                setting(ConfigurationSettingNames.Token.REFRESH_TOKEN_TIME_TO_LIVE, Duration.ofMinutes(60).getSeconds());
            } else {
                convertDurationToSeconds(settings, ConfigurationSettingNames.Token.REFRESH_TOKEN_TIME_TO_LIVE);
            }


            // Remove RFC properties promoted to EulerOAuth2Client
            settings.remove(ConfigurationSettingNames.Token.ID_TOKEN_SIGNATURE_ALGORITHM);
            settings.remove(ConfigurationSettingNames.Token.X509_CERTIFICATE_BOUND_ACCESS_TOKENS);

            return new EulerOAuth2TokenSettings(settings);
        }

    }

    private static void convertDurationToSeconds(Map<String, Object> settings, String key) {
        Object value = settings.get(key);
        if (value instanceof Duration d) {
            settings.put(key, d.getSeconds());
        }
    }

    private static void convertOAuth2TokenFormatToString(Map<String, Object> settings, String key) {
        Object value = settings.get(key);
        if (value instanceof OAuth2TokenFormat f) {
            settings.put(key, f.getValue());
        }
    }
}
