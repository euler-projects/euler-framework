/*
 * Copyright 2013-2026 the original author or authors.
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

/**
 * Non-RFC extension token settings for an OAuth 2.0 client.
 *
 * <p>All time-to-live values are expressed in <b>seconds</b> for JSON-friendly serialization.
 */
public interface EulerOAuth2TokenSettings {

    /**
     * Returns the time-to-live for an authorization code in seconds.
     *
     * @return seconds, or {@code null} if unspecified
     */
    Long getAuthorizationCodeTimeToLive();

    /**
     * Returns the time-to-live for an access token in seconds.
     *
     * @return seconds, or {@code null} if unspecified
     */
    Long getAccessTokenTimeToLive();

    /**
     * Returns the token format for an access token (e.g. "self-contained", "reference").
     *
     * @return the format string, or {@code null} if unspecified
     */
    String getAccessTokenFormat();

    /**
     * Returns the time-to-live for a device code in seconds.
     *
     * @return seconds, or {@code null} if unspecified
     */
    Long getDeviceCodeTimeToLive();

    /**
     * Returns whether refresh tokens are reused when returning the access token response.
     *
     * @return {@code true} to reuse, {@code false} to issue new, {@code null} if unspecified
     */
    Boolean getReuseRefreshTokens();

    /**
     * Returns the time-to-live for a refresh token in seconds.
     *
     * @return seconds, or {@code null} if unspecified
     */
    Long getRefreshTokenTimeToLive();
}
