package net.eulerform.web.core.base.dao.hibernate5;

import java.io.Serializable;
import java.util.Collection;

import net.eulerform.web.core.base.entity.BaseTombstoneEntity;

public interface IBaseTombstoneDao<T extends BaseTombstoneEntity<?>> extends IBaseDao<T>{
    
    void deletePhysical(T entity);
    
    void deletePhysical(Serializable id);

    void deletePhysicalAll(Collection<T> entities);

    void deletePhysical(Collection<Serializable> ids);
}
