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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.annotation.Nonnull;
import org.eulerframework.properties.AbstractProperties;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Immutable model of a user identity.
 *
 * <p>A {@code UserIdentity} represents one external addressing of a user
 * &mdash; a phone number, an email address, an IdP-issued subject, and so
 * on. A user may own several identities of mixed types.
 *
 * <p>Beyond the five envelope fields ({@link #IDENTITY_ID},
 * {@link #IDENTITY_TYPE}, {@link #SUBJECT}, {@link #USER_ID},
 * {@link #BOUND_AT}) declared on this class, callers and backends may
 * attach additional per-type attributes through the inherited
 * {@link Builder#property(String, Object) Builder.property(name, value)};
 * the extension key set is owned by the backend that handles the
 * corresponding {@link #IDENTITY_TYPE}.
 *
 * <p>Instances appear in two distinct shapes; callers disambiguate by
 * context:
 * <dl>
 *   <dt><em>Persisted</em></dt>
 *   <dd>Returned by a {@link UserIdentityService} after a successful
 *       read or write. All five envelope fields are populated; any
 *       attached extension attributes carry per-type business values
 *       projected by the backend (e.g. a masked phone number, a WeChat
 *       nickname).</dd>
 *
 *   <dt><em>Prototype</em></dt>
 *   <dd>Supplied to a {@link UserIdentityService} by callers that have
 *       already verified ownership of the underlying value through
 *       another channel. Only {@link #IDENTITY_TYPE} is meaningful;
 *       the other envelope fields must be left unset. The caller
 *       attaches the raw business value through
 *       {@link Builder#property(String, Object)} under the key the
 *       backend uses for its persisted projection (e.g. {@code "phone"}
 *       for the phone backend). The backend mints {@link #IDENTITY_ID},
 *       derives {@link #SUBJECT}, stamps {@link #BOUND_AT} and pairs the
 *       supplied user id into the returned persisted instance.</dd>
 * </dl>
 *
 * <p>Field semantics:
 * <ul>
 *   <li>{@link #IDENTITY_ID} &mdash; opaque framework identifier (UUID) of
 *       the binding; unique across all identities; stable across rebinds.
 *       Unset in prototype shape.</li>
 *   <li>{@link #IDENTITY_TYPE} &mdash; logical identity type
 *       ({@code phone}, {@code email}, {@code wechat}, {@code apple},
 *       {@code google}, ...). Required in both shapes.</li>
 *   <li>{@link #SUBJECT} &mdash; deterministic per-type unique key derived
 *       by the backend from the raw value; indexed together with
 *       {@link #IDENTITY_TYPE} for cross-account uniqueness. The
 *       derivation function is implementation defined and opaque to this
 *       model. Unset in prototype shape.</li>
 *   <li>{@link #USER_ID} &mdash; id of the owning user; internal SPI
 *       field. Unset in prototype shape.</li>
 *   <li>{@link #BOUND_AT} &mdash; first-bind timestamp. Unset in prototype
 *       shape.</li>
 * </ul>
 *
 * <p>Each {@link Builder} setter enforces a per-field non-null /
 * non-empty contract; cross-field invariants (notably the presence of
 * {@link #IDENTITY_TYPE} on any built instance) are not enforced here
 * and are the caller's responsibility. {@link AbstractProperties} also
 * rejects construction from an empty property map.
 *
 * @see UserIdentityService
 */
@JsonIgnoreProperties({"properties"})
public class UserIdentity extends AbstractProperties {
    private static final String IDENTITY_ID = "identityId";
    private static final String IDENTITY_TYPE = "identityType";
    private static final String SUBJECT = "subject";
    private static final String USER_ID = "userId";
    private static final String BOUND_AT = "boundAt";

    private static final Set<String> ENVELOPE_KEYS = Set.of(
            IDENTITY_ID, IDENTITY_TYPE, SUBJECT, USER_ID, BOUND_AT);

    private UserIdentity(@Nonnull Map<String, Object> properties) {
        super(properties);
    }

    public String getIdentityId() {
        return getProperty(IDENTITY_ID);
    }

    public String getIdentityType() {
        return getProperty(IDENTITY_TYPE);
    }

    public String getSubject() {
        return getProperty(SUBJECT);
    }

    public String getUserId() {
        return getProperty(USER_ID);
    }

    public Instant getBoundAt() {
        return getProperty(BOUND_AT);
    }

    /**
     * Returns the per-type extension attributes carried alongside the
     * envelope fields, i.e. every property whose key is not one of
     * {@link #IDENTITY_ID}, {@link #IDENTITY_TYPE}, {@link #SUBJECT},
     * {@link #USER_ID} or {@link #BOUND_AT}.
     *
     * <p>Iteration order matches the insertion order of the underlying
     * property map. The returned map is unmodifiable.
     *
     * @return the extension attributes, never {@code null}, possibly
     * empty
     */
    public Map<String, Object> getExtensions() {
        Map<String, Object> properties = getProperties();
        Map<String, Object> extensions = new LinkedHashMap<>(properties.size());
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            if (!ENVELOPE_KEYS.contains(entry.getKey())) {
                extensions.put(entry.getKey(), entry.getValue());
            }
        }
        return Collections.unmodifiableMap(extensions);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns a builder pre-populated with the given extension
     * attributes. A {@code null} or empty map is treated as the
     * absence of extensions and yields a plain {@link #builder()}.
     *
     * @param extensions extension attributes keyed by the owning
     *                   backend's convention; may be {@code null} or
     *                   empty
     */
    public static Builder withExtensions(Map<String, Object> extensions) {
        Builder builder = new Builder();
        if (extensions == null || extensions.isEmpty()) {
            return builder;
        }
        return builder.properties(p -> p.putAll(extensions));
    }

    public static final class Builder extends AbstractBuilder<UserIdentity, UserIdentity.Builder> {
        private Builder() {
        }

        public Builder identityId(String identityId) {
            Assert.hasText(identityId, "identityId cannot be empty");
            return property(IDENTITY_ID, identityId);
        }

        public Builder identityType(String identityType) {
            Assert.hasText(identityType, "identityType cannot be empty");
            return property(IDENTITY_TYPE, identityType);
        }

        public Builder subject(String subject) {
            Assert.hasText(subject, "subject cannot be empty");
            return property(SUBJECT, subject);
        }

        public Builder userId(String userId) {
            Assert.hasText(userId, "userId cannot be empty");
            return property(USER_ID, userId);
        }

        public Builder boundAt(Instant boundAt) {
            Assert.notNull(boundAt, "boundAt cannot be null");
            return property(BOUND_AT, boundAt);
        }

        @Override
        public UserIdentity build() {
            Assert.hasText((String) getProperties().get(IDENTITY_TYPE),
                    "identityType is required");
            return new UserIdentity(this.getProperties());
        }
    }
}
