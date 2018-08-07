package net.eulerframework.web.module.oauth2.endpoint;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpoint;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.eulerframework.web.module.authentication.context.UserContext;
import net.eulerframework.web.module.authentication.principal.EulerUserDetails;
import net.eulerframework.web.module.oauth2.vo.OAuth2User;
import net.eulerframework.web.module.oauth2.vo.UserInfo;

/**
 * @author cFrost
 *
 */
@FrameworkEndpoint
public class UserInfoEndpoint {
    @RequestMapping(value = "oauth/user_info")
    @ResponseBody
    public UserInfo userInfo() {
        EulerUserDetails userDetails = UserContext.getCurrentUser();
        OAuth2User user = new OAuth2User();
        user.setUserId(userDetails.getUserId());
        user.setUsername(userDetails.getUsername());
        user.setAccountNonExpired(userDetails.isAccountNonExpired());
        user.setAccountNonLocked(userDetails.isAccountNonLocked());
        user.setCredentialsNonExpired(userDetails.isCredentialsNonExpired());
        user.setEnabled(userDetails.isEnabled());

        Set<String> authority = Optional.ofNullable(userDetails.getAuthorities()).orElse(new HashSet<>()).stream()
                .map(each -> each.getAuthority()).collect(Collectors.toSet());

        UserInfo userInfo = new UserInfo();
        userInfo.setUser(user);
        userInfo.setAuthority(authority);

        return userInfo;
    }
}
