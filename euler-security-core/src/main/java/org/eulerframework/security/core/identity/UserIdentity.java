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
package org.eulerframework.security.core.identity;

import org.springframework.util.Assert;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immutable model of a user identity.
 *
 * <p>A {@code UserIdentity} represents one external addressing of a user
 * &mdash; a phone number, an email address, an IdP-issued subject, and so
 * on. A user may own several identities of mixed types.
 *
 * <p>The record carries two distinct shapes; callers disambiguate by
 * context:
 * <dl>
 *   <dt><em>Persisted</em></dt>
 *   <dd>Returned by a {@link UserIdentityService} after a successful
 *       read or write. All envelope fields ({@link #identityId},
 *       {@link #subject}, {@link #userId}, {@link #boundAt}) are
 *       populated; {@link #extensions} holds per-type business
 *       attributes (e.g. masked phone, WeChat nickname).</dd>
 *
 *   <dt><em>Prototype</em></dt>
 *   <dd>Supplied to a {@link UserIdentityService} by callers that have
 *       already verified ownership of the underlying value through
 *       another channel. Only {@link #identityType} and
 *       {@link #extensions} are meaningful; the remaining fields must
 *       be {@code null}. The backend mints {@link #identityId},
 *       derives {@link #subject}, stamps {@link #boundAt}, and pairs
 *       the supplied user id into the returned persisted instance.</dd>
 * </dl>
 *
 * <p>The canonical constructor enforces only invariants shared by both
 * shapes: {@link #identityType} is required, {@link #extensions} is
 * defensively copied and never {@code null}.
 *
 * @param identityId   opaque framework identifier (UUID) of the
 *                     binding; unique across all identities; stable
 *                     across rebinds. {@code null} in prototype shape
 * @param identityType logical identity type
 *                     ({@code phone}, {@code email}, {@code wechat},
 *                     {@code apple}, {@code google}, ...)
 * @param subject      deterministic per-type unique key. SHA-256 hex of
 *                     the normalised original for {@code phone} and
 *                     {@code email}; the IdP-issued {@code openid} /
 *                     {@code sub} for federated types. Indexed with
 *                     {@code identityType} for cross-account
 *                     uniqueness. {@code null} in prototype shape
 * @param userId       id of the owning user; internal SPI field.
 *                     {@code null} in prototype shape
 * @param boundAt      first-bind timestamp. {@code null} in prototype
 *                     shape
 * @param extensions   identity-type-specific attributes. In persisted
 *                     shape, carries per-type business values projected
 *                     by the backend (e.g.
 *                     {@code {"phone":"+8613*****00"}};
 *                     {@code {"openid":"oX1...","nickname":"...","unionid":"..."}}).
 *                     In prototype shape, the caller places the raw
 *                     business value here under the key the backend
 *                     uses for its persisted projection (e.g.
 *                     {@code {"phone":"+86138..."}}). Never
 *                     {@code null}, may be empty
 */
public record UserIdentity(
        String identityId,
        String identityType,
        String subject,
        String userId,
        Instant boundAt,
        Map<String, Object> extensions) {

    public UserIdentity {
        Assert.hasText(identityType, "identityType must not be empty");
        // Defensive copy; tolerate null Object values inside the map because
        // some identity backends may want to surface explicit nulls.
        if (extensions == null || extensions.isEmpty()) {
            extensions = Collections.emptyMap();
        } else {
            extensions = Collections.unmodifiableMap(new LinkedHashMap<>(extensions));
        }
    }
}
