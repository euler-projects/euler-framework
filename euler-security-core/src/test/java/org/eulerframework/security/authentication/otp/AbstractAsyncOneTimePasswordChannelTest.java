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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

class AbstractAsyncOneTimePasswordChannelTest {

    private static final OneTimePasswordDelivering DELIVERING = new OneTimePasswordDelivering(
            "sms", "+8613800138000", "login", "123456", Duration.ofMinutes(5));

    @Test
    void unsupportedChannelIsRejectedSynchronouslyWithoutDispatch() {
        AtomicInteger dispatched = new AtomicInteger();
        AtomicInteger delivered = new AtomicInteger();
        // Declares "email" while the delivery instruction targets "sms", so the
        // default supports() derived from getChannel() rejects it synchronously.
        AbstractAsyncOneTimePasswordChannel channel = new AbstractAsyncOneTimePasswordChannel(task -> dispatched.incrementAndGet()) {
            @Override
            public String getChannel() {
                return "email";
            }

            @Override
            protected void doSend(OneTimePasswordDelivering delivering) {
                delivered.incrementAndGet();
            }
        };

        Assertions.assertThrows(OneTimePasswordChannelNotFoundException.class, () -> channel.send(DELIVERING));
        Assertions.assertEquals(0, dispatched.get());
        Assertions.assertEquals(0, delivered.get());
    }

    @Test
    void doSendRunsOnTheConfiguredExecutor() {
        AtomicInteger dispatched = new AtomicInteger();
        List<OneTimePasswordDelivering> delivered = new ArrayList<>();
        Executor directExecutor = task -> {
            dispatched.incrementAndGet();
            task.run();
        };
        AbstractAsyncOneTimePasswordChannel channel = new AbstractAsyncOneTimePasswordChannel(directExecutor) {
            @Override
            public String getChannel() {
                return "sms";
            }

            @Override
            protected void doSend(OneTimePasswordDelivering delivering) {
                delivered.add(delivering);
            }
        };

        CompletableFuture<Void> future = channel.send(DELIVERING);

        Assertions.assertTrue(future.isDone());
        Assertions.assertFalse(future.isCompletedExceptionally());
        Assertions.assertEquals(1, dispatched.get());
        Assertions.assertEquals(List.of(DELIVERING), delivered);
    }

    @Test
    void deliveryFailureCompletesTheFutureExceptionally() {
        OneTimePasswordDeliveryException failure = new OneTimePasswordDeliveryException("delivery failed");
        AbstractAsyncOneTimePasswordChannel channel = new AbstractAsyncOneTimePasswordChannel(Runnable::run) {
            @Override
            public String getChannel() {
                return "sms";
            }

            @Override
            protected void doSend(OneTimePasswordDelivering delivering) throws OneTimePasswordDeliveryException {
                throw failure;
            }
        };

        CompletableFuture<Void> future = Assertions.assertDoesNotThrow(() -> channel.send(DELIVERING));

        Assertions.assertTrue(future.isCompletedExceptionally());
        ExecutionException wrapped = Assertions.assertThrows(ExecutionException.class, future::get);
        Assertions.assertSame(failure, wrapped.getCause());
    }

    @Test
    void runtimeDeliveryFailureIsNotThrownFromSend() {
        AbstractAsyncOneTimePasswordChannel channel = new AbstractAsyncOneTimePasswordChannel(Runnable::run) {
            @Override
            public String getChannel() {
                return "sms";
            }

            @Override
            protected void doSend(OneTimePasswordDelivering delivering) {
                throw new IllegalStateException("provider blew up");
            }
        };

        CompletableFuture<Void> future = Assertions.assertDoesNotThrow(() -> channel.send(DELIVERING));

        Assertions.assertTrue(future.isCompletedExceptionally());
    }
}
