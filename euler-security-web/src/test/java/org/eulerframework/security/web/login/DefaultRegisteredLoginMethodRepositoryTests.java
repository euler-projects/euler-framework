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
package org.eulerframework.security.web.login;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultRegisteredLoginMethodRepositoryTests {

    @Test
    void emptyRepositoryHoldsNothing() {
        DefaultRegisteredLoginMethodRepository repository = new DefaultRegisteredLoginMethodRepository();

        assertTrue(repository.findAll().isEmpty());
        assertNull(repository.findById("password"));
    }

    /**
     * Order is user-visible: the login page renders the methods in it and
     * expands the first one when none declares itself primary, so it must
     * survive the round trip rather than following a hash.
     */
    @Test
    void registrationsAreReturnedInTheOrderTheyWereSavedIn() {
        DefaultRegisteredLoginMethodRepository repository = new DefaultRegisteredLoginMethodRepository(
                otp("sms-id", "sms"),
                password("pwd-id", "password"),
                oauth2("google-id", "google"));

        assertEquals(List.of("sms", "password", "google"),
                repository.findAll().stream().map(RegisteredLoginMethod::getName).toList());
    }

    @Test
    void savingAppendsBehindTheRegistrationsAlreadyHeld() {
        DefaultRegisteredLoginMethodRepository repository =
                new DefaultRegisteredLoginMethodRepository(password("pwd-id", "password"));

        repository.save(oauth2("google-id", "google"));

        assertEquals(List.of("password", "google"),
                repository.findAll().stream().map(RegisteredLoginMethod::getName).toList());
    }

    @Test
    void findByIdReturnsTheRegistrationStoredUnderIt() {
        DefaultRegisteredLoginMethodRepository repository =
                new DefaultRegisteredLoginMethodRepository(oauth2("some-uuid", "corp-sso"));

        RegisteredLoginMethod found = repository.findById("some-uuid");

        assertNotNull(found);
        assertEquals("corp-sso", found.getName());
        assertNull(repository.findById("corp-sso"), "the name is not an id");
    }

    /**
     * Ids are only unique within their own {@code method-type} node, so
     * two types declaring the same key reach the registry as one id. It
     * is rejected here, while the declarations are being collected.
     */
    @Test
    void duplicateIdIsRejected() {
        DefaultRegisteredLoginMethodRepository repository =
                new DefaultRegisteredLoginMethodRepository(password("google", "local"));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> repository.save(oauth2("google", "federated")));

        assertTrue(e.getMessage().contains("duplicate identifier"), e.getMessage());
    }

    @Test
    void duplicateNameIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new DefaultRegisteredLoginMethodRepository(
                        oauth2("key-1", "google"),
                        oauth2("key-2", "google")));

        assertTrue(e.getMessage().contains("duplicate name"), e.getMessage());
    }

    @Test
    void nullRegistrationIsRejected() {
        DefaultRegisteredLoginMethodRepository repository = new DefaultRegisteredLoginMethodRepository();

        assertThrows(IllegalArgumentException.class, () -> repository.save(null));
        assertThrows(IllegalArgumentException.class, () -> repository.findById(""));
    }

    /**
     * Callers hold a snapshot: neither their copy nor the registry can be
     * changed through it.
     */
    @Test
    void findAllReturnsAnImmutableSnapshot() {
        DefaultRegisteredLoginMethodRepository repository =
                new DefaultRegisteredLoginMethodRepository(password("pwd-id", "password"));
        List<RegisteredLoginMethod> snapshot = repository.findAll();

        repository.save(oauth2("google-id", "google"));

        assertEquals(1, snapshot.size());
        assertEquals(2, repository.findAll().size());
        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.add(oauth2("other-id", "other")));
    }

    // ---- helpers ----

    private static RegisteredPasswordLoginMethod password(String id, String name) {
        return new RegisteredPasswordLoginMethod(id, name, false);
    }

    private static RegisteredOtpLoginMethod otp(String id, String name) {
        return new RegisteredOtpLoginMethod(id, name, "phone", false, "sms");
    }

    private static RegisteredOAuth2LoginMethod oauth2(String id, String name) {
        return new RegisteredOAuth2LoginMethod(id, name, "google", false, null, null);
    }
}
