package net.eulerframework.web.module.authentication.dao.impl;

import net.eulerframework.web.core.base.dao.impl.hibernate5.BaseDao;
import net.eulerframework.web.module.authentication.dao.IUserProfileDao;
import net.eulerframework.web.module.authentication.entity.AbstractUserProfile;

public class AbstractUserProfileDao<T extends AbstractUserProfile> extends BaseDao<T> implements IUserProfileDao<T> {

    @Override
    public boolean isMyEntity(Class<? extends AbstractUserProfile> clazz) {
        return clazz.equals(this.entityClass);
    }

}
