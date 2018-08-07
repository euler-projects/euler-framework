package net.eulerframework.web.module.authentication.service;

import java.util.List;
import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import net.eulerframework.web.module.authentication.entity.EulerUserProfileEntity;
import net.eulerframework.web.module.authentication.entity.User;
import net.eulerframework.web.module.authentication.exception.UserInfoCheckWebException;

/**
 * @author cFrost
 *
 */
@Transactional
@Service("userRegistService")
public class UserRegistServiceImpl extends UserRegistService {
    @Resource 
    private EulerUserEntityService eulerUserEntityService;
    @Resource 
    private PasswordEncoder passwordEncoder;
    @Autowired(required = false) 
    private List<EulerUserProfileService<? extends EulerUserProfileEntity>> eulerUserProfileServices;
    @Autowired(required = false) 
    private List<EulerUserExtraDataProcessor> eulerUserExtraDataProcessors;

    @Override
    public EulerUserEntityService getEulerUserEntityService() {
        return this.eulerUserEntityService;
    }

    @Override
    public List<EulerUserProfileService<? extends EulerUserProfileEntity>> getEulerUserProfileServices() {
        return this.eulerUserProfileServices;
    }

    @Override
    public List<EulerUserExtraDataProcessor> getEulerUserExtraDataProcessors() {
        return this.eulerUserExtraDataProcessors;
    }

    @Override
    protected User doSignup(String username, String email, String mobile, String password)
            throws UserInfoCheckWebException {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setMobile(mobile);
        user.setPassword(this.passwordEncoder.encode(password.trim()));
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setEnabled(true);
        return (User) this.eulerUserEntityService.createUser(user);
    }

}
