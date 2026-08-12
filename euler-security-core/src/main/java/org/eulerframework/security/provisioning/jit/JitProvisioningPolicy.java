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
package org.eulerframework.security.provisioning.jit;

import org.springframework.util.Assert;

import java.util.List;

/**
 * Just-in-time provisioning policy: whether a local user is created on
 * first successful authentication when no local user matches the
 * upstream identity, and which authorities that user is granted.
 *
 * <p>Applies to every authentication flow that may encounter an unknown
 * subject &mdash; federated login, WeChat authorization code, device
 * attestation, the OTP grant. The policy is resolved by the configuring
 * layer and passed down to the flow, so that no authentication provider
 * or user-details service hard-codes the granted authorities.
 *
 * <p>Instances are immutable.
 */
public final class JitProvisioningPolicy {

    private static final JitProvisioningPolicy DISABLED = new JitProvisioningPolicy(false, List.of());

    private final boolean enabled;
    private final List<String> defaultAuthorities;

    private JitProvisioningPolicy(boolean enabled, List<String> defaultAuthorities) {
        this.enabled = enabled;
        this.defaultAuthorities = List.copyOf(defaultAuthorities);
    }

    /**
     * Returns an enabled policy granting the given authorities.
     *
     * @param defaultAuthorities authorities granted to a provisioned
     *                           user; must not be empty
     */
    public static JitProvisioningPolicy enabled(List<String> defaultAuthorities) {
        Assert.notEmpty(defaultAuthorities,
                "defaultAuthorities must not be empty when JIT provisioning is enabled");
        return new JitProvisioningPolicy(true, defaultAuthorities);
    }

    /**
     * Returns the disabled policy: unknown subjects are rejected.
     */
    public static JitProvisioningPolicy disabled() {
        return DISABLED;
    }

    /**
     * Whether an unknown subject is provisioned as a new local user.
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Authorities granted to a provisioned user. Never {@code null};
     * empty exactly when {@link #isEnabled() disabled}.
     */
    public List<String> getDefaultAuthorities() {
        return this.defaultAuthorities;
    }

    /**
     * Returns {@link #getDefaultAuthorities()} in the array form
     * accepted by
     * {@code EulerUserDetails.Builder#authorities(String...)}.
     */
    public String[] defaultAuthoritiesArray() {
        return this.defaultAuthorities.toArray(new String[0]);
    }
}
