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
package org.eulerframework.security.authentication.otp;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

class DelegatingOneTimePasswordChannelTest {

    private static final OneTimePasswordDelivering DELIVERING = new OneTimePasswordDelivering(
            "sms", "+8613800138000", "login", "123456", Duration.ofMinutes(5));

    private static OneTimePasswordChannel channel(String name) {
        return new SingleOneTimePasswordChannel() {
            @Override
            public CompletableFuture<Void> send(OneTimePasswordDelivering delivering) {
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public String getChannel() {
                return name;
            }
        };
    }

    @Test
    void supportsReflectsRoutesAndFallback() {
        DelegatingOneTimePasswordChannel noFallback = new DelegatingOneTimePasswordChannel(Map.of("sms", channel("sms")));
        DelegatingOneTimePasswordChannel withFallback = new DelegatingOneTimePasswordChannel(Map.of(), new StdoutOneTimePasswordChannel());

        Assertions.assertTrue(noFallback.supports("sms"));
        Assertions.assertFalse(noFallback.supports("email"));
        Assertions.assertTrue(withFallback.supports("email"));
    }

    @Test
    void routingIsCaseInsensitive() {
        DelegatingOneTimePasswordChannel delegating = new DelegatingOneTimePasswordChannel(Map.of("SMS", channel("sms")));

        Assertions.assertTrue(delegating.supports("sms"));
        Assertions.assertTrue(delegating.supports("Sms"));

        CompletableFuture<Void> future = delegating.send(DELIVERING);

        Assertions.assertTrue(future.isDone());
        Assertions.assertFalse(future.isCompletedExceptionally());
    }

    @Test
    void routesDifferingOnlyInCaseAreRejected() {
        Map<String, OneTimePasswordChannel> routes = Map.of(
                "sms", channel("sms"),
                "SMS", channel("sms"));

        Assertions.assertThrows(IllegalArgumentException.class, () -> new DelegatingOneTimePasswordChannel(routes));
    }

    @Test
    void routeMissWithoutFallbackThrowsSynchronously() {
        DelegatingOneTimePasswordChannel delegating = new DelegatingOneTimePasswordChannel(Map.of("email", channel("email")));

        Assertions.assertThrows(OneTimePasswordChannelNotFoundException.class, () -> delegating.send(DELIVERING));
    }

    @Test
    void routeHitDelegatesToTheMatchedChannel() {
        DelegatingOneTimePasswordChannel delegating = new DelegatingOneTimePasswordChannel(Map.of("sms", channel("sms")));

        CompletableFuture<Void> future = delegating.send(DELIVERING);

        Assertions.assertTrue(future.isDone());
        Assertions.assertFalse(future.isCompletedExceptionally());
    }
}
