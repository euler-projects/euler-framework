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

import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.webauthn4j.appattest.DeviceCheckManager;
import com.webauthn4j.appattest.authenticator.DCAppleDevice;
import com.webauthn4j.appattest.authenticator.DCAppleDeviceImpl;
import com.webauthn4j.appattest.data.DCAssertionData;
import com.webauthn4j.appattest.data.DCAssertionParameters;
import com.webauthn4j.appattest.data.DCAssertionRequest;
import com.webauthn4j.appattest.data.DCAttestationData;
import com.webauthn4j.appattest.data.DCAttestationParameters;
import com.webauthn4j.appattest.data.DCAttestationRequest;
import com.webauthn4j.appattest.data.attestation.statement.AppleAppAttestAttestationStatement;
import com.webauthn4j.appattest.server.DCServerProperty;
import com.webauthn4j.data.attestation.authenticator.AAGUID;
import com.webauthn4j.data.attestation.authenticator.AttestedCredentialData;
import com.webauthn4j.data.attestation.authenticator.EC2COSEKey;
import com.webauthn4j.data.attestation.statement.AttestationCertificatePath;
import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import org.eulerframework.security.authentication.apple.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.cert.CertPath;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.util.Base64;
import java.util.List;

/**
 * Implementation of {@link AppleAppAttestValidationService} that uses webauthn4j-appattest
 * {@link DeviceCheckManager} for attestation and assertion validation.
 *
 * @see DeviceCheckManager
 */
public class Webauthn4jAppleAppAttestValidationService implements AppleAppAttestValidationService {

    private static final Logger logger = LoggerFactory.getLogger(Webauthn4jAppleAppAttestValidationService.class);

    private final DeviceCheckManager deviceCheckManager;
    private final AppleAppRepository appRepository;
    private final AppAttestRegistrationService registrationService;

    /**
     * Creates a new instance with the given {@link DeviceCheckManager}.
     * <p>
     * Use {@link AppleAppAttestRootCA#deviceCheckManager()} to create a production-ready
     * {@code DeviceCheckManager} with Apple's built-in root CA certificate chain validation.
     *
     * @param deviceCheckManager the device check manager (must not be {@code null})
     * @param appRepository       the registered Apple app repository (must not be {@code null})
     * @param registrationService the registration persistence service (must not be {@code null})
     * @see AppleAppAttestRootCA#deviceCheckManager()
     */
    public Webauthn4jAppleAppAttestValidationService(DeviceCheckManager deviceCheckManager,
                                                      AppleAppRepository appRepository,
                                                      AppAttestRegistrationService registrationService) {
        Assert.notNull(deviceCheckManager, "deviceCheckManager must not be null");
        Assert.notNull(appRepository, "appRepository must not be null");
        Assert.notNull(registrationService, "registrationService must not be null");
        this.deviceCheckManager = deviceCheckManager;
        this.appRepository = appRepository;
        this.registrationService = registrationService;
    }

    @Override
    public AppAttestRegistration validateAttestation(String keyId, String attestation, String challenge) throws AuthenticationException {
        try {
            byte[] keyIdBytes = Base64.getDecoder().decode(keyId);
            byte[] attestationBytes = Base64.getDecoder().decode(attestation);
            byte[] clientDataHash = sha256(challenge.getBytes(StandardCharsets.UTF_8));

            // 1. Construct the attestation request
            DCAttestationRequest request = new DCAttestationRequest(keyIdBytes, attestationBytes, clientDataHash);

            // 2. Parse to extract rpIdHash, then look up the registered app
            DCAttestationData parsed = this.deviceCheckManager.parse(request);
            byte[] rpIdHash = parsed.getAttestationObject().getAuthenticatorData().getRpIdHash();
            RegisteredAppleApp app = this.appRepository.findByAppIdHash(rpIdHash);
            if (app == null) {
                throw new AuthenticationServiceException("RP ID hash does not match any registered Apple App");
            }

            // 3. Construct server property with looked-up teamId + bundleId
            Challenge challengeObj = new DefaultChallenge(challenge.getBytes(StandardCharsets.UTF_8));
            DCServerProperty serverProperty = new DCServerProperty(app.teamId(), app.bundleId(), challengeObj);
            DCAttestationParameters params = new DCAttestationParameters(serverProperty);

            // 4. Validate (certificate chain, nonce, AAGUID, credentialId, etc.)
            DCAttestationData data = this.deviceCheckManager.validate(request, params);

            // 5. Extract attested credential data and flatten
            AttestedCredentialData attestedCredentialData =
                    data.getAttestationObject().getAuthenticatorData().getAttestedCredentialData();
            if (attestedCredentialData == null) {
                throw new AuthenticationServiceException("No attested credential data in attestation response");
            }
            byte[] aaguid = attestedCredentialData.getAaguid().getBytes();
            byte[] credentialId = attestedCredentialData.getCredentialId();
            PublicKey publicKey = attestedCredentialData.getCOSEKey().getPublicKey();

            // 6. Extract attestation statement data (certificate chain + receipt)
            AppleAppAttestAttestationStatement attestationStatement =
                    (AppleAppAttestAttestationStatement) data.getAttestationObject().getAttestationStatement();
            byte[] certChainBytes = encodeCertificateChain(attestationStatement.getX5c());
            byte[] receipt = attestationStatement.getReceipt();

            // 7. Generate JWKSet from the public key
            String jwksJson = generateJwksJson(publicKey);

            // 8. Save the registration with flattened data
            AppAttestRegistration registration = new AppAttestRegistration(
                    keyId, app.teamId(), app.bundleId(),
                    aaguid, credentialId,
                    certChainBytes, receipt,
                    publicKey, jwksJson, 0);
            this.registrationService.saveRegistration(registration);

            logger.debug("Apple App Attest attestation validation succeeded for keyId: {}", keyId);

            return registration;
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthenticationServiceException("Apple App Attest attestation validation failed", e);
        }
    }

