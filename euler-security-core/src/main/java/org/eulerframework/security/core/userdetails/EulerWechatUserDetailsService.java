package org.eulerframework.security.core.userdetails;

import org.eulerframework.security.authentication.WechatUser;

public interface EulerWechatUserDetailsService {
    EulerUserDetails loadUserByWechatUser(WechatUser wechatUser);
}
