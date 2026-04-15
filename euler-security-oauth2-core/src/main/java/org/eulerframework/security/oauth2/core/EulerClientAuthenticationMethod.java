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

import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

public class EulerClientAuthenticationMethod {

    /**
     * Attestation-based JWT client authentication method as registered in
     * <a href="https://www.ietf.org/archive/id/draft-ietf-oauth-attestation-based-client-auth-08.html#section-13.4">
     * Section 13.4</a> of the draft.
     */
    public static final ClientAuthenticationMethod ATTEST_JWT_CLIENT_AUTH = new ClientAuthenticationMethod(
            "attest_jwt_client_auth");
}
