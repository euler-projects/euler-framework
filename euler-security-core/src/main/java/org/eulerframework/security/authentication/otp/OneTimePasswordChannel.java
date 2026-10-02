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

import java.util.concurrent.CompletableFuture;

/**
 * Strategy that delivers an OTP value to a recipient.
 * <p>
 * This base contract only knows how to deliver
 * ({@link #send(OneTimePasswordDelivering)}) and whether a logical channel name can be
 * handled ({@link #supports(String)}); both are mandatory. Concrete gateway
 * implementations bound to exactly one logical channel should implement
 * {@link SingleOneTimePasswordChannel}, which adds the self-declared channel name and
 * derives {@code supports} from it. Channel-agnostic implementations - the
 * {@link DelegatingOneTimePasswordChannel} composite and the {@link StdoutOneTimePasswordChannel}
 * development fallback - implement this interface directly. Routing from a
 * logical channel name to the right implementation is the job of
 * {@link DelegatingOneTimePasswordChannel}.
 *
 * <h2>Asynchronous contract</h2>
 * {@link #send(OneTimePasswordDelivering)} returns a {@link CompletableFuture} tracking
 * the delivery outcome. Synchronous exceptions are reserved for routing and
 * argument errors (e.g. {@link OneTimePasswordChannelNotFoundException} when
 * {@link #supports(String)} rejects the channel name); delivery failures must
 * complete the future exceptionally - typically with
 * {@link OneTimePasswordDeliveryException} - and must not be thrown from {@code send}.
 * <p>
 * Implementations are free to perform the delivery on the calling thread and
 * return an already-completed future. Such implementations block the caller
 * and expose the provider round-trip in the issue-endpoint response time;
 * {@link AbstractAsyncOneTimePasswordChannel} is the ready-made template for
 * implementations that want proper asynchronous dispatch without writing any
 * threading code.
 *
 * @see SingleOneTimePasswordChannel
 * @see AbstractAsyncOneTimePasswordChannel
 * @see DelegatingOneTimePasswordChannel
 * @see StdoutOneTimePasswordChannel
 */
public interface OneTimePasswordChannel {

    /**
     * Deliver the OTP described by {@code delivering} to its recipient.
     *
     * @param delivering the delivery instruction
     * @return a future that completes when delivery finishes, or completes
     * exceptionally (typically with {@link OneTimePasswordDeliveryException}) when
     * delivery fails
     * @throws OneTimePasswordChannelNotFoundException if the requested channel cannot be
     *                                     handled by this instance
     */
    CompletableFuture<Void> send(OneTimePasswordDelivering delivering);

    /**
     * Whether this instance can handle deliveries for the given logical
     * channel name. Consulted synchronously before any asynchronous dispatch
     * so that unsupported channels are rejected on the calling thread.
     *
     * @param channel the logical channel name from the issue request
     * @return {@code true} if {@link #send(OneTimePasswordDelivering)} can handle the
     * channel
     */
    boolean supports(String channel);
}
