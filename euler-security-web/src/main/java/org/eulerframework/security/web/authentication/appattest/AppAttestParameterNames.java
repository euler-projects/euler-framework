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

/**
 * Credential carriage names for the App Attest business domain endpoints ({@code /app_attest/**}).
 * <p>
 * These are distinct from the OAuth business domain's {@code OAuth-Client-Attestation-*} headers,
 * which are reserved for {@code /oauth2/**}. Every authenticated {@code /app_attest/**} endpoint
 * accepts its credential in either of two carriages, and the two are never mixed within a single
 * request:
 * <ul>
 *     <li>HTTP headers: {@code App-Attest-Attestation}, {@code App-Attest-Kid},
 *     {@code App-Attest-Challenge}, {@code App-Attest-Assertion};</li>
 *     <li>{@code application/x-www-form-urlencoded} parameters: {@code app_attest_attestation},
 *     {@code app_attest_kid}, {@code app_attest_challenge}, {@code app_attest_assertion}.</li>
 * </ul>
 * The {@code attestation} fields carry the Apple App Attest attestation object (App instance
 * registration), whose embedded credential ID equals the key ID, so no {@code kid} is needed
 * alongside an attestation. The {@code assertion} fields carry an assertion (App instance
 * re-authentication); an assertion's authenticator data embeds no credential ID, so the {@code kid}
 * field must accompany an assertion to locate the registered public key.
 */
public final class AppAttestParameterNames {

    // ---- HTTP header carriage ----

    public static final String HEADER_ATTESTATION = "App-Attest-Attestation";
    public static final String HEADER_KID = "App-Attest-Kid";
    public static final String HEADER_CHALLENGE = "App-Attest-Challenge";
    public static final String HEADER_ASSERTION = "App-Attest-Assertion";

    // ---- form parameter carriage ----

    public static final String PARAM_ATTESTATION = "app_attest_attestation";
    public static final String PARAM_KID = "app_attest_kid";
    public static final String PARAM_CHALLENGE = "app_attest_challenge";
    public static final String PARAM_ASSERTION = "app_attest_assertion";

    private AppAttestParameterNames() {
    }
}
