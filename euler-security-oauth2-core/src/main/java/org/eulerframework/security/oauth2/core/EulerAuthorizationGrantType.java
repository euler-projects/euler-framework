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

package org.eulerframework.security.oauth2.core;

import org.springframework.security.oauth2.core.AuthorizationGrantType;

public class EulerAuthorizationGrantType {
    public static final AuthorizationGrantType PASSWORD = new AuthorizationGrantType("password");
    public static final AuthorizationGrantType WECHAT_AUTHORIZATION_CODE = new AuthorizationGrantType("wechat_authorization_code");

    /**
     * The {@code app_assertion} grant: renews a token from an App Attest assertion alone,
     * resolving the user from the device-to-user association that an earlier attestation
     * request established.
     * <p>
     * An assertion proves possession of a registered App Attest key, which authenticates the
     * client but does not identify a user. The grant survives only so that already-released
     * STATIC clients keep working: an attestation request JIT-provisions an anonymous user and
     * records the association, later assertion-only renewals read it, and a key that never went
     * through such an attestation request is rejected with {@code invalid_grant}.
     *
     * @deprecated compatibility path for released STATIC clients. Layer a user grant (e.g.
     * {@link #OTP}) or {@code refresh_token} on the assertion instead; those never write the
     * device-to-user association. Removable once no released client depends on assertion-only
     * renewal.
     */
    @Deprecated
    public static final AuthorizationGrantType APP_ASSERTION = new AuthorizationGrantType("urn:ietf:params:oauth:grant-type:app_assertion");

    public static final AuthorizationGrantType OTP = new AuthorizationGrantType("otp");
}
