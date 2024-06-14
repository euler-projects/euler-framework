package org.eulerframework.security.core.userdetails;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.CollectionUtils;

public interface EulerUserDetailsService extends UserDetailsService {
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
}
