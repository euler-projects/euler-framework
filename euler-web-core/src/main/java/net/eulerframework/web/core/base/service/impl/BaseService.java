package net.eulerframework.web.core.base.service.impl;

import java.util.Collection;

import net.eulerframework.common.base.log.LogSupport;
import net.eulerframework.web.core.base.dao.IBaseDao;
import net.eulerframework.web.core.base.entity.BaseEntity;
import net.eulerframework.web.core.base.service.IBaseService;

public abstract class BaseService extends LogSupport implements IBaseService {
    
    protected <D extends IBaseDao<ABSTRACT_E>, E extends ABSTRACT_E, ABSTRACT_E extends BaseEntity<ABSTRACT_E>> D getEntityDao(Collection<D> daoCollection, Class<E> entityClass) {
        
        for(D dao : daoCollection) {
            if(dao.isMyEntity(entityClass)) {
                return dao;
            }
        }

        throw new RuntimeException("Cannot find dao for " + entityClass.getName());   
        
    }

}
