package net.eulerframework.web.module.authentication.service.admin;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import net.eulerframework.web.core.base.request.PageQueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.core.base.service.impl.BaseService;
import net.eulerframework.web.module.authentication.entity.User;
import net.eulerframework.web.module.authentication.exception.UserInfoCheckWebException;
import net.eulerframework.web.module.authentication.exception.UserNotFoundException;
import net.eulerframework.web.module.authentication.repository.UserRepository;
import net.eulerframework.web.module.authentication.util.UserDataValidator;

/**
 * @author cFrost
 *
 */
@Service
public class UserManageServiceImpl extends BaseService implements UserManageService {

    @Resource
    private UserRepository userRepository;
    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public PageResponse<User> findUserByPage(PageQueryRequest pageQueryRequest) {
        List<User> all = this.userRepository.findAll();
        PageResponse<User> ret = new PageResponse<>(all, all.size(), pageQueryRequest.getPageIndex(), pageQueryRequest.getPageSize());//this.userDao.pageQuery(pageQueryRequest);
        //TODO:实现分页
        if (!ret.getRows().isEmpty()) {
            ret.getRows().forEach(user -> user.eraseCredentials());
        }

        return ret;
    }

    @Override
    public void addUser(String username, String email, String mobile, String password, boolean enabled,
            boolean accountNonExpired, boolean accountNonLocked, boolean credentialsNonExpired)
            throws UserInfoCheckWebException {
        Assert.hasText(username, "Param 'username' can not be empty");
        Assert.hasText(password, "Param 'password' can not be empty");
        
        User user = new User();
        
        UserDataValidator.validUsername(username);
        user.setUsername(username);
        
        if(StringUtils.hasText(email)) {
            UserDataValidator.validEmail(email);
            user.setEmail(email);
        }
        
        if(StringUtils.hasText(mobile)) {
            UserDataValidator.validMobile(mobile);
            user.setMobile(mobile);
        }
        
        UserDataValidator.validPassword(password);
        user.setPassword(this.passwordEncoder.encode(password));
        user.setEnabled(enabled);
        user.setAccountNonExpired(accountNonExpired);
        user.setAccountNonLocked(accountNonLocked);
        user.setCredentialsNonExpired(credentialsNonExpired);
        user.setRegistTime(new Date());
        
        this.userRepository.save(user);
    }

    @Override
    public void updateUser(String userId, String username, String email, String mobile, boolean enabled,
            boolean accountNonExpired, boolean accountNonLocked, boolean credentialsNonExpired)
            throws UserNotFoundException, UserInfoCheckWebException {
        Assert.hasText(userId, "Param 'userId' can not be empty");
        Assert.hasText(username, "Param 'username' can not be empty");
        
        User user = this.userRepository.findUserById(userId);
        
        if(user == null) {
            throw new UserNotFoundException();
        }
        
        if(!user.getUsername().equalsIgnoreCase(username)) {
            UserDataValidator.validUsername(username);            
        }
        user.setUsername(username);

        if(StringUtils.hasText(email) && !email.equalsIgnoreCase(user.getEmail())) {
            UserDataValidator.validEmail(email);       
        }
        user.setEmail(email);
        
        if(StringUtils.hasText(mobile) && !mobile.equalsIgnoreCase(user.getMobile())) {
            UserDataValidator.validMobile(mobile);    
        }
        user.setMobile(mobile);
        
        user.setEnabled(enabled);
        user.setAccountNonExpired(accountNonExpired);
        user.setAccountNonLocked(accountNonLocked);
        user.setCredentialsNonExpired(credentialsNonExpired);
        
        this.userRepository.save(user);
    }

    @Override
    public void updatePassword(String userId, String password) throws UserNotFoundException, UserInfoCheckWebException {
        Assert.hasText(userId, "Param 'userId' can not be empty");
        Assert.hasText(password, "Param 'password' can not be empty");
        
        User user = this.userRepository.findUserById(userId);
        
        if(user == null) {
            throw new UserNotFoundException();
        }
        
        UserDataValidator.validPassword(password);
        user.setPassword(this.passwordEncoder.encode(password));

        this.userRepository.save(user);
    }

    @Override
    @Transactional
    public void activeUser(String userId) throws UserNotFoundException {
        Assert.hasText(userId, "Param 'userId' can not be empty");
        
        User user = this.userRepository.findUserById(userId);
        
        if(user == null) {
            throw new UserNotFoundException();
        }

        user.setEnabled(true);
        
        this.userRepository.save(user);
    }

    @Override
    @Transactional
    public void blockUser(String userId) throws UserNotFoundException {
        Assert.hasText(userId, "Param 'userId' can not be empty");
        
        User user = this.userRepository.findUserById(userId);
        
        if(user == null) {
            throw new UserNotFoundException();
        }

        user.setEnabled(false);
        
        this.userRepository.save(user);
    }

}
