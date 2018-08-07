package net.eulerframework.web.module.authentication.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.StringUtils;

/**
 * @author cFrost
 *
 */
public interface EulerAuthorityEntity extends GrantedAuthority {

    default SimpleGrantedAuthority toSimpleGrantedAuthority() {
        return StringUtils.hasText(this.getAuthority()) ? new SimpleGrantedAuthority(this.getAuthority()) : null;
    }
}
