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

package org.eulerframework.security.oauth2.core.endpoint;

public final class EulerOAuth2ParameterNames {
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String KEY_ID = "kid";
    public static final String ATTESTATION = "attestation";
    public static final String ASSERTION = "assertion";
    public static final String CHALLENGE = "challenge";

    /**
     * Client Attestation JWT header.
     */
    public static final String OAUTH_CLIENT_ATTESTATION = "OAuth-Client-Attestation";

    /**
     * Client Attestation Proof-of-Possession data header.
     */
    public static final String OAUTH_CLIENT_ATTESTATION_POP = "OAuth-Client-Attestation-PoP";

    /**
     * Custom extension: Client Attestation type identifier.
     * Defaults to {@link org.eulerframework.security.oauth2.core.EulerOAuth2ClientAttestationType#JWT} when absent.
     */
    public static final String OAUTH_CLIENT_ATTESTATION_TYPE = "OAuth-Client-Attestation-Type";

    public static final String ADDITIONAL_SECURITY_SIGNAL = "additional-security-signal";
}
