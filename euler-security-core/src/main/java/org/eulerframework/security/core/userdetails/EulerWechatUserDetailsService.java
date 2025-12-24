package org.eulerframework.security.core.userdetails;

import org.eulerframework.security.authentication.WechatUser;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface EulerWechatUserDetailsService {
    EulerUserDetails loadUserByWechatUser(WechatUser wechatUser) throws UsernameNotFoundException;

    EulerUserDetails createUser(WechatUser wechatUser);
}
