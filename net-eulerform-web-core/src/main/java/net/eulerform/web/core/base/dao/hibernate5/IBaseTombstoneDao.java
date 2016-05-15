package net.eulerform.web.core.base.dao.hibernate5;

import java.io.Serializable;

import net.eulerform.web.core.base.entity.BaseTombstoneEntity;

public interface IBaseTombstoneDao<T extends BaseTombstoneEntity<?>> extends IBaseDao<T>{
    
    void deletePhysical(T entity);
    
    void deletePhysical(Serializable id);
}
