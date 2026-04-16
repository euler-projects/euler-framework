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

/**
 * Error codes defined by or related to draft-ietf-oauth-attestation-based-client-auth-08.
 */
public final class EulerOAuth2ErrorCodes {

    /**
     * The authorization server requires the client to obtain and present
     * an attestation challenge before proceeding (Section 6.2 of the draft).
     */
    public static final String USE_ATTESTATION_CHALLENGE = "use_attestation_challenge";

    /**
     * The client attestation has expired or is stale and the client must
     * obtain a fresh attestation (Section 6.2 of the draft).
     */
    public static final String USE_FRESH_ATTESTATION = "use_fresh_attestation";

    /**
     * The client attestation or its proof-of-possession is invalid
     * (Section 6.2 of the draft).
     */
    public static final String INVALID_CLIENT_ATTESTATION = "invalid_client_attestation";

    private EulerOAuth2ErrorCodes() {
    }
}
