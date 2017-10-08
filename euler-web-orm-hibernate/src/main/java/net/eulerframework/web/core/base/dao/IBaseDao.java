package net.eulerframework.web.core.base.dao;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;

import net.eulerframework.web.core.base.entity.BaseEntity;
import net.eulerframework.web.core.base.request.PageQueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;

public interface IBaseDao<T extends BaseEntity<?, ?>>{

    T load(Serializable id);

    List<T> load(Collection<Serializable> ids);

    List<T> load(Serializable[] idArray);
    
    Serializable save(T entity);
    
    void update(T entity);
    
    void saveOrUpdate(T entity);
    
    void saveOrUpdateBatch(Collection<T> entities);

    void delete(T entity);
    
    void deleteById(Serializable id);

    void deleteBatch(Collection<T> entities);

    void deleteByIds(Collection<Serializable> ids);

    void deleteByIds(Serializable[] idArray);
    
    List<T> queryByEntity(T entity);
    
    List<T> queryAll();
    
    long countAll();
    
    PageResponse<T> pageQuery(PageQueryRequest pageQueryRequest);

    /**
     * @param pageQueryRequest
     * @param criterions
     * @return
     */
    PageResponse<T> pageQuery(PageQueryRequest pageQueryRequest, List<Criterion> criterions);

    /**
     * @param pageQueryRequest
     * @param criterions
     * @param orders
     * @return
     */
    PageResponse<T> pageQuery(PageQueryRequest pageQueryRequest, List<Criterion> criterions, List<Order> orders);

    /**
     * @param pageQueryRequest
     * @param criterions
     * @param orders
     * @param projection
     * @return
     */
    PageResponse<T> pageQuery(PageQueryRequest pageQueryRequest, List<Criterion> criterions, List<Order> orders,
            Projection projection);
    
    /**
     * 
     * @param pageQueryRequest
     * @param propertySetToSelectMode
     * @return
     * @deprecated
     */
    public PageResponse<T> pageQuery(PageQueryRequest pageQueryRequest, String... propertySetToSelectMode);

    void flushSession();

    public boolean isMyEntity(Class<? extends T> clazz);
}
