package net.eulerframework.web.module.authentication.entity;

import net.eulerframework.web.core.base.entity.BaseEntity;

public interface IUserProfile extends BaseEntity<IUserProfile> {

    public void setUserId(String userId);    

    public String getUserId();
}
