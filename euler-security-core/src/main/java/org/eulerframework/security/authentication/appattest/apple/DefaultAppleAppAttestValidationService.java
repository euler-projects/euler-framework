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

package org.eulerframework.security.authentication.appattest.apple;

import org.eulerframework.security.authentication.appattest.AppAttestAttestationRegistration;
import org.eulerframework.security.authentication.appattest.AppAttestAttestationRegistrationService;
import org.eulerframework.security.authentication.appattest.RegisteredAppRepository;
import org.eulerframework.security.authentication.appattest.RegisteredApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;
import tools.jackson.dataformat.cbor.CBORMapper;

import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWKSet;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.Signature;
import java.security.cert.*;
import java.security.interfaces.ECPublicKey;
import java.util.*;

/**
 * Default implementation of {@link AppleAppAttestValidationService} that performs
 * full attestation and assertion validation according to Apple's App Attest documentation.
 * <p>
 * Attestation validation steps:
 * <ol>
 *     <li>CBOR-decode the attestation object</li>
 *     <li>Verify the x5c certificate chain against Apple's App Attestation Root CA</li>
 *     <li>Verify the nonce in the credential certificate extension</li>
 *     <li>Verify the credential ID matches the provided key ID</li>
 *     <li>Verify the RP ID hash matches the expected App ID</li>
 *     <li>Verify the sign count is 0 (initial attestation)</li>
 * </ol>
 * <p>
 * Assertion validation steps:
 * <ol>
 *     <li>CBOR-decode the assertion object</li>
 *     <li>Load the stored public key and sign count</li>
 *     <li>Verify the RP ID hash matches the expected App ID</li>
 *     <li>Verify the sign count is greater than the stored challenge</li>
 *     <li>Verify the signature using the stored EC public key</li>
 *     <li>Update the stored sign count</li>
 * </ol>
 *
 * @see <a href="https://developer.apple.com/documentation/devicecheck/validating-apps-that-connect-to-your-server">
 * Validating Apps That Connect to Your Server</a>
 */
public class DefaultAppleAppAttestValidationService implements AppleAppAttestValidationService {

    private final Logger logger = LoggerFactory.getLogger(DefaultAppleAppAttestValidationService.class);

    /**
     * OID of the Apple credential certificate extension containing the nonce.
     */
    private static final String NONCE_EXTENSION_OID = "1.2.840.113635.100.8.2";

    /**
     * Apple App Attest AAGUID for production environment.
     */
    private static final byte[] PRODUCTION_AAGUID = {
            'a', 'p', 'p', 'a', 't', 't', 'e', 's', 't',
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };

    /**
     * Apple App Attest AAGUID for development environment.
     */
    private static final byte[] DEVELOPMENT_AAGUID = {
            'a', 'p', 'p', 'a', 't', 't', 'e', 's', 't',
            'd', 'e', 'v', 'e', 'l', 'o', 'p'
    };

    /**
     * Apple App Attestation Root CA certificate (PEM-encoded).
     *
     * @see <a href="https://www.apple.com/certificateauthority/">Apple PKI</a>
     */
    private static final String APPLE_APP_ATTESTATION_ROOT_CA_PEM =
            "-----BEGIN CERTIFICATE-----\n" +
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

    private final CBORMapper cborMapper = new CBORMapper();
    private final RegisteredAppRepository appRepository;
    private final X509Certificate rootCertificate;
    private final AppAttestAttestationRegistrationService appAttestAttestationRegistrationService;
    private volatile boolean allowDevelopmentEnvironment = false;
    private boolean revocationEnabled = false;

    /**
     * Create a new instance with an {@link RegisteredAppRepository} for multi-app support.
     *
     * @param registeredAppRepository                     the repository of registered Apple Apps
     * @param appAttestAttestationRegistrationService the service for storing and retrieving key credentials
     */
    public DefaultAppleAppAttestValidationService(RegisteredAppRepository registeredAppRepository,
                                                  AppAttestAttestationRegistrationService appAttestAttestationRegistrationService) {
        Assert.notNull(registeredAppRepository, "deviceRepository must not be null");
        Assert.notNull(appAttestAttestationRegistrationService, "deviceAttestRegistrationService must not be null");
        this.appRepository = registeredAppRepository;
        this.rootCertificate = loadRootCertificate();
        this.appAttestAttestationRegistrationService = appAttestAttestationRegistrationService;
    }

    /**
     * Set whether to allow attestations from the development environment.
     * <p>
     * When {@code true}, both production and development AAGUIDs are accepted.
     * When {@code false} (default), only the production AAGUID is accepted.
     *
     * @param allowDevelopmentEnvironment whether to allow development environment
     */
    public void setAllowDevelopmentEnvironment(boolean allowDevelopmentEnvironment) {
        this.allowDevelopmentEnvironment = allowDevelopmentEnvironment;
    }

