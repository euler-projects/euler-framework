package net.eulerframework.web.module.authentication.service;

import javax.annotation.Resource;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import net.eulerframework.common.util.Assert;
import net.eulerframework.web.core.base.service.impl.BaseService;
import net.eulerframework.web.module.authentication.dao.UserDao;
import net.eulerframework.web.module.authentication.entity.EulerUserEntity;
import net.eulerframework.web.module.authentication.entity.User;

@Service
public class EulerUserEntityServiceImpl extends BaseService implements EulerUserEntityService {

    @Resource private UserDao userDao;

    @Override
    public EulerUserEntity loadUserByUsername(String username) throws UsernameNotFoundException {
        Assert.notNull(username, "username can not be null");
        
        return this.userDao.loadUserByUsername(username);
    }

    @Override
    public void updateUser(EulerUserEntity eulerUserEntity) {
        if(User.class.isAssignableFrom(eulerUserEntity.getClass())) {
            this.userDao.update((User)eulerUserEntity);
        }
    }
}
