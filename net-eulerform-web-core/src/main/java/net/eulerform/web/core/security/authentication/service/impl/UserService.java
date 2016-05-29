package net.eulerform.web.core.security.authentication.service.impl;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import net.eulerform.web.core.base.service.impl.BaseService;
import net.eulerform.web.core.security.authentication.dao.IUserDao;
import net.eulerform.web.core.security.authentication.entity.User;
import net.eulerform.web.core.security.authentication.service.IUserService;

public class UserService extends BaseService implements IUserService, UserDetailsService {

    private IUserDao userDao;

    private PasswordEncoder passwordEncoder;

    public void setUserDao(IUserDao userDao) {
        this.userDao = userDao;
    }
        
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void createUser(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(this.passwordEncoder.encode(password));
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        this.userDao.save(user);
        
    }

    @Override
    public List<User> findAllUsers() {
        List<User> result = this.userDao.findAll();
        if(result != null){
            for(User user : result){
                user.eraseCredentials();
            }
        }
        return result;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = this.userDao.findUserByName(username);
        if(user == null) {
            throw new UsernameNotFoundException("User \"" + username + "\" not found.");
        }
        
        user.getAuthorities().size();
        
        return user;
    }
}
