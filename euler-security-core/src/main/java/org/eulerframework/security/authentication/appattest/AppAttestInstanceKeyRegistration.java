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

package org.eulerframework.security.authentication.appattest;

import org.springframework.util.Assert;

/**
 * An App instance's registration of a public key it generated itself, so that the server will
 * later accept a signature made by it.
 * <p>
 * What makes the key trustworthy is who registered it: an App Attest authenticated App
 * instance, which is to say an unmodified installation of a registered app running on genuine
 * Apple hardware. Nothing beyond that is claimed &mdash; no user is identified, no account is
 * created or referenced, and <b>no use is prescribed</b>. Signing a jwt-bearer assertion is
 * one thing a consumer may do with a key registered here; it is not what this record means,
 * and the App Attest domain does not know about it.
 * <p>
 * The two identifiers are deliberately named apart, because they are two different keys in two
 * different namespaces and conflating them is the one mistake this record can cause:
 * {@code appAttestKid} is the Apple-assigned identifier of the <em>App Attest KEY</em> the
 * instance authenticated with (the primary key of {@link AppAttestAttestationRegistration}),
 * while {@code jwkKid} identifies <em>this</em> key, which the instance generated on its own.
 * An instance registers one App Attest KEY and may register several of these.
 *
 * @param appAttestKid the App Attest KEY the registering instance authenticated with; the
 *                     instance this key belongs to
 * @param jwkKid       the {@code kid} of {@code jwk}, by which a consumer addresses it; the
 *                     RFC 7638 JWK Thumbprint of the key, derived by the server rather than
 *                     supplied by the client. {@link AppAttestInstanceKeyRegistrationService}
 *                     implementations require the two to agree before storing anything, so a
 *                     registration cannot name one key and carry another
 * @param jwk          the public JWK (RFC 7517) as JSON, carrying no private members
 * @see AppAttestInstanceKeyRegistrationService
 */
public record AppAttestInstanceKeyRegistration(String appAttestKid, String jwkKid, String jwk) {

    /**
     * The {@code identity_type} of a user identity whose credential is one of these keys.
     * <p>
     * Named for the issuer rather than for the kind of credential, because that is what the
     * jwt-bearer grant requires of it: an issuer and the identity type its assertions are read
     * against correspond one to one, so a type is one issuer's key space and not a general
     * notion of "a public key". A second issuer contributes a type of its own rather than
     * sharing this one, or an assertion would have more than one key space to be read against
     * and no way to say which was meant.
     * <p>
     * Held here because the App Attest domain owns the term, and an identity backend that files
     * these keys borrows it as a label the same way a {@code google} identity borrows Google's
     * name. The backend lives in the application and the issuer authenticator that reads it
     * lives above this module, so the two have no common home but this one; declaring the value
     * here is what keeps them from drifting apart on it.
     */
    public static final String USER_IDENTITY_TYPE = "app_attest_instance_key";

    public AppAttestInstanceKeyRegistration {
        Assert.hasText(appAttestKid, "appAttestKid must not be empty");
        Assert.hasText(jwkKid, "jwkKid must not be empty");
        Assert.hasText(jwk, "jwk must not be empty");
    }
}
