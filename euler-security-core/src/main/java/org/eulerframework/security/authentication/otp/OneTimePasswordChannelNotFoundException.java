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
 * Thrown synchronously by an {@link OneTimePasswordChannel} when the requested logical
 * channel name cannot be handled - by {@link DelegatingOneTimePasswordChannel} when the
 * name is not in its routing table and no fallback channel is configured, or
 * by {@link AbstractAsyncOneTimePasswordChannel} when {@link OneTimePasswordChannel#supports(String)}
 * rejects the name before asynchronous dispatch.
 * <p>
 * Maps to the {@code unsupported_channel} HTTP error in
 * {@code OneTimePasswordIssueEndpointFilter}.
 */
public class OneTimePasswordChannelNotFoundException extends RuntimeException {

    public OneTimePasswordChannelNotFoundException(String channel) {
        super("No OneTimePasswordChannel supports channel '" + channel + "'");
    }
}