    @Override
    public AppAttestRegistration validateAssertion(String keyId, String assertion, String challenge) throws AuthenticationException {
        try {
            byte[] keyIdBytes = Base64.getDecoder().decode(keyId);
            byte[] assertionBytes = Base64.getDecoder().decode(assertion);
            byte[] clientDataHash = sha256(challenge.getBytes(StandardCharsets.UTF_8));

            // 1. Load the stored registration
            AppAttestRegistration reg = this.registrationService.findByKeyId(keyId);
            if (reg == null) {
                throw new AuthenticationServiceException("No registered device found for key ID");
            }

            // 2. Construct the assertion request
            DCAssertionRequest request = new DCAssertionRequest(keyIdBytes, assertionBytes, clientDataHash);

            // 3. Reconstruct DCAppleDevice from stored flat registration data
            AttestedCredentialData acd = new AttestedCredentialData(
                    new AAGUID(reg.getAaguid()),
                    reg.getCredentialId(),
                    EC2COSEKey.create((ECPublicKey) reg.getPublicKey()));
            AppleAppAttestAttestationStatement stmt = new AppleAppAttestAttestationStatement(
                    decodeCertificateChain(reg.getAttestationCertificateChain()),
                    reg.getReceipt());
            DCAppleDevice device = new DCAppleDeviceImpl(acd, stmt, reg.getSignCount(), null);

            // 4. Construct server property
            Challenge challengeObj = new DefaultChallenge(challenge.getBytes(StandardCharsets.UTF_8));
            DCServerProperty serverProperty = new DCServerProperty(reg.getTeamId(), reg.getBundleId(), challengeObj);
            DCAssertionParameters params = new DCAssertionParameters(serverProperty, device);

            // 5. Validate (signature, rpIdHash, signCount, etc.)
            DCAssertionData data = this.deviceCheckManager.validate(request, params);

            // 6. Update sign count
            long newSignCount = data.getAuthenticatorData().getSignCount();
            this.registrationService.updateSignCount(keyId, newSignCount);

            logger.debug("Apple App Attest assertion validation succeeded for keyId: {}", keyId);

            return reg;
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthenticationServiceException("Apple App Attest assertion validation failed", e);
        }
    }

    private static String generateJwksJson(PublicKey publicKey) {
        ECPublicKey ecPublicKey = (ECPublicKey) publicKey;
        ECKey jwk = new ECKey.Builder(Curve.P_256, ecPublicKey).build();
        JWKSet jwkSet = new JWKSet(jwk);
        return jwkSet.toString();
    }

    private static byte[] sha256(byte[] data) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(data);
        } catch (Exception e) {
            throw new AuthenticationServiceException("SHA-256 computation failed", e);
        }
    }

    /**
     * Encodes an {@link AttestationCertificatePath} to a {@code byte[]} using the standard
     * {@code PkiPath} encoding via {@link CertPath#getEncoded()}.
     */
    private static byte[] encodeCertificateChain(AttestationCertificatePath certPath) throws Exception {
        return certPath.createCertPath().getEncoded();
    }

    /**
     * Decodes a {@code PkiPath}-encoded {@code byte[]} back to an {@link AttestationCertificatePath}.
     */
    @SuppressWarnings("unchecked")
    private static AttestationCertificatePath decodeCertificateChain(byte[] encoded) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        CertPath certPath = cf.generateCertPath(new ByteArrayInputStream(encoded));
        return new AttestationCertificatePath((List<X509Certificate>) certPath.getCertificates());
    }
}
