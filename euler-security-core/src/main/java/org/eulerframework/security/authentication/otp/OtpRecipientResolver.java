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
 * Resolves the {@code identityId} of an authentication factor (e.g. a phone
 * binding's id, an email binding's id) into a concrete {@code recipient} that
 * an {@link OtpChannel} can address (a phone number, an email address, ...).
 * <p>
 * Identity factor binding storage lives in the application's user-profile
 * subsystem; the framework intentionally provides only the SPI here and does
 * not ship a default implementation. When the {@code /otp/tickets} endpoint
 * receives an {@code identity_id} but no
 * {@code OtpRecipientResolver} bean is registered, the request is rejected
 * with the {@code invalid_identity_id} error.
 */
@FunctionalInterface
public interface OtpRecipientResolver {

    /**
     * Resolve the given {@code identityId} into a channel-addressable recipient.
     *
     * @param identityId the identity factor id to resolve
     * @return the recipient (phone number, email, ...) bound to that identity,
     *         or {@code null} if no such binding exists
     */
    String resolve(String identityId);
}
