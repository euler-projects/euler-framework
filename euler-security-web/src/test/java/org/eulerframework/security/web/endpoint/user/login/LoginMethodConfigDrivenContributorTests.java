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
package org.eulerframework.security.web.endpoint.user.login;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LoginMethodConfigDrivenContributorTests {

    @Test
    void dispatchesEachEntryToMatchingType() {
        RecordingHandler oauth2Handler = new RecordingHandler("oauth2");
        RecordingHandler otpHandler = new RecordingHandler("otp");

        List<RegisteredLoginMethod> methods = List.of(
                method("key-1", "oauth2", Map.of("k", "v1")),
                method("key-2", "otp", Map.of("channel", "sms")));

        LoginMethodConfigDrivenContributor contributor =
                new LoginMethodConfigDrivenContributor(List.of(oauth2Handler, otpHandler), () -> methods);

        List<LoginMethod> contributed = contributor.contribute();

        assertAll("both types invoked in declaration order under derived names",
                () -> assertEquals(2, contributed.size()),
                () -> assertEquals("oauth2", contributed.get(0).getName()),
                () -> assertEquals("otp", contributed.get(1).getName()),
                () -> assertEquals(List.of("oauth2"), oauth2Handler.namesSeen),
                () -> assertEquals(List.of("otp"), otpHandler.namesSeen));
    }

    @Test
    void declaredMethodNameTakesPrecedenceOverDerivation() {
        RecordingHandler oauth2Handler = new RecordingHandler("oauth2");
        List<RegisteredLoginMethod> methods = List.of(
                RegisteredLoginMethod.withId("some-uuid").type("oauth2").name("corp-sso").build());

        LoginMethodConfigDrivenContributor contributor =
                new LoginMethodConfigDrivenContributor(List.of(oauth2Handler), () -> methods);

        List<LoginMethod> contributed = contributor.contribute();

        assertEquals(1, contributed.size());
        assertEquals("corp-sso", contributed.get(0).getName());
        assertNotNull(contributor.resolve("corp-sso"));
        assertNull(contributor.resolve("some-uuid"));
    }

    @Test
    void duplicateEffectiveNameFailsFast() {
        RecordingHandler oauth2Handler = new RecordingHandler("oauth2");
        List<RegisteredLoginMethod> methods = List.of(
                method("key-1", "oauth2", Map.of()),
                method("key-2", "oauth2", Map.of()));

        LoginMethodConfigDrivenContributor contributor =
                new LoginMethodConfigDrivenContributor(List.of(oauth2Handler), () -> methods);

        assertThrows(IllegalStateException.class, contributor::contribute);
    }

    @Test
    void underivableNameIsSkipped() {
        NamelessHandler namelessHandler = new NamelessHandler("oauth2");
        List<RegisteredLoginMethod> methods = List.of(method("key-1", "oauth2", Map.of()));

        LoginMethodConfigDrivenContributor contributor =
                new LoginMethodConfigDrivenContributor(List.of(namelessHandler), () -> methods);

        assertTrue(contributor.contribute().isEmpty());
    }

    @Test
    void unknownTypeIsSkippedWithoutFailing() {
        RecordingHandler oauth2Handler = new RecordingHandler("oauth2");
        List<RegisteredLoginMethod> methods = List.of(
                method("key-1", "oauth2", Map.of()),
                method("key-2", "mystery-type", Map.of()));

        LoginMethodConfigDrivenContributor contributor =
                new LoginMethodConfigDrivenContributor(List.of(oauth2Handler), () -> methods);

        List<LoginMethod> contributed = contributor.contribute();

        assertEquals(1, contributed.size());
        assertEquals("oauth2", contributed.get(0).getName());
    }

    @Test
    void handlerReturningNullIsSkipped() {
        NullReturningHandler nullHandler = new NullReturningHandler("oauth2");
        List<RegisteredLoginMethod> methods = List.of(method("key-1", "oauth2", Map.of()));

        LoginMethodConfigDrivenContributor contributor =
                new LoginMethodConfigDrivenContributor(List.of(nullHandler), () -> methods);

        assertTrue(contributor.contribute().isEmpty());
    }

    @Test
    void emptyConfigSynthesizesDefaultPassword() {
        RecordingHandler passwordHandler = new RecordingHandler("password");
        LoginMethodConfigDrivenContributor contributor =
                new LoginMethodConfigDrivenContributor(List.of(passwordHandler), List::of);

        List<LoginMethod> contributed = contributor.contribute();

        assertEquals(1, contributed.size());
        assertEquals("password", contributed.get(0).getName());
        assertEquals(List.of("password"), passwordHandler.namesSeen);
    }

    @Test
    void registrationWithoutIdOrTypeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> RegisteredLoginMethod.withId(""));
        assertThrows(IllegalArgumentException.class, () -> RegisteredLoginMethod.withId("key-1").build());
    }

    @Test
    void duplicateTypeFailsFast() {
        RecordingHandler a = new RecordingHandler("oauth2");
        RecordingHandler b = new RecordingHandler("oauth2");
        assertThrows(IllegalStateException.class,
                () -> new LoginMethodConfigDrivenContributor(List.of(a, b), List::of));
    }

    @Test
    void resolveReturnsNullForUnknownName() {
        RecordingHandler passwordHandler = new RecordingHandler("password");
        LoginMethodConfigDrivenContributor contributor =
                new LoginMethodConfigDrivenContributor(List.of(passwordHandler), List::of);

        assertNull(contributor.resolve("nonexistent"));
    }

    @Test
    void resolveReturnsEntryForKnownName() {
        RecordingHandler oauth2Handler = new RecordingHandler("oauth2");
        List<RegisteredLoginMethod> methods = List.of(method("some-uuid", "oauth2", Map.of()));

        LoginMethodConfigDrivenContributor contributor =
                new LoginMethodConfigDrivenContributor(List.of(oauth2Handler), () -> methods);

        LoginMethodConfigDrivenContributor.ResolvedLoginMethod resolved = contributor.resolve("oauth2");
        assertNotNull(resolved);
        assertEquals("oauth2", resolved.name());
        assertEquals("some-uuid", resolved.method().getId());
        assertEquals("oauth2", resolved.method().getType());
        assertSame(oauth2Handler, resolved.handler());
    }

    // ---- helpers ----

    private static RegisteredLoginMethod method(String id, String type, Map<String, Object> properties) {
        return RegisteredLoginMethod.withId(id)
                .type(type)
                .properties(properties)
                .build();
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
        public LoginMethod describe(String name, RegisteredLoginMethod method) {
            namesSeen.add(name);
            return LoginMethod.withType(this.type).name(name).build();
        }

        @Override
        public LoginMethodDispatch dispatch(String name, RegisteredLoginMethod method, HttpServletRequest request) {
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
        public LoginMethod describe(String name, RegisteredLoginMethod method) {
            return null;
        }

        @Override
        public LoginMethodDispatch dispatch(String name, RegisteredLoginMethod method, HttpServletRequest request) {
            return LoginMethodDispatch.notImplemented();
        }
    }

    private static final class NamelessHandler implements LoginMethodHandler {
        private final String type;

        NamelessHandler(String type) {
            this.type = type;
        }

        @Override
        public String type() {
            return type;
        }

        @Override
        public String resolveName(RegisteredLoginMethod method) {
            return null;
        }

        @Override
        public LoginMethod describe(String name, RegisteredLoginMethod method) {
            return LoginMethod.withType(this.type).name(name).build();
        }

        @Override
        public LoginMethodDispatch dispatch(String name, RegisteredLoginMethod method, HttpServletRequest request) {
            return LoginMethodDispatch.notImplemented();
        }
    }
}
