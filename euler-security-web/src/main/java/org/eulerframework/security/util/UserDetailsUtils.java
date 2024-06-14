package org.eulerframework.security.util;

import org.eulerframework.security.core.EulerAuthority;
import org.eulerframework.security.core.EulerUser;
import org.eulerframework.security.core.userdetails.EulerGrantedAuthority;
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

        return EulerUserDetails.builder()
                .userId(eulerUser.getUserId())
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

    public static GrantedAuthority toGrantedAuthority(EulerAuthority eulerAuthority) {
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
