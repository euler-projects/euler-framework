package org.eulerframework.security.util;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

public abstract class UserDetailsUtils {
    public static <A extends GrantedAuthority> SortedSet<A> sortAuthorities(Collection<A> authorities) {
        Assert.notNull(authorities, "Cannot pass a null GrantedAuthority collection");
        // Ensure array iteration order is predictable (as per
        // UserDetails.getAuthorities() contract and SEC-717)
        SortedSet<A> sortedAuthorities = new TreeSet<>(UserDetailsUtils::compareGrantedAuthority);
        for (A grantedAuthority : authorities) {
            Assert.notNull(grantedAuthority, "GrantedAuthority list cannot contain any null elements");
            sortedAuthorities.add(grantedAuthority);
        }
        return sortedAuthorities;
    }

    public static int compareGrantedAuthority(GrantedAuthority g1, GrantedAuthority g2) {
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
