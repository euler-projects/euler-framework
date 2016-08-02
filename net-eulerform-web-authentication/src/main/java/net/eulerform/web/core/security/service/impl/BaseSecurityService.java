package net.eulerform.web.core.security.service.impl;

import net.eulerform.web.core.base.service.impl.BaseService;
import net.eulerform.web.core.security.service.IBaseSecurityService;
import net.eulerform.web.module.authentication.entity.User;
import net.eulerform.web.module.authentication.util.UserContext;

/**
 * 带有权限控制的基础业务逻辑层实现类，除Spring Security验证相关的业务逻辑层外，所有业务逻辑层实现均应继承类<br>
 * @author cFrost
 *
 */
public abstract class BaseSecurityService extends BaseService implements IBaseSecurityService {

    protected User getCurrentUser(){
        return UserContext.getCurrentUser();
    }
    
}
