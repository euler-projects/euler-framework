package net.eulerform.web.core.security.authentication.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import net.eulerform.web.core.base.entity.CacheStore;
import net.eulerform.web.core.base.service.impl.BaseService;
import net.eulerform.web.core.security.authentication.dao.IUserDao;
import net.eulerform.web.core.security.authentication.entity.User;
import net.eulerform.web.core.security.authentication.service.IUserService;
import net.eulerform.web.core.security.authentication.util.UserContext;

public class UserService extends BaseService implements IUserService, UserDetailsService {

    private IUserDao userDao;
    
    private boolean cacheEnabled = false;
    private Map<String, CacheStore<User>> clientCache = new HashMap<>();
    private long cacheMilliseconds = 10000;

    private PasswordEncoder passwordEncoder;

    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = false;//不启用
    }

    public void setCacheSeconds(long cacheSecond) {
        if(cacheSecond < 10){
            this.cacheMilliseconds = cacheSecond * 1000;            
        }
    }

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

        if(cacheEnabled) {
            CacheStore<User> cacheStore = this.clientCache.get(username);
            if(cacheStore != null) {
                if((new Date().getTime() - cacheStore.getAddDate().getTime()) < this.cacheMilliseconds){
                    return cacheStore.getData();
                } else {
                    this.clientCache.remove(username);
                }
            }
        }
        User user = this.userDao.findUserByName(username);
        if(user == null) {
            throw new UsernameNotFoundException("User \"" + username + "\" not found.");
        }

        if(cacheEnabled) {
            this.clientCache.put(username, new CacheStore<User>(user));
        }
        
        user.getAuthorities().size();
        UserContext.addUserDetailsToCache(user);
        
        return user;
    }
    
    
}
