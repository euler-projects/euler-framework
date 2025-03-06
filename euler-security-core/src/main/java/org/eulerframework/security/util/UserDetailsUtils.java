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

package org.eulerframework.security.util;

import org.eulerframework.security.core.EulerAuthority;
import org.eulerframework.security.core.EulerUser;
import org.eulerframework.security.core.EulerGrantedAuthority;
import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

public abstract class UserDetailsUtils {
    public static EulerUserDetails toEulerUserDetails(EulerUser eulerUser) {
        if (eulerUser == null) {
            return null;
        }

        EulerUserDetails.UserBuilder builder = EulerUserDetails.builder();
        if (eulerUser.getUserId() != null) {
            builder.userId(eulerUser.getUserId());
        }

        return builder
                .password(eulerUser.getPassword())
                .username(eulerUser.getUsername())
                .accountExpired(!eulerUser.isAccountNonExpired())
                .accountLocked(!eulerUser.isAccountNonLocked())
                .credentialsExpired(!eulerUser.isCredentialsNonExpired())
                .disabled(!eulerUser.isEnabled())
                .authorities(Optional.ofNullable(eulerUser.getAuthorities())
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(UserDetailsUtils::toGrantedAuthority)
                        .collect(Collectors.toList()))
                .build();
    }

    public static EulerGrantedAuthority toGrantedAuthority(EulerAuthority eulerAuthority) {
        return eulerAuthority == null ? null : new EulerGrantedAuthority(
                eulerAuthority.getAuthority(),
                eulerAuthority.getName(),
                eulerAuthority.getDescription()
        );
    }

    public static <A extends GrantedAuthority> SortedSet<A> sortGrantedAuthorities(Collection<A> authorities) {
        Assert.notNull(authorities, "Cannot pass a null GrantedAuthority collection");
        // Ensure array iteration order is predictable (as per
        // UserDetails.getAuthorities() contract and SEC-717)
        SortedSet<A> sortedAuthorities = new TreeSet<>(UserDetailsUtils::compareGrantedAuthority);
        Set<String> authorityCodes = new HashSet<>();
        for (A grantedAuthority : authorities) {
            Assert.notNull(grantedAuthority, "GrantedAuthority list cannot contain any null elements");
            if (authorityCodes.contains(grantedAuthority.getAuthority())) {
                throw new IllegalArgumentException(
                        "Can not sort authorities, there are more than one authority which code is: "
                                + grantedAuthority.getAuthority());
            }
            authorityCodes.add(grantedAuthority.getAuthority());

            sortedAuthorities.add(grantedAuthority);
        }
        return sortedAuthorities;
    }

    private static int compareGrantedAuthority(GrantedAuthority g1, GrantedAuthority g2) {
        // Neither should ever be null as each entry is checked before adding it to
        // the set. If the authority is null, it is a custom authority and should
        // precede others.
        if (g2.getAuthority() == null) {
            return -1;
        }
        if (g1.getAuthority() == null) {
            return 1;
        }
        return g1.getAuthority().compareTo(g2.getAuthority());
    }
}
