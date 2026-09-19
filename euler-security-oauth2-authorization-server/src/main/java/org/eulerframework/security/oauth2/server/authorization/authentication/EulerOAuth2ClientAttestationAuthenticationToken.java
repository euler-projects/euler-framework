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
package org.eulerframework.security.oauth2.server.authorization.authentication;

import org.eulerframework.security.authentication.appattest.AppAttestAttestationRegistration;
import org.eulerframework.security.oauth2.core.EulerClientAttestationProof;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.util.Assert;

/**
 * The <i>authenticated</i> client authentication result for a request that involved a client
 * attestation, carrying the verified {@link AppAttestAttestationRegistration} in a dedicated slot.
 * <p>
 * {@link OAuth2ClientAuthenticationToken} has a single {@code credentials} slot, which Spring's own
 * providers fill with the credential that was presented, so the registration needs a slot of its own;
 * whatever {@code credentials} the underlying authentication produced is preserved as-is.
 * <p>
 * This is a <b>result</b> token: it is published to the {@code SecurityContext} for grant components
 * to read {@link #getVerifiedRegistration()} from, and is never fed back into an
 * {@code AuthenticationManager} as a request.
 *
 * @see OAuth2ClientAuthenticationToken
 */
public final class EulerOAuth2ClientAttestationAuthenticationToken extends OAuth2ClientAuthenticationToken {

    private final AppAttestAttestationRegistration verifiedRegistration;

    @Deprecated
    private final EulerClientAttestationProof proof;

    /**
     * @param registeredClient            the authenticated client
     * @param clientAuthenticationMethod  the method the client authenticated with
     *                                    ({@code attest_jwt_client_auth} for the basic path, or the
     *                                    traditional method for the enhanced path)
     * @param credentials                 the credential the underlying authentication produced,
     *                                    preserved as-is (may be {@code null})
     * @param verifiedRegistration        the verified attestation registration
     * @param proof                       the kind of proof the request presented; see
     *                                    {@link #getProof()}
     */
    public EulerOAuth2ClientAttestationAuthenticationToken(RegisteredClient registeredClient,
                                                           ClientAuthenticationMethod clientAuthenticationMethod,
                                                           Object credentials,
                                                           AppAttestAttestationRegistration verifiedRegistration,
                                                           EulerClientAttestationProof proof) {
        super(registeredClient, clientAuthenticationMethod, credentials);
        Assert.notNull(verifiedRegistration, "verifiedRegistration must not be null");
        Assert.notNull(proof, "proof must not be null");
        this.verifiedRegistration = verifiedRegistration;
        this.proof = proof;
    }

    /**
     * The verified {@link AppAttestAttestationRegistration} this client's attestation resolved to.
     *
     * @return the verified registration, never {@code null}
     */
    public AppAttestAttestationRegistration getVerifiedRegistration() {
        return this.verifiedRegistration;
    }

    /**
     * The kind of proof the request presented, which is what tells App instance registration apart from
     * assertion verification once the request reaches a grant provider.
     *
     * @return the proof, never {@code null}
     * @deprecated compatibility logic; see
     * {@link org.eulerframework.security.core.userdetails.EulerDeviceUserDetailsService}. Removed,
     * along with the constructor parameter that feeds it, once the device-to-user mapping is retired.
     */
    @Deprecated
    public EulerClientAttestationProof getProof() {
        return this.proof;
    }
}
