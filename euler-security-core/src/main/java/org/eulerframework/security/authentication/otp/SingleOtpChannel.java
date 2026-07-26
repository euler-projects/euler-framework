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

/**
 * An {@link OtpChannel} bound to exactly one logical channel - the natural
 * contract for a concrete delivery gateway (an SMS provider, a mail
 * provider, ...).
 * <p>
 * Implementations declare their channel name via {@link #getChannel()}, and
 * {@link #supports(String)} is derived from it by case-insensitive
 * comparison, so a single-channel gateway only implements {@code getChannel}
 * and {@code send}. Channel-agnostic implementations (composites such as
 * {@link DelegatingOtpChannel}, fallbacks such as {@link StdoutOtpChannel})
 * implement {@link OtpChannel} directly instead - the channel name accessor
 * simply does not exist on their type.
 *
 * @see AbstractAsyncOtpChannel
 */
public interface SingleOtpChannel extends OtpChannel {

    /**
     * The single logical channel name this implementation delivers for
     * (e.g. {@code sms}, {@code email}).
     *
     * @return the channel name; never {@code null}
     */
    String getChannel();

    /**
     * Case-insensitive comparison against {@link #getChannel()}.
     */
    @Override
    default boolean supports(String channel) {
        return getChannel().equalsIgnoreCase(channel);
    }
}
