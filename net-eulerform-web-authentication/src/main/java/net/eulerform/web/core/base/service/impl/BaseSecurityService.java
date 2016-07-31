package net.eulerform.web.core.base.service.impl;

import net.eulerform.web.core.base.service.IBaseSecurityService;

/**
 * 带有权限控制的基础业务逻辑层实现类，除Spring Security验证相关的业务逻辑层外，所有业务逻辑层实现均应继承类<br>
 * 但是，在修改框架底层代码时，不建议在本类添加任何代码，而是添加在{@link BaseService}
 * @author cFrost
 *
 */
public abstract class BaseSecurityService extends BaseService implements IBaseSecurityService {

}
