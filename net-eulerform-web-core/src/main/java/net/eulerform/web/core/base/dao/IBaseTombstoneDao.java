package net.eulerform.web.core.base.dao;

import java.io.Serializable;
import java.util.Collection;

import net.eulerform.web.core.base.entity.BaseTombstoneEntity;

public interface IBaseTombstoneDao<T extends BaseTombstoneEntity<?>> extends IBaseDao<T>{
    
    void deletePhysical(T entity);

    void deleteAllPhysical(Collection<T> entities);
    
    void deleteByIdPhysical(Serializable id);

    void deleteByIdsPhysical(Collection<Serializable> ids);

    void deleteByIdsPhysical(Serializable[] idArray);
}
