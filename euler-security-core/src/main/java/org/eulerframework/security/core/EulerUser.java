/*
 * Copyright 2013-2024 the original author or authors.
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

package org.eulerframework.security.core;

import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.springframework.security.core.CredentialsContainer;

import java.util.Collection;

public interface EulerUser extends CredentialsContainer {

    /**
     * Returns the user id used to identify the user.
     * @return the user id, <code>null</code> if the user instance is not persisted.
     */
    String getUserId();

    /**
     * Returns the username used to authenticate the user. Cannot return
     * <code>null</code>.
     * @return the username (never <code>null</code>)
     */
    String getUsername();

    /**
     * Returns the authorities granted to the user. Cannot return <code>null</code>.
     * @return the authorities, sorted by natural key (never <code>null</code>)
     */
    Collection<? extends EulerAuthority> getAuthorities();

    /**
     * Returns the password used to authenticate the user.
     * @return the password
     */
    String getPassword();

    /**
     * Indicates whether the user's account has expired. An expired account cannot be
     * authenticated.
     * @return <code>true</code> if the user's account is valid (ie non-expired),
     * <code>false</code> if no longer valid (ie expired)
     */
    default boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is locked or unlocked. A locked user cannot be
     * authenticated.
     * @return <code>true</code> if the user is not locked, <code>false</code> otherwise
     */
    default boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indicates whether the user's credentials (password) has expired. Expired
     * credentials prevent authentication.
     * @return <code>true</code> if the user's credentials are valid (ie non-expired),
     * <code>false</code> if no longer valid (ie expired)
     */
    default boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is enabled or disabled. A disabled user cannot be
     * authenticated.
     * @return <code>true</code> if the user is enabled, <code>false</code> otherwise
     */
    default boolean isEnabled() {
        return true;
    }

    void reloadUserDetails(EulerUserDetails userDetails);
}
