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
package org.eulerframework.security.web.endpoint.user.login;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * View-model rendered on the shared login page for a single available
 * login method (e.g. {@code oauth2:google}, {@code webauthn},
 * {@code otp:sms}). Produced by {@link LoginMethodContributor}s.
 *
 * <p>The template dispatches on {@link #type()}; unknown types are
 * silently ignored so that a project can ship an experimental method
 * without breaking the login page when the corresponding
 * template branch is missing.
 *
 * <p>Instances are immutable. Use {@link Builder} to construct.
 *
 * <h2>Field conventions</h2>
 * <ul>
 *   <li>{@link #type()} &mdash; a short stable identifier for the
 *       login-method family. Well-known values:
 *       {@code "oauth2"}, {@code "webauthn"}, {@code "otp"}.</li>
 *   <li>{@link #id()} &mdash; a stable identifier within the type
 *       (e.g. {@code "google"} for {@code oauth2:google}, or the OTP
 *       channel name for {@code otp:sms}). May be {@code null} for
 *       method families with a single entry (e.g. {@code webauthn}).</li>
 *   <li>{@link #href()} &mdash; absolute-path URL the anchor should
 *       point to. Nullable when the method is not link-based (e.g. a
 *       JS-driven Passkey button, in which case the template renders a
 *       {@code <button>} instead).</li>
 *   <li>{@link #displayName()} &mdash; short human-readable name of the
 *       provider (e.g. {@code "Google"}). Rendered as the {@code {0}}
 *       argument of the {@code _SIGN_IN_WITH} i18n message by
 *       convention.</li>
 *   <li>{@link #iconClass()} &mdash; optional CSS class that swaps the
 *       icon on the button (e.g. {@code "btn-oauth2-google"}).</li>
 *   <li>{@link #order()} &mdash; sort key across all contributions;
 *       lower values render first. Defaults to {@code 0}.</li>
 *   <li>{@link #attributes()} &mdash; free-form extension bag for
 *       template-specific data (e.g. {@code data-*} attributes a JS
 *       widget needs to bootstrap). Never {@code null}.</li>
 * </ul>
 */
public final class LoginMethodView {

    /** Well-known value for {@link #type()}: OAuth2 / OIDC redirect flow. */
    public static final String TYPE_OAUTH2 = "oauth2";

    /** Well-known value for {@link #type()}: WebAuthn / Passkey. */
    public static final String TYPE_WEBAUTHN = "webauthn";

    /** Well-known value for {@link #type()}: OTP (SMS, email, TOTP, ...). */
    public static final String TYPE_OTP = "otp";

    private final String type;
    private final String id;
    private final String href;
    private final String displayName;
    private final String iconClass;
    private final int order;
    private final Map<String, String> attributes;

    private LoginMethodView(Builder builder) {
        this.type = Objects.requireNonNull(builder.type, "type is required");
        this.id = builder.id;
        this.href = builder.href;
        this.displayName = builder.displayName;
        this.iconClass = builder.iconClass;
        this.order = builder.order;
        this.attributes = builder.attributes.isEmpty()
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(builder.attributes));
    }

    public String type() {
        return this.type;
    }

    public String id() {
        return this.id;
    }

    public String href() {
        return this.href;
    }

    public String displayName() {
        return this.displayName;
    }

    public String iconClass() {
        return this.iconClass;
    }

    public int order() {
        return this.order;
    }

    public Map<String, String> attributes() {
        return this.attributes;
    }

    /*
     * Thymeleaf's default OGNL / Spring EL access uses JavaBean
     * getters, not record-style accessors, so mirror both. The record
     * shape stays available for programmatic use.
     */

    public String getType() {
        return this.type;
    }

    public String getId() {
        return this.id;
    }

    public String getHref() {
        return this.href;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getIconClass() {
        return this.iconClass;
    }

    public int getOrder() {
        return this.order;
    }

    public Map<String, String> getAttributes() {
        return this.attributes;
    }

    public static Builder builder(String type) {
        return new Builder(type);
    }

    public static final class Builder {
        private final String type;
        private String id;
        private String href;
        private String displayName;
        private String iconClass;
        private int order;
        private final Map<String, String> attributes = new LinkedHashMap<>();

        private Builder(String type) {
            this.type = type;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder href(String href) {
            this.href = href;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder iconClass(String iconClass) {
            this.iconClass = iconClass;
            return this;
        }

        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public Builder attribute(String key, String value) {
            this.attributes.put(key, value);
            return this;
        }

        public LoginMethodView build() {
            return new LoginMethodView(this);
        }
    }
}
