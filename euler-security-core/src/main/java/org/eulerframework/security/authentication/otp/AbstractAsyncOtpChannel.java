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

import org.springframework.util.Assert;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * Template {@link SingleOtpChannel} base class that dispatches delivery to an
 * {@link Executor}, so subclasses only declare their channel name via
 * {@link #getChannel()} and write plain blocking code in
 * {@link #doSend(OtpDelivering)}, never dealing with threading themselves.
 * <p>
 * {@link #send(OtpDelivering)} rejects unsupported channel names
 * synchronously via {@link #supports(String)} and then executes
 * {@code doSend} on the executor, completing the returned future with the
 * delivery outcome. This decouples the issue-endpoint response time from the
 * provider round-trip: {@link OtpTicketIssueAuthenticationProvider} responds
 * in near-constant time whether the delivery is slow, fails, or is skipped
 * entirely for {@link OtpTestAccountSupport test accounts}, leaving no
 * timing side channel for recipient probing.
 * <p>
 * Extending this class is optional. Implementations that need full control
 * over their threading - or that deliberately deliver synchronously - can
 * implement {@link SingleOtpChannel} directly.
 */
public abstract class AbstractAsyncOtpChannel implements SingleOtpChannel {

    private final Executor executor;

    /**
     * Create a channel that dispatches deliveries to
     * {@link ForkJoinPool#commonPool()}. Suitable for demos and tests; for
     * blocking-I/O providers in production, prefer
     * {@link #AbstractAsyncOtpChannel(Executor)} with a dedicated executor.
     */
    protected AbstractAsyncOtpChannel() {
        this(ForkJoinPool.commonPool());
    }

    /**
     * @param executor the executor deliveries are dispatched to; must not be
     *                 {@code null}
     */
    protected AbstractAsyncOtpChannel(Executor executor) {
        Assert.notNull(executor, "executor must not be null");
        this.executor = executor;
    }

    @Override
    public final CompletableFuture<Void> send(OtpDelivering delivering) {
        Assert.notNull(delivering, "delivering must not be null");
        if (!supports(delivering.channel())) {
            throw new OtpChannelNotFoundException(delivering.channel());
        }
        CompletableFuture<Void> result = new CompletableFuture<>();
        this.executor.execute(() -> {
            try {
                doSend(delivering);
                result.complete(null);
            } catch (Throwable t) {
                result.completeExceptionally(t);
            }
        });
        return result;
    }

    /**
     * Perform the actual (typically blocking) delivery. Invoked on the
     * configured executor, never on the calling thread.
     *
     * @param delivering the delivery instruction
     * @throws OtpDeliveryException if delivery fails
     */
    protected abstract void doSend(OtpDelivering delivering) throws OtpDeliveryException;
}
