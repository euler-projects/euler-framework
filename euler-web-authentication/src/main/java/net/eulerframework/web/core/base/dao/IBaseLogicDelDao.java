package net.eulerframework.web.core.base.dao;

import java.io.Serializable;
import java.util.Collection;

import net.eulerframework.web.core.base.entity.BaseLogicDelEntity;

public interface IBaseLogicDelDao<T extends BaseLogicDelEntity<?>> extends IBaseModifyInfoDao<T>{
    
    void deletePhysical(T entity);
    
    void deleteByIdPhysical(Serializable id);

    void deleteAllPhysical(Collection<T> entities);

    void deleteByIdsPhysical(Collection<Serializable> ids);

    void deleteByIdsPhysical(Serializable[] idArray);
}
