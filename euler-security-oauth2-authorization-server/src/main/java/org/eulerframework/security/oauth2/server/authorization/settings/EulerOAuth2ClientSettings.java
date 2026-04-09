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
 * Non-RFC extension settings for an OAuth 2.0 client.
 *
 * <p>These properties are <b>not</b> part of the
 * <a href="https://datatracker.ietf.org/doc/html/rfc7591">RFC 7591</a> client metadata;
 * RFC 7591 fields are exposed directly on
 * {@link org.eulerframework.security.oauth2.server.authorization.client.EulerOAuth2Client}.
 */
public interface EulerOAuth2ClientSettings {

    /**
     * Returns whether the client is required to provide a proof key challenge
     * and verifier when performing the Authorization Code Grant flow.
     *
     * @return {@code true} if a proof key is required, {@code null} if unspecified
     */
    Boolean getRequireProofKey();

    /**
     * Returns whether authorization consent is required when the client requests access.
     *
     * @return {@code true} if authorization consent is required, {@code null} if unspecified
     */
    Boolean getRequireAuthorizationConsent();
}
