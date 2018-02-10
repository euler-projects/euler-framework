package net.eulerframework.web.module.oldauthentication.service;

import javax.annotation.Resource;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.eulerframework.common.util.StringUtils;
import net.eulerframework.common.util.io.file.SimpleFileIOUtils;
import net.eulerframework.web.core.base.service.impl.BaseService;
import net.eulerframework.web.module.oldauthentication.dao.UserDao;
import net.eulerframework.web.module.oldauthentication.entity.User;
import net.eulerframework.web.util.ServletUtils;

@Service
@Transactional(rollbackFor = Exception.class)
public class RootService extends BaseService {

    @Resource private UserDao userDao;
    @Resource private PasswordEncoder passwordEncoder;

    /**
     * 重置root用户的密码,只用root用户在数据的密码字段被设置为NaN才能使用,重置的密码保存在WEB-INF下的.rootpassword文件中
     * @throws Exception 无法重置密码
     */
    public void resetRootPassword() throws Exception {
        User root = this.userDao.findUserByName("root");
        if(!root.getPassword().equals("NaN"))
            throw new Exception();
        
        String newPassword = StringUtils.randomString(16);
        
        root.setPassword(passwordEncoder.encode(newPassword));
        
        String webInfPath = ServletUtils.getServletContext().getRealPath("/WEB-INF");
        String rootpasswordFilePath = webInfPath + "/.rootpassword";
        
        SimpleFileIOUtils.writeFile(rootpasswordFilePath, newPassword, false);
        
        this.userDao.update(root);
        
        this.userDao.flushSession();
        
        System.out.println("!!! root's password has been reset, new password was saved in " + rootpasswordFilePath);
    }

    /**
     * 重置admin用户的密码,只用admin用户在数据的密码字段被设置为NaN才能使用,重置的密码保存在WEB-INF下的.adminpassword文件中
     * @throws Exception 无法重置密码
     */
    public void resetAdminPassword() throws Exception {
        User admin = this.userDao.findUserByName("admin");
        if(!admin.getPassword().equals("NaN"))
            throw new Exception();

        String newPassword = StringUtils.randomString(16);
        
        admin.setPassword(passwordEncoder.encode(newPassword));
        
        String webInfPath = ServletUtils.getServletContext().getRealPath("/WEB-INF");
        String adminpasswordFilePath = webInfPath + "/.adminpassword";
        
        SimpleFileIOUtils.writeFile(adminpasswordFilePath, newPassword, false);
        
        this.userDao.update(admin);
        
        this.userDao.flushSession();
        
        System.out.println("!!! admin's password has been reset, new password was saved in " + adminpasswordFilePath);

    }

}
