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
package org.eulerframework.security.web.login;

/**
 * A registered {@code otp} login method: one-time-password login,
 * served by {@link OtpLoginMethodHandler}.
 *
 * <p>Several registrations of this type may coexist, one per delivery
 * channel.
 */
public final class RegisteredOtpLoginMethod extends RegisteredLoginMethod {

    /** The {@code method-type} this registration is declared under. */
    public static final String TYPE = "otp";

    private final String channel;

    /**
     * @param id           the identifier this registration is stored under
     * @param name         the name clients address this method by
     * @param identityType the identity type this method establishes,
     *                     e.g. {@code phone} or {@code email}
     * @param primary      whether the method belongs to the primary group
     * @param channel      the delivery channel, or {@code null} to have
     *                     it mapped from the identity type
     */
    public RegisteredOtpLoginMethod(String id, String name, String identityType,
                                    boolean primary, String channel) {
        super(TYPE, id, name, identityType, primary);
        this.channel = channel;
    }

    /**
     * The channel the one-time password is delivered over, e.g.
     * {@code sms} or {@code email}. {@code null} leaves it to be mapped
     * from the {@link #getIdentityType() identity type}.
     */
    public String getChannel() {
        return this.channel;
    }
}