    /**
     * Set whether certificate revocation checking is enabled.
     * <p>
     * When {@code true}, the PKIX validator will check certificate revocation status.
     * Default is {@code false}.
     *
     * @param revocationEnabled whether to enable revocation checking
     */
    public void setRevocationEnabled(boolean revocationEnabled) {
        this.revocationEnabled = revocationEnabled;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AppAttestAttestationRegistration validateAttestation(String keyId, String attestation, String challenge) throws AuthenticationException {
        try {
            // Step 1: Base64-decode and CBOR-decode the attestation object
            byte[] attestationBytes = Base64.getDecoder().decode(attestation);
            Map<String, Object> attestationObject = cborMapper.readValue(attestationBytes, Map.class);

            String fmt = (String) attestationObject.get("fmt");
            if (!"apple-appattest".equals(fmt)) {
                throw new AuthenticationServiceException(
                        "Unexpected attestation format");
            }

            Map<String, Object> attStmt = (Map<String, Object>) attestationObject.get("attStmt");
            byte[] authData = (byte[]) attestationObject.get("authData");

            // Validate authData minimum length (rpIdHash 32 + flags 1 + signCount 4 + aaguid 16 + credIdLen 2 = 55)
            if (authData == null || authData.length < 55) {
                throw new AuthenticationServiceException("Invalid authenticator data");
            }

            // Step 2: Verify the x5c certificate chain against Apple App Attestation Root CA
            List<byte[]> x5cBytes = (List<byte[]>) attStmt.get("x5c");
            if (x5cBytes == null || x5cBytes.size() < 2) {
                throw new AuthenticationServiceException("Invalid x5c certificate chain");
            }

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            List<X509Certificate> certificates = new ArrayList<>();
            for (byte[] certBytes : x5cBytes) {
                certificates.add((X509Certificate) cf.generateCertificate(
                        new ByteArrayInputStream(certBytes)));
            }
            verifyCertificateChain(certificates);

            // Step 3: Verify nonce
            // clientDataHash = SHA256(challenge)
            byte[] clientDataHash = sha256(challenge.getBytes(StandardCharsets.UTF_8));
            // composite = authData || clientDataHash
            byte[] composite = new byte[authData.length + clientDataHash.length];
            System.arraycopy(authData, 0, composite, 0, authData.length);
            System.arraycopy(clientDataHash, 0, composite, authData.length, clientDataHash.length);
            // expectedNonce = SHA256(composite)
            byte[] expectedNonce = sha256(composite);
            byte[] actualNonce = extractNonceFromCertificate(certificates.get(0));

            if (!MessageDigest.isEqual(expectedNonce, actualNonce)) {
                throw new AuthenticationServiceException("Nonce verification failed");
            }

            // Step 4: Verify the credential ID matches the key ID
            byte[] credentialId = extractCredentialId(authData);
            if (!Base64.getEncoder().encodeToString(credentialId).equals(keyId)) {
                throw new AuthenticationServiceException(
                        "Key ID mismatch: credential ID does not match the provided key ID");
            }

            // Step 5: Look up the registered Apple App by RP ID hash
            byte[] rpIdHash = Arrays.copyOfRange(authData, 0, 32);
            RegisteredApp app = this.appRepository.findByAppIdHash(rpIdHash);
            if (app == null) {
                throw new AuthenticationServiceException(
                        "RP ID hash does not match any registered Apple App");
            }

            // Step 6: Verify the AAGUID matches Apple App Attest
            byte[] aaguid = Arrays.copyOfRange(authData, 37, 53);
            if (!MessageDigest.isEqual(aaguid, PRODUCTION_AAGUID)
                    && !(this.allowDevelopmentEnvironment && MessageDigest.isEqual(aaguid, DEVELOPMENT_AAGUID))) {
                throw new AuthenticationServiceException(
                        "Invalid AAGUID");
            }

            // Step 7: Verify the counter is 0 (initial attestation)
            int counter = ByteBuffer.wrap(authData, 33, 4).getInt();
            if (counter != 0) {
                throw new AuthenticationServiceException(
                        "Sign count must be 0 for attestation");
            }

            // Step 8: Encode the certificate chain as PkiPath
            byte[] certChainBytes = cf.generateCertPath(certificates).getEncoded();

            // Step 9: Extract the receipt from the attestation statement
            byte[] receipt = (byte[]) attStmt.get("receipt");

            // Step 10: Generate JWK Set JSON from the EC public key
            ECPublicKey ecPublicKey = (ECPublicKey) certificates.get(0).getPublicKey();
            String jwksJson = new JWKSet(new ECKey.Builder(Curve.P_256, ecPublicKey).build()).toString();

            if (logger.isDebugEnabled()) {
                logger.debug("Apple App Attest validation succeeded for keyId: {}", keyId);
            }
            AppAttestAttestationRegistration registration = new AppAttestAttestationRegistration(
                    keyId, app.teamId(), app.bundleId(),
                    aaguid, credentialId,
                    certChainBytes, receipt,
                    ecPublicKey, jwksJson, 0);
            this.appAttestAttestationRegistrationService.saveRegistration(registration);

            logger.debug("Apple App Attest attestation validation succeeded for keyId: {}", keyId);

            return registration;
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            this.logger.error("Apple App Attest attestation validation failed {}", e.getMessage(), e);
            throw new AuthenticationServiceException("Apple App Attest attestation validation failed", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public AppAttestAttestationRegistration validateAssertion(String keyId, String assertion, String challenge) throws AuthenticationException {
        try {
            // Step 1: Load the stored key credential
            AppAttestAttestationRegistration appAttestAttestationRegistration = this.appAttestAttestationRegistrationService.findByKeyId(keyId);
            if (appAttestAttestationRegistration == null) {
                throw new AuthenticationServiceException("No registered key credential found for key ID");
            }

            // Step 2: Base64-decode and CBOR-decode the assertion object
            byte[] assertionBytes = Base64.getDecoder().decode(assertion);
            Map<String, Object> assertionObject = cborMapper.readValue(assertionBytes, Map.class);

            byte[] authenticatorData = (byte[]) assertionObject.get("authenticatorData");
            byte[] signature = (byte[]) assertionObject.get("signature");

            if (authenticatorData == null || authenticatorData.length < 37) {
                throw new AuthenticationServiceException("Invalid authenticator data in assertion");
            }
            if (signature == null || signature.length == 0) {
                throw new AuthenticationServiceException("Missing signature in assertion");
            }

            // Step 3: Look up the registered Apple App by RP ID hash
            byte[] rpIdHash = Arrays.copyOfRange(authenticatorData, 0, 32);
            RegisteredApp app = this.appRepository.findByAppIdHash(rpIdHash);
            if (app == null) {
                throw new AuthenticationServiceException(
                        "RP ID hash does not match any registered Apple App");
            }

            // Step 4: Verify the sign count is greater than the stored challenge
            int counter = ByteBuffer.wrap(authenticatorData, 33, 4).getInt();
            long unsignedCounter = Integer.toUnsignedLong(counter);
            if (unsignedCounter <= appAttestAttestationRegistration.getSignCount()) {
                throw new AuthenticationServiceException("Sign count not incremented, possible replay attack");
            }

            // Step 5: Compute nonce and verify signature
            // nonce = SHA256(authenticatorData || SHA256(challenge))
            byte[] clientDataHash = sha256(challenge.getBytes(StandardCharsets.UTF_8));
            byte[] composite = new byte[authenticatorData.length + clientDataHash.length];
            System.arraycopy(authenticatorData, 0, composite, 0, authenticatorData.length);
            System.arraycopy(clientDataHash, 0, composite, authenticatorData.length, clientDataHash.length);
            byte[] nonce = sha256(composite);

            // Verify the signature over the nonce using the stored public key
            Signature sig = Signature.getInstance("SHA256withECDSA");
            sig.initVerify(appAttestAttestationRegistration.getPublicKey());
            sig.update(nonce);
            if (!sig.verify(signature)) {
                throw new AuthenticationServiceException("Assertion signature verification failed");
            }

            // Step 6: Update the stored sign count
            this.appAttestAttestationRegistrationService.updateSignCount(keyId, unsignedCounter);

            if (logger.isDebugEnabled()) {
                logger.debug("Apple App Attest assertion validation succeeded for keyId: {}", keyId);
            }

            return appAttestAttestationRegistration;
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthenticationServiceException("Apple App Attest assertion validation failed", e);
        }
    }

    /**
     * Verify the certificate chain against the Apple App Attestation Root CA.
     */
    private void verifyCertificateChain(List<X509Certificate> certificates) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        CertPath certPath = cf.generateCertPath(certificates);

        Set<TrustAnchor> trustAnchors = Set.of(new TrustAnchor(this.rootCertificate, null));
        CertPathValidator validator = CertPathValidator.getInstance("PKIX");
        PKIXParameters params = new PKIXParameters(trustAnchors);
        params.setRevocationEnabled(this.revocationEnabled);
        validator.validate(certPath, params);
    }

    /**
     * Extract the nonce from the credential certificate's Apple extension (OID 1.2.840.113635.100.8.2).
     * <p>
     * The extension value has the following ASN.1 structure:
     * <pre>
     * SEQUENCE {
     *   [1] EXPLICIT {
     *     OCTET STRING (32 bytes) -- nonce
     *   }
     * }
     * </pre>
     */
    private byte[] extractNonceFromCertificate(X509Certificate certificate) {
        byte[] extensionValue = certificate.getExtensionValue(NONCE_EXTENSION_OID);
        if (extensionValue == null) {
            throw new AuthenticationServiceException(
                    "Certificate does not contain nonce extension (OID: " + NONCE_EXTENSION_OID + ")");
        }

        // Unwrap the outer DER OCTET STRING wrapping added by getExtensionValue()
        byte[] inner = unwrapDerOctetString(extensionValue);

        // Parse: SEQUENCE -> [1] EXPLICIT -> OCTET STRING -> nonce
        int offset = 0;
        offset += skipDerTagAndLength(inner, offset); // SEQUENCE
        if (offset >= inner.length) {
            throw new AuthenticationServiceException(
                    "Malformed nonce extension: unexpected end of data after SEQUENCE");
        }
        offset += skipDerTagAndLength(inner, offset); // context [1] EXPLICIT
        if (offset >= inner.length) {
            throw new AuthenticationServiceException(
                    "Malformed nonce extension: unexpected end of data after context tag");
        }

        if (inner[offset] != 0x04) {
            throw new AuthenticationServiceException(
                    "Expected OCTET STRING tag (0x04) in nonce extension, got: 0x"
                            + String.format("%02X", inner[offset]));
        }
        offset++; // skip OCTET STRING tag
        if (offset >= inner.length) {
            throw new AuthenticationServiceException(
                    "Malformed nonce extension: unexpected end of data after OCTET STRING tag");
        }
        int nonceLength = readDerLength(inner, offset);
        offset += derLengthSize(inner, offset);
        if (offset + nonceLength > inner.length) {
            throw new AuthenticationServiceException(
                    "Malformed nonce extension: nonce length " + nonceLength
                            + " exceeds available data at offset " + offset);
        }
        return Arrays.copyOfRange(inner, offset, offset + nonceLength);
    }

    /**
     * Extract the credential ID from the authenticator data.
     * <p>
     * AuthData layout:
     * <pre>
     * [0..31]   rpIdHash (32 bytes)
     * [32]      flags (1 byte)
     * [33..36]  signCount (4 bytes, big-endian)
     * [37..52]  aaguid (16 bytes)
     * [53..54]  credentialIdLength (2 bytes, big-endian)
     * [55..]    credentialId
     * </pre>
     */
    private byte[] extractCredentialId(byte[] authData) {
        byte flags = authData[32];
        boolean hasAttestedCredentialData = (flags & 0x40) != 0;
        if (!hasAttestedCredentialData) {
            throw new AuthenticationServiceException(
                    "Attested credential data flag not set in authData");
        }

        int credentialIdLength = ByteBuffer.wrap(authData, 53, 2).getShort() & 0xFFFF;
        return Arrays.copyOfRange(authData, 55, 55 + credentialIdLength);
    }

    private byte[] unwrapDerOctetString(byte[] der) {
        if (der[0] != 0x04) {
            throw new AuthenticationServiceException("Expected DER OCTET STRING tag (0x04)");
        }
        int offset = 1;
        int length = readDerLength(der, offset);
        offset += derLengthSize(der, offset);
        return Arrays.copyOfRange(der, offset, offset + length);
    }

    /**
     * Skip a DER tag and its length field, returning the total number of bytes consumed.
     */
    private int skipDerTagAndLength(byte[] der, int offset) {
        int start = offset;
        offset++; // skip tag byte
        offset += derLengthSize(der, offset); // skip length bytes
        return offset - start;
    }

    private int readDerLength(byte[] der, int offset) {
        int firstByte = der[offset] & 0xFF;
        if (firstByte < 0x80) {
            return firstByte;
        }
        int numBytes = firstByte & 0x7F;
        int length = 0;
        for (int i = 0; i < numBytes; i++) {
            length = (length << 8) | (der[offset + 1 + i] & 0xFF);
        }
        return length;
    }

    private int derLengthSize(byte[] der, int offset) {
        int firstByte = der[offset] & 0xFF;
        if (firstByte < 0x80) {
            return 1;
        }
        return 1 + (firstByte & 0x7F);
    }

    private byte[] sha256(byte[] data) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(data);
        } catch (Exception e) {
            throw new AuthenticationServiceException("SHA-256 computation failed", e);
        }
    }

    private X509Certificate loadRootCertificate() {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf.generateCertificate(
                    new ByteArrayInputStream(APPLE_APP_ATTESTATION_ROOT_CA_PEM.getBytes(StandardCharsets.UTF_8)));
        } catch (CertificateException e) {
            throw new IllegalStateException("Failed to load Apple App Attestation Root CA", e);
        }
    }
}
