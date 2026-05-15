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

import java.util.Map;

/**
 * Composite {@link OtpChannel} that routes an {@link OtpDelivering} to one of
 * a set of registered channels, looked up by
 * {@link OtpDelivering#channel()} bean name.
 * <p>
 * Naming and design follow Spring's
 * {@code DelegatingFilterProxy} / {@code DelegatingPasswordEncoder} pattern:
 * the framework operates against a single {@code OtpChannel} entry-point,
 * while business code is free to register as many backing channels as needed
 * (one per Spring bean, where the bean name is the channel name).
 * <p>
 * If no entry matches the requested channel:
 * <ul>
 *     <li>and a {@code fallback} channel was supplied at construction time,
 *         delivery is delegated to it (e.g. the bundled
 *         {@link StdoutOtpChannel} during development);</li>
 *     <li>otherwise an {@link OtpChannelNotFoundException} is thrown, which
 *         the issue endpoint surfaces as the {@code unsupported_channel}
 *         error.</li>
 * </ul>
 */
public class DelegatingOtpChannel implements OtpChannel {

    private final Map<String, OtpChannel> routes;
    private final OtpChannel fallback;

    /**
     * Create a delegator with no fallback - any unregistered channel name
     * will result in {@link OtpChannelNotFoundException}.
     *
     * @param routes the routing table from channel name to channel
     */
    public DelegatingOtpChannel(Map<String, OtpChannel> routes) {
        this(routes, null);
    }

    /**
     * Create a delegator with an explicit fallback channel.
     *
     * @param routes   the routing table from channel name to channel
     * @param fallback the channel to use when no route matches; may be
     *                 {@code null}
     */
    public DelegatingOtpChannel(Map<String, OtpChannel> routes, OtpChannel fallback) {
        Assert.notNull(routes, "routes must not be null");
        this.routes = Map.copyOf(routes);
        this.fallback = fallback;
    }

    @Override
    public void send(OtpDelivering delivering) throws OtpDeliveryException {
        Assert.notNull(delivering, "delivering must not be null");
        OtpChannel target = this.routes.get(delivering.channel());
        if (target == null) {
            if (this.fallback != null) {
                this.fallback.send(delivering);
                return;
            }
            throw new OtpChannelNotFoundException(delivering.channel());
        }
        target.send(delivering);
    }
}
