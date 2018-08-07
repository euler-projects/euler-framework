package net.eulerframework.web.module.oauth2.vo;

import java.util.Collection;

/**
 * @author cFrost
 *
 */
public class UserInfo {
    private OAuth2User user;
    private Collection<String> authority;

    public OAuth2User getUser() {
        return user;
    }

    public void setUser(OAuth2User user) {
        this.user = user;
    }

    public Collection<String> getAuthority() {
        return authority;
    }

    public void setAuthority(Collection<String> authority) {
        this.authority = authority;
    }

}
