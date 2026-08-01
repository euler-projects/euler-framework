package org.eulerframework.security.core.userdetails;

import org.eulerframework.security.authentication.wechat.WechatUser;

import java.util.List;

public interface EulerWechatUserDetailsService {
    EulerUserDetails loadUserByWechatUser(WechatUser wechatUser) throws UserDetailsNotFoundException;

    /**
     * Create a local user bound to the given WeChat identity.
     *
     * @param wechatUser  the resolved WeChat identity
     * @param authorities authorities to grant, supplied by the caller's
     *                    just-in-time provisioning policy; never empty.
     *                    Implementations must persist these verbatim
     *                    rather than choosing authorities themselves.
     */
    EulerUserDetails createUser(WechatUser wechatUser, List<String> authorities);
}
