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

class AbstractAsyncOtpChannelTest {

    private static final OtpDelivering DELIVERING = new OtpDelivering(
            "sms", "+8613800138000", "login", "123456", Duration.ofMinutes(5));

    @Test
    void unsupportedChannelIsRejectedSynchronouslyWithoutDispatch() {
        AtomicInteger dispatched = new AtomicInteger();
        AtomicInteger delivered = new AtomicInteger();
        // Declares "email" while the delivery instruction targets "sms", so the
        // default supports() derived from getChannel() rejects it synchronously.
        AbstractAsyncOtpChannel channel = new AbstractAsyncOtpChannel(task -> dispatched.incrementAndGet()) {
            @Override
            public String getChannel() {
                return "email";
            }

            @Override
            protected void doSend(OtpDelivering delivering) {
                delivered.incrementAndGet();
            }
        };

        Assertions.assertThrows(OtpChannelNotFoundException.class, () -> channel.send(DELIVERING));
        Assertions.assertEquals(0, dispatched.get());
        Assertions.assertEquals(0, delivered.get());
    }

    @Test
    void doSendRunsOnTheConfiguredExecutor() {
        AtomicInteger dispatched = new AtomicInteger();
        List<OtpDelivering> delivered = new ArrayList<>();
        Executor directExecutor = task -> {
            dispatched.incrementAndGet();
            task.run();
        };
        AbstractAsyncOtpChannel channel = new AbstractAsyncOtpChannel(directExecutor) {
            @Override
            public String getChannel() {
                return "sms";
            }

            @Override
            protected void doSend(OtpDelivering delivering) {
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
        OtpDeliveryException failure = new OtpDeliveryException("delivery failed");
        AbstractAsyncOtpChannel channel = new AbstractAsyncOtpChannel(Runnable::run) {
            @Override
            public String getChannel() {
                return "sms";
            }

            @Override
            protected void doSend(OtpDelivering delivering) throws OtpDeliveryException {
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
        AbstractAsyncOtpChannel channel = new AbstractAsyncOtpChannel(Runnable::run) {
            @Override
            public String getChannel() {
                return "sms";
            }

            @Override
            protected void doSend(OtpDelivering delivering) {
                throw new IllegalStateException("provider blew up");
            }
        };

        CompletableFuture<Void> future = Assertions.assertDoesNotThrow(() -> channel.send(DELIVERING));

        Assertions.assertTrue(future.isCompletedExceptionally());
    }
}
