package net.eulerframework.web.module.authentication.service.impl;

import net.eulerframework.cache.DefaultObjectCache;
import net.eulerframework.cache.ObjectCachePool;
import net.eulerframework.common.util.BeanTool;
import net.eulerframework.web.core.base.request.QueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.core.base.service.impl.BaseService;
import net.eulerframework.web.module.authentication.dao.IGroupDao;
import net.eulerframework.web.module.authentication.dao.IUserDao;
import net.eulerframework.web.module.authentication.entity.Group;
import net.eulerframework.web.module.authentication.entity.User;
import net.eulerframework.web.module.authentication.service.IUserService2;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.*;

public class UserService2 extends BaseService implements IUserService2, UserDetailsService {

    @Resource private IUserDao userDao;
    @Resource private IGroupDao groupDao;

    private PasswordEncoder passwordEncoder;
        
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
    
    private boolean enableEmailLogin = false;
    private boolean enableMobileLogin = false;
    private boolean enableCache = false;
    
    private final static DefaultObjectCache<String, User> USER_CAHCE = ObjectCachePool.generateDefaultObjectCache(10_000L);
    
    public void setEnableEmailLogin(boolean enableEmailLogin) {
        this.enableEmailLogin = enableEmailLogin;
    }

    public void setEnableMobileLogin(boolean enableMobileLogin) {
        this.enableMobileLogin = enableMobileLogin;
    }

    public void setEnableCache(boolean enableCache) {
        this.enableCache = enableCache;
    }

    public void setUserCacheSeconds(int cacheSecond) {
        Assert.state(cacheSecond <= 10, "User cache second must less than 10 seconds");
        UserService2.USER_CAHCE.setDataLife(cacheSecond * 1000);
        if(!enableCache) {
            this.logger.warn("User cache was disabled, cacheSecond will not take any effect");
        } else {
            this.logger.info("User cache secode has been set to " + cacheSecond + " secodes");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = null;
        if(enableCache) {
            user = loadUserFromCache(username);
        }
        if(user == null) {
            user = this.userDao.findUserByName(username);
            if(user == null && enableEmailLogin) {
                user = this.userDao.findUserByEmail(username);
            }
            if(user == null && enableMobileLogin) {
                user = this.userDao.findUserByMobile(username);
            }
            if(user == null) {
                throw new UsernameNotFoundException("User \"" + username + "\" not found.");
            }
            if(enableCache) {
                this.putUserToCache(username, user);
            }
        }
        return user;
    }
    
    private User loadUserFromCache(String username) {
        return USER_CAHCE.get(username);
    }
    
    private void putUserToCache(String username, User user) {
        USER_CAHCE.put(username, user);
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
            user.setPassword(this.passwordEncoder.encode("123456"));
        }

        user.setUsername(user.getUsername().toLowerCase());
        user.setEmail(user.getEmail() == null ? null : user.getEmail().toLowerCase());
        user.setMobile(user.getMobile() == null ? null : user.getMobile().toLowerCase());
        
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
//    @Override
//    public User checkResetTokenRT(String userId, String resetToken) {
//        if(!enableUserResetPassword)
//            throw new UsernameNotFoundException("User not found.");
//        User user = this.userDao.findUserByResetToken(resetToken);
//        if(user == null || !user.getId().equalsIgnoreCase(userId))
//            throw new UsernameNotFoundException("User not found.");
//        return user;
//    }
//
//    @Override
//    public void resetUserPasswordWithResetTokenRWT(String userId, String newPassword, String resetToken) {
//        if(!enableUserResetPassword)
//            throw new UsernameNotFoundException("User not found.");
//        User user = this.userDao.findUserByResetToken(resetToken);
//        if(user == null || !user.getId().equalsIgnoreCase(userId))
//            throw new UsernameNotFoundException("User not found.");
//        if(newPassword.length() < this.miniPasswordLength) {
//            throw new IllegalParamException(Tag.i18n("global.minPasswdLength"));
//        }
////        user.setResetToken(null);
////        user.setResetTokenExpireTime(null);
//        user.setPassword(this.passwordEncoder.encode(newPassword));
//        this.userDao.update(user);        
//    }
//
//    @Override
//    public void forgotPasswordRWT(String email) {
//        User user = this.userDao.findUserByEmail(email);
//        if(user == null)
//            return;
//        String userId = user.getId();
//        String resetToken = UUID.randomUUID().toString();
//        Date resetTokenExpireTime = new Date(new Date().getTime() + 600000L);
////        user.setResetToken(resetToken);
////        user.setResetTokenExpireTime(resetTokenExpireTime);
//        this.userDao.update(user);
//        String resetURL = resetTokenURL.replace("{userId}", userId).replace("{resetToken}", resetToken);
//        System.out.println(resetURL);
//        ThreadSimpleMailSender threadSimpleSystemMailSender;
//            String modelZhCn = "<p>请点击下面的链接重置您的密码，10分钟内有效</p>"
//                    + "<p><a href=\"%1$s\">%2$s</a></p>"
//                    + "<p>发送时间: %3$tY-%<tm-%<td %<tH:%<tM:%<tS %<tZ</p>"
//                    + "<p>此邮件为系统自动发出，请勿回复。</p>";
//            String modelEnUs = "<p>You can use the following link within the next 10 minutes to reset your password:</p>"
//                    + "<p><a href=\"%1$s\">%2$s</a></p>"
//                    + "<p>Send time: %3$tY/%<tm/%<td %<tH:%<tM:%<tS %<tZ</p>"
//                    + "<p>This mail was sent from an system address. Please do not respond to this mail.</p>";
//            String content = String.format(modelZhCn+"<br><br><br>"+modelEnUs, resetURL, resetURL, Calendar.getInstance(TimeZone.getTimeZone("UTC")));
//            try {
//                threadSimpleSystemMailSender = MailSenderFactory.getThreadSimpleMailSender();
//            } catch (GlobalPropertyReadException e) {
//                throw new RuntimeException(e);
//            }
//            threadSimpleSystemMailSender.send("密码重置邮件 Please reset your password", content, email);
//    
//    }
}
