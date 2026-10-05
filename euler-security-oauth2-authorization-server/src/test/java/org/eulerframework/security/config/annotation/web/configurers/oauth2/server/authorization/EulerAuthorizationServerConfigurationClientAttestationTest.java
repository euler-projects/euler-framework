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

package org.eulerframework.security.config.annotation.web.configurers.oauth2.server.authorization;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.server.authorization.web.authentication.PublicClientAuthenticationConverter;
import org.springframework.security.web.authentication.AuthenticationConverter;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pins the ordering constraint for client attestation: traditional credential converters stay
 * ahead, while the attestation converter must claim an attestation-bearing PKCE request before
 * Spring's shape-only public-client converter can label it {@code none}.
 */
class EulerAuthorizationServerConfigurationClientAttestationTest {

    @Test
    void insertsAttestationImmediatelyBeforePublicClient() {
        AuthenticationConverter traditional = request -> null;
        AuthenticationConverter attestation = request -> null;
        PublicClientAuthenticationConverter publicClient = new PublicClientAuthenticationConverter();
        AuthenticationConverter trailing = request -> null;
        List<AuthenticationConverter> converters = new ArrayList<>(
                List.of(traditional, publicClient, trailing));

        EulerAuthorizationServerConfiguration.insertBeforePublicClientConverter(converters, attestation);

        assertEquals(4, converters.size());
        assertSame(traditional, converters.get(0));
        assertSame(attestation, converters.get(1));
        assertSame(publicClient, converters.get(2));
        assertSame(trailing, converters.get(3));
    }

    @Test
    void failsFastWhenSpringHasNoPublicClientConverter() {
        List<AuthenticationConverter> converters = new ArrayList<>(List.of(request -> null));

        assertThrows(IllegalStateException.class, () ->
                EulerAuthorizationServerConfiguration.insertBeforePublicClientConverter(
                        converters, request -> null));
    }
}
