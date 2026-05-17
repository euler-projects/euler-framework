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
package org.eulerframework.security.authentication.factor;

import org.springframework.util.Assert;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immutable view of one authentication factor bound to a user.
 * <p>
 * An authentication factor (Chinese: <em>用户认证因素</em>) is a credential or
 * possession the user can present to authenticate, e.g. a phone number, an
 * email address, a WeChat openid, a passkey or a password. The same user may
 * own multiple factors, and future MFA flows will require the user to satisfy
 * two or more of them in a single authentication ceremony.
 * <p>
 * The framework intentionally exposes <em>only</em> what the
 * {@code /user/identities} REST surface needs to render — concrete state, such
 * as the encrypted phone number or the public key of a passkey, lives in the
 * factor-specific persistence owned by each {@link UserAuthenticationFactorService}
 * implementation and is surfaced (if at all) through {@link #extensions()}.
 *
 * @param userId          id of the user this factor is bound to;
 *                        intentionally <em>not</em> serialised back to the
 *                        client by the {@code /user/identities} endpoint.
 *                        It is an internal SPI field consumed by
 *                        reverse-lookup callers (e.g. the OAuth2 OTP grant)
 *                        which need to map an original identifier back to
 *                        the owning user without first knowing the user's
 *                        principal name
 * @param factorId        opaque identifier of this factor (a UUID string);
 *                        unique across all factors of all users; surfaced on
 *                        the API as {@code factor_id}
 * @param factorType      logical factor type (e.g. {@code phone},
 *                        {@code email}, {@code wechat}); routes
 *                        {@link UserAuthenticationFactorService#bind} calls
 * @param identifier      stable, factor-scoped identifier of the bound
 *                        credential (e.g. hash of the phone number or
 *                        OpenID); used for uniqueness checks and surfaced
 *                        on the API as {@code identifier}
 * @param boundAt         when the factor was originally bound to the user
 * @param lastVerifiedAt  when the factor was last successfully verified;
 *                        equals {@code boundAt} immediately after binding
 * @param extensions      free-form, factor-specific attributes returned to
 *                        the API caller (e.g. {@code {"phone": "+8613*****00"}}
 *                        for the phone factor); never {@code null}, but may
 *                        be empty
 */
public record UserAuthenticationFactor(
        String userId,
        String factorId,
        String factorType,
        String identifier,
        Instant boundAt,
        Instant lastVerifiedAt,
        Map<String, Object> extensions) {

    public UserAuthenticationFactor {
        Assert.hasText(userId, "userId must not be empty");
        Assert.hasText(factorId, "factorId must not be empty");
        Assert.hasText(factorType, "factorType must not be empty");
        Assert.hasText(identifier, "identifier must not be empty");
        Assert.notNull(boundAt, "boundAt must not be null");
        Assert.notNull(lastVerifiedAt, "lastVerifiedAt must not be null");
        // Defensive copy. We accept null Object values inside the map (some
        // factor implementations may want to surface explicit nulls) so we
        // cannot use Map.copyOf which forbids null values.
        if (extensions == null || extensions.isEmpty()) {
            extensions = Collections.emptyMap();
        } else {
            extensions = Collections.unmodifiableMap(new LinkedHashMap<>(extensions));
        }
    }
}
