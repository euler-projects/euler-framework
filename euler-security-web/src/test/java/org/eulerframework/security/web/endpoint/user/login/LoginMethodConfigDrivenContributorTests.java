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

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginMethodConfigDrivenContributorTests {

    @Test
    void dispatchesEachEntryToMatchingHandler() {
        RecordingHandler oauth2Handler = new RecordingHandler("oauth2");
        RecordingHandler otpHandler = new RecordingHandler("otp");

        Map<String, LoginMethod> methods = new LinkedHashMap<>();
        methods.put("google", method("oauth2", Map.of("k", "v1")));
        methods.put("sms", method("otp", Map.of("channel", "sms")));

        LoginMethodConfigDrivenContributor contributor =
                new LoginMethodConfigDrivenContributor(List.of(oauth2Handler, otpHandler), () -> methods);

        List<LoginMethodView> views = contributor.contribute();

        assertAll("both handlers invoked in map order",
                () -> assertEquals(2, views.size()),
                () -> assertEquals("google", views.get(0).id()),
                () -> assertEquals("sms", views.get(1).id()),
                () -> assertEquals(List.of("google"), oauth2Handler.namesSeen),
                () -> assertEquals(List.of("sms"), otpHandler.namesSeen));
    }

    @Test
    void unknownTypeIsSkippedWithoutFailing() {
        RecordingHandler oauth2Handler = new RecordingHandler("oauth2");
        Map<String, LoginMethod> methods = new LinkedHashMap<>();
        methods.put("google", method("oauth2", Map.of()));
        methods.put("mystery", method("mystery-type", Map.of()));

        LoginMethodConfigDrivenContributor contributor =
                new LoginMethodConfigDrivenContributor(List.of(oauth2Handler), () -> methods);

        List<LoginMethodView> views = contributor.contribute();

        assertEquals(1, views.size());
        assertEquals("google", views.get(0).id());
    }

    @Test
    void handlerReturningNullIsSkipped() {
        NullReturningHandler nullHandler = new NullReturningHandler("oauth2");
        Map<String, LoginMethod> methods = Map.of("google", method("oauth2", Map.of()));

        LoginMethodConfigDrivenContributor contributor =
                new LoginMethodConfigDrivenContributor(List.of(nullHandler), () -> methods);

        assertTrue(contributor.contribute().isEmpty());
    }

    @Test
    void emptyMapProducesEmptyList() {
        LoginMethodConfigDrivenContributor contributor =
                new LoginMethodConfigDrivenContributor(List.of(), Map::of);
        assertTrue(contributor.contribute().isEmpty());
    }

    @Test
    void entryWithoutTypeIsSkipped() {
        Map<String, LoginMethod> methods = new LinkedHashMap<>();
        LoginMethod incomplete = new LoginMethod();
        // no type set
        methods.put("bad", incomplete);
        LoginMethodConfigDrivenContributor contributor =
                new LoginMethodConfigDrivenContributor(List.of(), () -> methods);

        assertTrue(contributor.contribute().isEmpty());
    }

    @Test
    void duplicateHandlerForSameTypeFailsFast() {
        RecordingHandler a = new RecordingHandler("oauth2");
        RecordingHandler b = new RecordingHandler("oauth2");
        assertThrows(IllegalStateException.class,
                () -> new LoginMethodConfigDrivenContributor(List.of(a, b), Map::of));
    }

    // ---- helpers ----

    private static LoginMethod method(String type, Map<String, Object> properties) {
        LoginMethod method = new LoginMethod();
        method.setType(type);
        method.getProperties().putAll(properties);
        return method;
    }

    /**
     * A {@link LoginMethodTypeHandler} that emits a minimal view whose
     * {@code id} equals the login-method name, so the dispatcher's
     * invocation order can be asserted from the returned list.
     */
    private static final class RecordingHandler implements LoginMethodTypeHandler {
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
        public LoginMethodView toView(String name, Map<String, Object> properties) {
            namesSeen.add(name);
            return LoginMethodView.builder(this.type).id(name).build();
        }
    }

    private static final class NullReturningHandler implements LoginMethodTypeHandler {
        private final String type;

        NullReturningHandler(String type) {
            this.type = type;
        }

        @Override
        public String type() {
            return type;
        }

        @Override
        public LoginMethodView toView(String name, Map<String, Object> properties) {
            return null;
        }
    }
}
