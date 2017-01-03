package net.eulerframework.web.module.authentication.service;

import org.springframework.security.access.prepost.PreAuthorize;

import net.eulerframework.web.core.base.service.IBaseService;

@PreAuthorize("isFullyAuthenticated() and hasAuthority('ROOT')")
public interface IRootService extends IBaseService {

    /**
     * 重置root用户的密码,只用root用户在数据的密码字段被设置为NaN才能使用,重置的密码保存在WEB-INF下的.rootpassword文件中
     */
    public void resetRootPasswordRWT();
    
    /**
     * 重置admin用户的密码,只用admin用户在数据的密码字段被设置为NaN才能使用,重置的密码保存在WEB-INF下的.adminpassword文件中
     */
    public void resetAdminPasswordRWT();
}
