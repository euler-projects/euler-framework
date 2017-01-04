package net.eulerframework.web.module.authentication.service;

import javax.annotation.Resource;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.eulerframework.common.util.StringTool;
import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.base.service.impl.BaseService;
import net.eulerframework.web.core.exception.BadRequestException;
import net.eulerframework.web.module.authentication.dao.IUserDao;
import net.eulerframework.web.module.authentication.entity.IUserProfile;
import net.eulerframework.web.module.authentication.entity.User;

@Service
@Transactional
public class AuthenticationService extends BaseService implements IAuthenticationService {

    @Resource private IUserDao userDao;
    @Resource private PasswordEncoder passwordEncoder;
    
    @Override
    public String signUp(User user) {
        user.setId(null);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setEnabled(true);
        
        String password = StringTool.earseAllSpcases(user.getPassword());
        
        if(password == null || password.length() < WebConfig.getMinPasswordLength())
            throw new BadRequestException("密码格式或长度不符合要求");
        
        user.setPassword(this.passwordEncoder.encode(password));
        
        user.setResetToken(null);
        user.setResetTokenExpireTime(null);
        
        return (String) this.userDao.save(user);
    }

    @Override
    public void signUp(User user, IUserProfile userProfile) {
        // TODO Auto-generated method stub

    }

    @Override
    public void passwdResetEmailGen(String email) {
        // TODO Auto-generated method stub

    }

    @Override
    public void passwdResetSMSGen(String email) {
        // TODO Auto-generated method stub

    }

    @Override
    public User findUser(String passwordResetToken) {
        // TODO Auto-generated method stub
        return null;
    }

}
