package net.eulerframework.web.module.basic.dao;

import java.io.Serializable;

import net.eulerframework.web.core.base.dao.impl.hibernate5.BaseDao;
import net.eulerframework.web.module.basic.entity.Config;

public class ConfigDao extends BaseDao<Config> {

    @Override
    public Config load(Serializable id) {
        Config entity = super.load(id);
        if (entity == null || entity.getEnabled() == null || entity.getEnabled() == false)
            return null;
        return entity;
    }

}
