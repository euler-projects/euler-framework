package net.eulerform.web.module.authentication.service.impl;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import net.eulerform.common.BeanTool;
import net.eulerform.web.core.base.entity.PageResponse;
import net.eulerform.web.core.base.entity.QueryRequest;
import net.eulerform.web.core.base.service.impl.BaseService;
import net.eulerform.web.module.authentication.dao.IUserDao;
import net.eulerform.web.module.authentication.entity.Group;
import net.eulerform.web.module.authentication.entity.User;
import net.eulerform.web.module.authentication.service.IUserService;

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
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = this.userDao.findUserByName(username);
        if(user == null) {
            throw new UsernameNotFoundException("User \"" + username + "\" not found.");
        }
        
        return user;
    }

    @Override
    public PageResponse<User> findUserByPage(QueryRequest queryRequest, int pageIndex, int pageSize) {
        return this.userDao.findUserByPage(queryRequest, pageIndex, pageSize);
    }

    @Override
    public void saveUser(User user) {
        BeanTool.clearEmptyProperty(user);
        if(user.getId() != null) {
            User tmp = null;
            if(user.getPassword() == null) {
                if(tmp == null) {
                    tmp = this.userDao.load(user.getId());
                }
                user.setPassword(tmp.getPassword());
            }
            if(user.getGroups() == null || user.getGroups().isEmpty()) {
                if(tmp == null) {
                    tmp = this.userDao.load(user.getId());
                }
                user.setGroups(tmp.getGroups());
            }
            if(user.getAuthorities() == null || user.getAuthorities().isEmpty()) {
                if(tmp == null) {
                    tmp = this.userDao.load(user.getId());
                }
                user.setAuthorities(tmp.getAuthorities());
            }
        }
        if(user.getPassword() == null || user.getPassword().trim().equals("")) {
            user.setPassword(this.passwordEncoder.encode("sf123456"));
        }
        this.userDao.saveOrUpdate(user);
    }

    @Override
    public void saveUserGroups(String userId, List<Group> groups) {
        User user = this.userDao.load(userId);
        if(user == null)
            throw new RuntimeException("指定的User不存在");
        Set<Group> groupSet = new HashSet<>(groups);
        user.setGroups(groupSet);
        this.userDao.saveOrUpdate(user);;        
    }

    @Override
    public void deleteUsers(String[] idArray) {
        this.userDao.deleteByIds(idArray);
    }

    @Override
    public void enableUsersRWT(String[] idArray) {
        List<User> users = this.userDao.load(idArray);
        for(User user :users){
            user.setEnabled(true);
        }
        this.userDao.saveOrUpdate(users);
    }

    @Override
    public void disableUsersRWT(String[] idArray) {
        List<User> users = this.userDao.load(idArray);
        for(User user :users){
            user.setEnabled(false);
        }
        this.userDao.saveOrUpdate(users);
    }

    @Override
    public User findUserById(Serializable id) {
        User user = this.userDao.load(id);
        if(user == null)
            return null;
        user.eraseCredentials();
        return user;
    }

    @Override
    public List<User> findUserByNameOrCode(String nameOrCode) {
        return this.userDao.findUserByNameOrCode(nameOrCode);
    }
}
