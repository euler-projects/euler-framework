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

package org.springframework.security.oauth2.server.authorization.authentication;

import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.util.Assert;

/**
 * Exposes the package-private {@link CodeVerifierAuthenticator} to code outside
 * {@code org.springframework.security.oauth2.server.authorization.authentication}, so that the PKCE
 * {@code code_verifier} check Spring performs for its own client authentication providers can be
 * reused from another package.
 * <p>
 * {@code authenticateIfAvailable} is forwarded rather than {@code authenticateRequired} because it
 * self-gates on the grant type: a no-op for any grant other than {@code authorization_code}, so
 * callers can invoke it unconditionally.
 *
 * @see CodeVerifierAuthenticator
 */
public class CodeVerifierAuthenticatorAccessor {

    private final CodeVerifierAuthenticator codeVerifierAuthenticator;

    public CodeVerifierAuthenticatorAccessor(OAuth2AuthorizationService authorizationService) {
        Assert.notNull(authorizationService, "authorizationService must not be null");
        this.codeVerifierAuthenticator = new CodeVerifierAuthenticator(authorizationService);
    }

    public void authenticateIfAvailable(OAuth2ClientAuthenticationToken clientAuthentication,
                                        RegisteredClient registeredClient) {
        this.codeVerifierAuthenticator.authenticateIfAvailable(clientAuthentication, registeredClient);
    }
}
