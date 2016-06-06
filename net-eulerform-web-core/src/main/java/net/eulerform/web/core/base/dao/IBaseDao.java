package net.eulerform.web.core.base.dao;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import net.eulerform.web.core.base.entity.BaseEntity;

public interface IBaseDao<T extends BaseEntity<?>>{

    T load(Serializable id);
    
    Serializable save(T entity);
    
    void update(T entity);
    
    void saveOrUpdate(T entity);
    
    void saveOrUpdate(Collection<T> entities);
    
    void delete(T entity);
    
    void delete(Serializable id);

    void deleteAll(Collection<T> entities);

    void delete(Collection<Serializable> ids);
    
    List<T> findBy(T entity);
    
    List<T> findAll();
    
    long findCount();
}
