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

package org.eulerframework.security.web.authentication.appattest;

import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * The App Attest credential fields resolved from a single request, all read from one carriage.
 * <p>
 * A field is {@code null} when the request did not carry it. Which fields an endpoint requires is
 * up to the endpoint: App instance registration needs an attestation and a challenge, while
 * re-authentication needs an assertion, a {@code kid} and a challenge.
 *
 * @param attestation the Base64-encoded Apple App Attest attestation object, or {@code null}
 * @param assertion   the Base64-encoded Apple App Attest assertion object, or {@code null}
 * @param kid         the App Attest KEY identifier, or {@code null}. Required alongside an
 *                    assertion, whose authenticator data embeds no credential ID; unnecessary
 *                    alongside an attestation, whose credential ID equals the KEY identifier
 * @param challenge   the one-time challenge in its raw form, or {@code null}
 * @see AppAttestCredentialResolver
 * @see AppAttestParameterNames
 */
public record AppAttestCredential(String attestation, String assertion, String kid, String challenge) {

    private static final AppAttestCredential ABSENT = new AppAttestCredential(null, null, null, null);

    /**
     * The result for a request that carries no App Attest credential in either carriage.
     *
     * @return an instance whose every field is {@code null}
     */
    public static AppAttestCredential absent() {
        return ABSENT;
    }

    /**
     * Whether the request carried any of the four fields, i.e. whether it used the App Attest
     * carriage at all. Callers use this to decide whether to fall back to another vocabulary.
     *
     * @return {@code true} if at least one field has text
     */
    public boolean isPresent() {
        return StringUtils.hasText(this.attestation)
                || StringUtils.hasText(this.assertion)
                || StringUtils.hasText(this.kid)
                || StringUtils.hasText(this.challenge);
    }

    /**
     * Rebuild a credential from a parameter map that a collector already normalized onto the
     * canonical {@link AppAttestParameterNames} keys, so that whatever consumed the request need not
     * know which carriage it arrived in.
     * <p>
     * It gives typed access to those fields. Deciding <i>which</i> client attestation mechanism a
     * request used is not this method's job: that is resolved once from the raw request and travels
     * with the collected parameters as a {@code ClientAuthenticationMethod}.
     *
     * @param collectedParams the normalized parameters; values under the canonical keys are expected
     *                        to be strings, and anything else is treated as absent
     * @return the rebuilt credential, or {@link #absent()} when none of the canonical keys is present
     */
    public static AppAttestCredential fromCollectedParameters(Map<String, ?> collectedParams) {
        return new AppAttestCredential(
                stringAt(collectedParams, AppAttestParameterNames.HEADER_ATTESTATION),
                stringAt(collectedParams, AppAttestParameterNames.HEADER_ASSERTION),
                stringAt(collectedParams, AppAttestParameterNames.HEADER_KID),
                stringAt(collectedParams, AppAttestParameterNames.HEADER_CHALLENGE));
    }

    private static String stringAt(Map<String, ?> collectedParams, String key) {
        return collectedParams.get(key) instanceof String value ? value : null;
    }
}
