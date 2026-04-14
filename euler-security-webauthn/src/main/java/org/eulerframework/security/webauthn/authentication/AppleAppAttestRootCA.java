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

package org.eulerframework.security.webauthn.authentication;

import com.webauthn4j.anchor.TrustAnchorRepository;
import com.webauthn4j.appattest.DeviceCheckManager;
import com.webauthn4j.data.attestation.authenticator.AAGUID;
import com.webauthn4j.verifier.attestation.trustworthiness.certpath.DefaultCertPathTrustworthinessVerifier;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateFactory;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Set;

/**
 * Apple App Attestation Root CA certificate and related utilities.
 * <p>
 * This class embeds the official Apple App Attestation Root CA (valid until 2045-03-15),
 * and provides convenience methods to create a {@link TrustAnchorRepository} and a
 * production-ready {@link DeviceCheckManager} that performs full certificate chain validation.
 *
 * @see <a href="https://www.apple.com/certificateauthority/">Apple PKI</a>
 */
public final class AppleAppAttestRootCA {

    /**
     * Apple App Attestation Root CA certificate (PEM-encoded).
     *
     * @see <a href="https://www.apple.com/certificateauthority/">Apple PKI</a>
     */
    public static final String PEM = "-----BEGIN CERTIFICATE-----\n" +
            "MIICITCCAaegAwIBAgIQC/O+DvHN0uD7jG5yH2IXmDAKBggqhkjOPQQDAzBSMSYw\n" +
            "JAYDVQQDDB1BcHBsZSBBcHAgQXR0ZXN0YXRpb24gUm9vdCBDQTETMBEGA1UECgwK\n" +
            "QXBwbGUgSW5jLjETMBEGA1UECAwKQ2FsaWZvcm5pYTAeFw0yMDAzMTgxODMyNTNa\n" +
            "Fw00NTAzMTUwMDAwMDBaMFIxJjAkBgNVBAMMHUFwcGxlIEFwcCBBdHRlc3RhdGlv\n" +
            "biBSb290IENBMRMwEQYDVQQKDApBcHBsZSBJbmMuMRMwEQYDVQQIDApDYWxpZm9y\n" +
            "bmlhMHYwEAYHKoZIzj0CAQYFK4EEACIDYgAERTHhmLW07ATaFQIEVwTtT4dyctdh\n" +
            "NbJhFs/Ii2FdCgAHGbpphY3+d8qjuDngIN3WVhQUBHAoMeQ/cLiP1sOUtgjqK9au\n" +
            "Yen1mMEvRq9Sk3Jm5X8U62H+xTD3FE9TgS41o0IwQDAPBgNVHRMBAf8EBTADAQH/\n" +
            "MB0GA1UdDgQWBBSskRBTM72+aEH/pwyp5frq5eWKoTAOBgNVHQ8BAf8EBAMCAQYw\n" +
            "CgYIKoZIzj0EAwMDaAAwZQIwQgFGnByvsiVbpTKwSga0kP0e8EeDS4+sQmTvb7vn\n" +
            "53O5+FRXgeLhd6azKnMFpR/ZAjEAp5U4xDgEgllF7En3VcE3iexZZtKeYnpqtijV\n" +
            "oyFraWVIyd/dganmrduC1bmTBGwD\n" +
            "-----END CERTIFICATE-----";

    private AppleAppAttestRootCA() {
    }

    /**
     * Loads the built-in Apple App Attestation Root CA as an {@link X509Certificate}.
     */
    public static X509Certificate certificate() {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf.generateCertificate(
                    new ByteArrayInputStream(PEM.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load Apple App Attestation Root CA", e);
        }
    }

    /**
     * Creates a {@link TrustAnchorRepository} that returns the Apple App Attestation Root CA
     * for any AAGUID or attestation certificate key identifier lookup.
     */
    public static TrustAnchorRepository trustAnchorRepository() {
        Set<TrustAnchor> anchors = Set.of(new TrustAnchor(certificate(), null));
        return new TrustAnchorRepository() {
            @Override
            public Set<TrustAnchor> find(AAGUID aaguid) {
                return anchors;
            }

            @Override
            public Set<TrustAnchor> find(byte[] attestationCertificateKeyIdentifier) {
                return anchors;
            }
        };
    }

    /**
     * Creates a production-ready {@link DeviceCheckManager} with full certificate chain
     * validation against the Apple App Attestation Root CA.
     */
    public static DeviceCheckManager deviceCheckManager() {
        return new DeviceCheckManager(
                new DefaultCertPathTrustworthinessVerifier(trustAnchorRepository()));
    }
}
