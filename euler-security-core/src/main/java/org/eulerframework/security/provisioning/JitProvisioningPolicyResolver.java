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
package org.eulerframework.security.provisioning;

/**
 * Resolves the {@link JitProvisioningPolicy} applied to a given identity
 * type.
 *
 * <p>JIT provisioning is an identity-type concern: it answers "what
 * happens when an identity of this type is seen for the first time",
 * regardless of which entry point (web login, OAuth2 token grant,
 * device registration) encountered it. Keying the policy by identity
 * type guarantees that the same person receives the same provisioning
 * outcome on every path.
 *
 * <p>Flows that do not establish a regular identity resolve under a
 * pseudo identity type: {@link #IDENTITY_TYPE_DEVICE} for anonymous
 * device users, {@link #IDENTITY_TYPE_WECHAT} for WeChat open-ID
 * mappings.
 */
@FunctionalInterface
public interface JitProvisioningPolicyResolver {

    /**
     * Pseudo identity type under which anonymous device users
     * (App Attest) are provisioned.
     */
    String IDENTITY_TYPE_DEVICE = "device";

    /**
     * Pseudo identity type under which WeChat open-ID users are
     * provisioned.
     */
    String IDENTITY_TYPE_WECHAT = "wechat";

    /**
     * Returns the policy applied to the given identity type. Never
     * {@code null}.
     *
     * @param identityType the identity type (or pseudo identity type)
     *                     about to be provisioned
     * @return the policy; implementations decide the default for
     *         undeclared identity types
     */
    JitProvisioningPolicy resolve(String identityType);
}
