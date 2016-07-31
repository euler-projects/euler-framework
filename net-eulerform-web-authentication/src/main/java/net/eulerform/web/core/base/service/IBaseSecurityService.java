package net.eulerform.web.core.base.service;

import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 带有权限控制的基础业务逻辑层接口，除Spring Security验证相关的业务逻辑层外，所有业务逻辑层接口均应继承此接口<br>
 * 但是，在修改框架底层代码时，不建议在本接口添加任何接口方法，所有接口方法应添加在{@link IBaseService}
 * @author cFrost
 *
 */
@PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','SYSTEM')")
public interface IBaseSecurityService extends IBaseService {
    
}
