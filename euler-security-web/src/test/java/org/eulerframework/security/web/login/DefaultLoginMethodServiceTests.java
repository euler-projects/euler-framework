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

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultLoginMethodServiceTests {

    @Test
    void dispatchesEachEntryToMatchingType() {
        RecordingHandler oauth2Handler = new RecordingHandler("oauth2");
        RecordingHandler otpHandler = new RecordingHandler("otp");

        DefaultLoginMethodService service = new DefaultLoginMethodService(
                List.of(oauth2Handler, otpHandler),
                repository(method("key-1", "oauth2", "google"), method("key-2", "otp", "sms")));

        List<LoginMethod> served = service.listAll();

        assertAll("both types invoked in declaration order under their own names",
                () -> assertEquals(2, served.size()),
                () -> assertEquals("google", served.get(0).getName()),
                () -> assertEquals("sms", served.get(1).getName()),
                () -> assertEquals(List.of("google"), oauth2Handler.namesSeen),
                () -> assertEquals(List.of("sms"), otpHandler.namesSeen));
    }

    @Test
    void registrationIsAddressedByNameNotByKey() {
        RecordingHandler oauth2Handler = new RecordingHandler("oauth2");

        DefaultLoginMethodService service = new DefaultLoginMethodService(
                List.of(oauth2Handler), repository(method("some-uuid", "oauth2", "corp-sso")));

        List<LoginMethod> served = service.listAll();

        assertEquals(1, served.size());
        assertEquals("corp-sso", served.get(0).getName());
        assertNotNull(service.resolve("corp-sso"));
        assertNull(service.resolve("some-uuid"));
    }

    /**
     * The repository rejects a duplicate name up front, but a repository
     * that does not is still not allowed to leave a name ambiguous to
     * dispatch.
     */
    @Test
    void duplicateNameFromARepositoryFailsFast() {
        RecordingHandler oauth2Handler = new RecordingHandler("oauth2");
        List<RegisteredLoginMethod> duplicates = List.of(
                method("key-1", "oauth2", "google"),
                method("key-2", "oauth2", "google"));

        DefaultLoginMethodService service = new DefaultLoginMethodService(
                List.of(oauth2Handler), new StubRepository(duplicates));

        assertThrows(IllegalStateException.class, service::listAll);
    }

    @Test
    void unknownTypeIsSkippedWithoutFailing() {
        RecordingHandler oauth2Handler = new RecordingHandler("oauth2");

        DefaultLoginMethodService service = new DefaultLoginMethodService(
                List.of(oauth2Handler),
                repository(method("key-1", "oauth2", "google"),
                        method("key-2", "mystery-type", "mystery")));

        List<LoginMethod> served = service.listAll();

        assertEquals(1, served.size());
        assertEquals("google", served.get(0).getName());
    }

    @Test
    void handlerReturningNullIsSkipped() {
        NullReturningHandler nullHandler = new NullReturningHandler("oauth2");

        DefaultLoginMethodService service = new DefaultLoginMethodService(
                List.of(nullHandler), repository(method("key-1", "oauth2", "google")));

        assertTrue(service.listAll().isEmpty());
    }

    @Test
    void emptyRepositorySynthesizesDefaultPassword() {
        RecordingHandler passwordHandler = new RecordingHandler("password");
        DefaultLoginMethodService service = new DefaultLoginMethodService(
                List.of(passwordHandler), repository());

        List<LoginMethod> served = service.listAll();

        assertEquals(1, served.size());
        assertEquals("password", served.get(0).getName());
        assertEquals(List.of("password"), passwordHandler.namesSeen);
    }

    /**
     * Identity, type and name are what make a registration usable, so
     * none of them may be blank.
     */
    @Test
    void registrationWithoutTypeIdOrNameIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> method("key-1", "", "google"));
        assertThrows(IllegalArgumentException.class, () -> method("", "oauth2", "google"));
        assertThrows(IllegalArgumentException.class, () -> method("key-1", "oauth2", null));
        assertThrows(IllegalArgumentException.class, () -> method("key-1", "oauth2", " "));
        assertThrows(IllegalArgumentException.class,
                () -> new RegisteredPasswordLoginMethod("key-1", null, false));
    }

    @Test
    void duplicateTypeFailsFast() {
        RecordingHandler a = new RecordingHandler("oauth2");
        RecordingHandler b = new RecordingHandler("oauth2");
        assertThrows(IllegalStateException.class,
                () -> new DefaultLoginMethodService(List.of(a, b), repository()));
    }

    @Test
    void resolveReturnsNullForUnknownName() {
        RecordingHandler passwordHandler = new RecordingHandler("password");
        DefaultLoginMethodService service = new DefaultLoginMethodService(
                List.of(passwordHandler), repository());

        assertNull(service.resolve("nonexistent"));
    }

    @Test
    void resolveReturnsEntryForKnownName() {
        RecordingHandler oauth2Handler = new RecordingHandler("oauth2");

        DefaultLoginMethodService service = new DefaultLoginMethodService(
                List.of(oauth2Handler), repository(method("some-uuid", "oauth2", "google")));

        DefaultLoginMethodService.ResolvedLoginMethod resolved = service.resolve("google");
        assertNotNull(resolved);
        assertEquals("google", resolved.method().getName());
        assertEquals("some-uuid", resolved.method().getId());
        assertEquals("oauth2", resolved.method().getType());
        assertSame(oauth2Handler, resolved.handler());
    }

    // ---- helpers ----

    private static RegisteredLoginMethodRepository repository(RegisteredLoginMethod... registrations) {
        return new DefaultRegisteredLoginMethodRepository(registrations);
    }

    private static RegisteredLoginMethod method(String id, String type, String name) {
        return new TestRegistration(type, id, name);
    }

    /**
     * A registration of an arbitrary type: the service itself is
     * type-agnostic, so its tests need not pick a concrete type.
     */
    private static final class TestRegistration extends RegisteredLoginMethod {

        TestRegistration(String type, String id, String name) {
            super(type, id, name, null, false);
        }
    }

    /**
     * A repository handing out whatever it was given, including what the
     * default one would refuse to store.
     */
    private record StubRepository(List<RegisteredLoginMethod> registrations)
            implements RegisteredLoginMethodRepository {

        @Override
        public void save(RegisteredLoginMethod loginMethod) {
            throw new UnsupportedOperationException("read-only stub");
        }

        @Override
        public RegisteredLoginMethod findById(String id) {
            return this.registrations.stream()
                    .filter(registration -> registration.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<RegisteredLoginMethod> findAll() {
            return this.registrations;
        }
    }

    private static final class RecordingHandler implements LoginMethodHandler {
        private final String type;
        final List<String> namesSeen = new java.util.ArrayList<>();

        RecordingHandler(String type) {
            this.type = type;
        }

        @Override
        public String type() {
            return type;
        }

        @Override
        public LoginMethod describe(RegisteredLoginMethod method) {
            namesSeen.add(method.getName());
            return LoginMethod.withType(this.type).name(method.getName()).build();
        }

        @Override
        public LoginMethodDispatch dispatch(RegisteredLoginMethod method, HttpServletRequest request) {
            return LoginMethodDispatch.notImplemented();
        }
    }

    private static final class NullReturningHandler implements LoginMethodHandler {
        private final String type;

        NullReturningHandler(String type) {
            this.type = type;
        }

        @Override
        public String type() {
            return type;
        }

        @Override
        public LoginMethod describe(RegisteredLoginMethod method) {
            return null;
        }

        @Override
        public LoginMethodDispatch dispatch(RegisteredLoginMethod method, HttpServletRequest request) {
            return LoginMethodDispatch.notImplemented();
        }
    }
}
