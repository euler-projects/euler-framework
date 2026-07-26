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
 * Indicates that delivery of an OTP value failed.
 * <p>
 * Under the asynchronous {@link OtpChannel} contract this exception is not
 * thrown from {@link OtpChannel#send(OtpDelivering)}; it completes the
 * returned future exceptionally (blocking implementations such as
 * {@link AbstractAsyncOtpChannel} subclasses throw it from their
 * {@code doSend} hook and the template wraps it into the future).
 */
public class OtpDeliveryException extends Exception {

    public OtpDeliveryException(String message) {
        super(message);
    }

    public OtpDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
