package org.eulerframework.security.core.userdetails;

import org.eulerframework.security.authentication.wechat.WechatUser;

public interface EulerWechatUserDetailsService {
    EulerUserDetails loadUserByWechatUser(WechatUser wechatUser) throws UserDetailsNotFountException;

    EulerUserDetails createUser(WechatUser wechatUser);
}
