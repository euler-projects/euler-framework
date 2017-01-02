package net.eulerframework.web.core.base.dao;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import net.eulerframework.web.core.base.entity.BaseEntity;
import net.eulerframework.web.core.base.request.PageQueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;

public interface IBaseDao<T extends BaseEntity<?>>{

    T load(Serializable id);

    List<T> load(Collection<Serializable> ids);

    List<T> load(Serializable[] idArray);
    
    Serializable save(T entity);
    
    void update(T entity);
    
    void saveOrUpdate(T entity);
    
    void saveOrUpdate(Collection<T> entities);
    
    void delete(T entity);
    
    void deleteById(Serializable id);

    void deleteAll(Collection<T> entities);

    void deleteByIds(Collection<Serializable> ids);

    void deleteByIds(Serializable[] idArray);
    
    List<T> findBy(T entity);
    
    List<T> findAll();
    
    long findCount();
    
    PageResponse<T> findEntityInPage(PageQueryRequest pageQueryRequest);

    void flushSession();
}
