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

import com.nimbusds.jose.jwk.JWK;
import org.eulerframework.security.authentication.appattest.AppAttestAttestationRegistration;
import org.eulerframework.security.authentication.appattest.AppAttestIssuedKey;
import org.eulerframework.security.authentication.appattest.AppAttestIssuedKeyService;
import org.eulerframework.security.core.EulerUserService;
import org.eulerframework.security.core.identity.UserIdentityService;
import org.eulerframework.security.oauth2.core.EulerClientAuthenticationMethod;
import org.eulerframework.security.provisioning.jit.JitProvisioningPolicy;
import org.eulerframework.security.provisioning.jit.JitProvisioningPolicyResolver;
import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.text.ParseException;

/**
 * The {@link JwtBearerIssuerAuthenticator} for an issuer that is an Apple App Attest
 * authenticated App instance, whose {@code iss} is its own OAuth2 {@code client_id}.
 * <p>
 * This is a deliberately low-assurance anchor. What an App Attest credential proves is that a
 * genuine, unmodified installation of a registered app made the request from real Apple hardware;
 * it says nothing about which person is holding the device. Accounts opened on that basis are
 * therefore anonymous trials, and the {@code public_key} identity they carry is confined to
 * accounts with no other identity. Raising the assurance means contributing a different
 * authenticator for a different kind of issuer, not changing this one.
 * <p>
 * All this class adds to
 * {@link AbstractUserIdentityJwtBearerIssuerAuthenticator} is what is an App Attest fact rather than
 * an identity-model fact: who the issuer may be, where a first login's key comes from, that a first
 * login is the point of this issuer at all, and who may ask for one.
 * <p>
 * Keys are looked up in {@link AppAttestIssuedKeyService}, which the {@code POST /app_attest/keys}
 * endpoint writes. That endpoint authenticates its caller with an assertion over an App Attest KEY
 * whose bound {@code client_id} becomes the issuer, so an issuer appearing in the registry is by
 * construction a client that was minted by attestation-based dynamic registration. That is enough to
 * look a key up by, but not enough to open an account on: see
 * {@link #requireProvisioningAllowed(JwtBearerAssertion)}.
 *
 * @see AppAttestIssuedKeyService
 */
public class AppAttestJwtBearerIssuerAuthenticator extends AbstractUserIdentityJwtBearerIssuerAuthenticator {

    private final AppAttestIssuedKeyService issuedKeyService;

    public AppAttestJwtBearerIssuerAuthenticator(AppAttestIssuedKeyService issuedKeyService,
                                                 UserIdentityService userIdentityService,
                                                 EulerUserService userService,
                                                 JitProvisioningPolicyResolver jitProvisioningPolicyResolver) {
        super(userIdentityService, userService, jitProvisioningPolicyResolver);
        Assert.notNull(issuedKeyService, "issuedKeyService must not be null");
        this.issuedKeyService = issuedKeyService;
    }

    /**
     * An App Attest issuer is the App instance that authenticated this very request, so the
     * assertion's {@code iss} has to name the {@code client_id} bound to the App Attest KEY the
     * client authentication was verified against. Reading it from that verified KEY rather than
     * from the resolved client keeps this authenticator and {@code POST /app_attest/keys} agreeing
     * on what an issuer is by construction, since the endpoint registers under the same value.
     */
    @Override
    public boolean supports(String issuer, OAuth2ClientAuthenticationToken clientPrincipal) {
        if (!(clientPrincipal instanceof EulerOAuth2ClientAttestationAuthenticationToken attestationAuthentication)) {
            return false;
        }
        AppAttestAttestationRegistration registration = attestationAuthentication.getVerifiedRegistration();
        return registration != null && issuer.equals(registration.getClientId());
    }

    @Override
    protected String identityType() {
        return UserIdentityService.IDENTITY_TYPE_PUBLIC_KEY;
    }

    @Override
    @Nullable
    protected JWK resolveRegisteredKey(String issuer, @Nullable String keyId) {
        if (!StringUtils.hasText(keyId)) {
            // The registry is keyed by (issuer, keyId); without a key ID there is nothing to look
            // up. The caller reports the missing header rather than an unknown key.
            return null;
        }
        AppAttestIssuedKey issuedKey = this.issuedKeyService.findByIssuerAndKeyId(issuer, keyId);
        if (issuedKey == null) {
            return null;
        }
        try {
            return JWK.parse(issuedKey.jwk());
        } catch (ParseException | RuntimeException e) {
            // This JSON was written by this server, so it failing to parse is a storage fault
            // rather than something the caller can be told to fix.
            throw new IllegalStateException(
                    "Stored issued key is not a parsable JWK (issuer='" + issuer + "', keyId='" + keyId + "')", e);
        }
    }

    /**
     * Allowed: opening an anonymous trial account is the whole point of this issuer. Whether a
     * given deployment wants that is the {@code public_key} JIT provisioning policy's call, not
     * this authenticator's.
     */
    @Override
    protected boolean allowsProvisioningWithoutSubject() {
        return true;
    }

    /**
     * An account may be opened only for a client that authenticated <em>by</em> App Attest.
     * <p>
     * Having reached this authenticator is not the same thing, and the difference is the whole
     * reason this is here. An {@link EulerOAuth2ClientAttestationAuthenticationToken} is also
     * produced for a client that authenticated with a traditional credential and merely presented an
     * attestation alongside it, and for a public client whose authentication stays {@code none} with
     * the attestation carried as an additional signal. Both carry a verified registration naming the
     * issuer, so both pass {@link #supports} and both can look a key up. But in both the attestation
     * describes the request rather than being how the client proved itself, and an anonymous account
     * is minted on the strength of that proof alone &mdash; so neither may open one. Fails closed: a
     * client authentication with no method at all is simply not this one.
     * <p>
     * Stated here rather than as a setting on the provisioning policy because it is a fact about
     * this issuer, not about the {@code public_key} identity type. The type says nothing about who
     * may ask for one; a policy dimension only this issuer could use would be carried into every
     * other provisioning point in the framework and answered by none of them.
     */
    @Override
    protected JitProvisioningPolicy requireProvisioningAllowed(JwtBearerAssertion assertion) {
        ClientAuthenticationMethod method = assertion.getClientPrincipal().getClientAuthenticationMethod();
        if (!EulerClientAuthenticationMethod.ATTEST_APPATTEST_CLIENT_AUTH.equals(method)) {
            throw refuse("the client authenticated by '" + method
                    + "', which is not App Attest client authentication; it may not open an account");
        }
        return super.requireProvisioningAllowed(assertion);
    }
}
