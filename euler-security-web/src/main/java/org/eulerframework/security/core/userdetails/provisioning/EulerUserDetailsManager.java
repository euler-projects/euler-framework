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
package org.eulerframework.security.core.userdetails.provisioning;

import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.util.CollectionUtils;

public interface EulerUserDetailsManager extends UserDetailsManager, UserDetailsPasswordService {
    /**
     * Like {@link UserDetailsService#loadUserByUsername(String)},
     * but will return <code>null</code> instead of throw {@link UsernameNotFoundException} if principal not found.
     * <p>
     * More attention, if the user exist but has no GrantedAuthority, this method will still return the user record,
     *
     * @param principal the principal identifying the user whose data is required, like username or email.
     * @return a fully populated user record event the user has no GrantedAuthority,
     * or <code>null</code> if the user could not be found
     */
    EulerUserDetails loadUserByPrincipal(String principal);

    @Override
    default EulerUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        EulerUserDetails userDetails = this.loadUserByPrincipal(username);

        if (userDetails == null || CollectionUtils.isEmpty(userDetails.getAuthorities())) {
            throw new UsernameNotFoundException("User '" + username + "' not found.");
        } else {
            return userDetails;
        }
    }

    @Override
    EulerUserDetails updatePassword(UserDetails user, String newPassword);

    void disableUser(String username);
}
