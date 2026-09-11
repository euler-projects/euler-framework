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

import org.junit.jupiter.api.Test;

import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests for {@link InMemoryAppAttestAttestationRegistrationService}, focusing on the
 * bind-if-absent {@code client_id} binding used by the DYNAMIC App Attest flow.
 */
class InMemoryAppAttestAttestationRegistrationServiceTest {

    private static AppAttestAttestationRegistration registration(String keyId, String clientId) {
        return new AppAttestAttestationRegistration(
                keyId, "ABCD1234EF", "com.example.app", clientId,
                new byte[16], new byte[0], new byte[0], new byte[0],
                null, "{}", 0);
    }

    private static PublicKey dummyPublicKey() {
        return new PublicKey() {
            @Override
            public String getAlgorithm() {
                return "EC";
            }

            @Override
            public String getFormat() {
                return "RAW";
            }

            @Override
            public byte[] getEncoded() {
                return new byte[0];
            }
        };
    }

    @Test
    void saveAndFindByKeyId() {
        InMemoryAppAttestAttestationRegistrationService service = new InMemoryAppAttestAttestationRegistrationService();

        service.saveRegistration(registration("kid-1", null));

        assertNotNull(service.findByKeyId("kid-1"));
        assertNull(service.findByKeyId("unknown"));
    }

    @Test
    void bindClientIdBindsWhenAbsent() {
        InMemoryAppAttestAttestationRegistrationService service = new InMemoryAppAttestAttestationRegistrationService();
        service.saveRegistration(registration("kid-1", null));

        service.bindClientId("kid-1", "client-1");

        assertEquals("client-1", service.findByKeyId("kid-1").getClientId());
    }

    @Test
    void bindClientIdDoesNotOverwriteAnExistingBinding() {
        InMemoryAppAttestAttestationRegistrationService service = new InMemoryAppAttestAttestationRegistrationService();
        service.saveRegistration(registration("kid-1", "client-original"));

        service.bindClientId("kid-1", "client-other");

        assertEquals("client-original", service.findByKeyId("kid-1").getClientId(),
                "bindClientId is bind-if-absent and must not overwrite an existing client_id");
    }

    @Test
    void bindClientIdIsNoOpForUnknownKey() {
        InMemoryAppAttestAttestationRegistrationService service = new InMemoryAppAttestAttestationRegistrationService();

        service.bindClientId("missing", "client-1");

        assertNull(service.findByKeyId("missing"));
    }

    /**
     * The in-memory implementation rebuilds the registration to bind a {@code client_id},
     * so every other field must survive the copy and the original instance must stay unbound.
     */
    @Test
    void bindClientIdPreservesEveryOtherField() {
        InMemoryAppAttestAttestationRegistrationService service = new InMemoryAppAttestAttestationRegistrationService();
        byte[] aaguid = {1, 2, 3};
        byte[] credentialId = {4, 5};
        byte[] certChain = {6};
        byte[] receipt = {7};
        PublicKey publicKey = dummyPublicKey();
        AppAttestAttestationRegistration original = new AppAttestAttestationRegistration(
                "kid-1", "ABCD1234EF", "com.example.app", null,
                aaguid, credentialId, certChain, receipt, publicKey, "{\"keys\":[]}", 42);
        service.saveRegistration(original);

        service.bindClientId("kid-1", "client-1");

        AppAttestAttestationRegistration bound = service.findByKeyId("kid-1");
        assertEquals("client-1", bound.getClientId());
        assertEquals("kid-1", bound.getKeyId());
        assertEquals("ABCD1234EF", bound.getTeamId());
        assertEquals("com.example.app", bound.getBundleId());
        assertSame(aaguid, bound.getAaguid());
        assertSame(credentialId, bound.getCredentialId());
        assertSame(certChain, bound.getAttestationCertificateChain());
        assertSame(receipt, bound.getReceipt());
        assertSame(publicKey, bound.getPublicKey());
        assertEquals("{\"keys\":[]}", bound.getJwks());
        assertEquals(42, bound.getSignCount());
        assertNull(original.getClientId(), "the original instance must remain unbound");
    }

    @Test
    void updateSignCountOnlyIncreases() {
        InMemoryAppAttestAttestationRegistrationService service = new InMemoryAppAttestAttestationRegistrationService();
        service.saveRegistration(registration("kid-1", null));

        service.updateSignCount("kid-1", 5);
        assertEquals(5, service.findByKeyId("kid-1").getSignCount());

        service.updateSignCount("kid-1", 3);
        assertEquals(5, service.findByKeyId("kid-1").getSignCount(), "a lower sign count must be ignored (replay guard)");
    }
}
