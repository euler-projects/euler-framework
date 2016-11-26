package net.eulerframework.web.core.security.service;

import org.springframework.security.access.prepost.PreAuthorize;

import net.eulerframework.web.core.base.service.IBaseService;

/**
 * 带有权限控制的基础业务逻辑层接口，除Spring Security验证相关的业务逻辑层外，所有业务逻辑层接口均应继承此接口<br>
 * @author cFrost
 *
 */
@PreAuthorize("isFullyAuthenticated() and hasAnyAuthority('ADMIN','SYSTEM')")
public interface IBaseSecurityService extends IBaseService {
    
}
