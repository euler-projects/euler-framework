package net.eulerframework.web.module.oldauthentication.dao;

import net.eulerframework.web.core.base.dao.IBaseDao;
import net.eulerframework.web.module.oldauthentication.entity.AbstractUserProfile;

public interface IUserProfileDao<T extends AbstractUserProfile> extends IBaseDao<T> {
}
