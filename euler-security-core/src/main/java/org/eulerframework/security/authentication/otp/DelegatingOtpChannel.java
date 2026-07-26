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

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Composite {@link OtpChannel} that routes an {@link OtpDelivering} to one of
 * a set of registered channels, looked up case-insensitively by
 * {@link OtpDelivering#channel()}.
 * <p>
 * Naming and design follow Spring's
 * {@code DelegatingFilterProxy} / {@code DelegatingPasswordEncoder} pattern:
 * the framework operates against a single {@code OtpChannel} entry-point,
 * while business code is free to register as many backing channels as needed.
 * As a channel-name-keyed composite it implements the channel-agnostic
 * {@link OtpChannel} contract directly and exposes no single channel name.
 * <p>
 * If no entry matches the requested channel:
 * <ul>
 *     <li>and a {@code fallback} channel was supplied at construction time,
 *         delivery is delegated to it (e.g. the bundled
 *         {@link StdoutOtpChannel} during development);</li>
 *     <li>otherwise an {@link OtpChannelNotFoundException} is thrown
 *         synchronously, which the issue endpoint surfaces as the
 *         {@code unsupported_channel} error.</li>
 * </ul>
 * {@link #supports(String)} reflects the same routing decision, and the
 * delivery future returned by the matched channel is passed through as-is.
 * Route keys are normalized to lower case at construction time and lookups
 * are normalized likewise, matching the case-insensitive semantics of
 * {@link SingleOtpChannel#supports(String)}.
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
     * @param routes   the routing table from channel name to channel; keys
     *                 are normalized to lower case, so entries that only
     *                 differ in case are rejected
     * @param fallback the channel to use when no route matches; may be
     *                 {@code null}
     */
    public DelegatingOtpChannel(Map<String, OtpChannel> routes, OtpChannel fallback) {
        Assert.notNull(routes, "routes must not be null");
        Map<String, OtpChannel> normalized = new LinkedHashMap<>();
        routes.forEach((channel, target) -> {
            Assert.hasText(channel, "route channel name must not be empty");
            Assert.notNull(target, "route target must not be null");
            OtpChannel previous = normalized.putIfAbsent(normalizeChannel(channel), target);
            Assert.isNull(previous,
                    () -> "Duplicate route for channel '" + channel + "' after case normalization");
        });
        this.routes = Map.copyOf(normalized);
        this.fallback = fallback;
    }

    @Override
    public CompletableFuture<Void> send(OtpDelivering delivering) {
        Assert.notNull(delivering, "delivering must not be null");
        OtpChannel target = this.routes.get(normalizeChannel(delivering.channel()));
        if (target == null) {
            if (this.fallback != null) {
                return this.fallback.send(delivering);
            }
            throw new OtpChannelNotFoundException(delivering.channel());
        }
        return target.send(delivering);
    }

    @Override
    public boolean supports(String channel) {
        return (channel != null && this.routes.containsKey(normalizeChannel(channel)))
                || (this.fallback != null && this.fallback.supports(channel));
    }

    private static String normalizeChannel(String channel) {
        return channel.toLowerCase(Locale.ROOT);
    }
}
