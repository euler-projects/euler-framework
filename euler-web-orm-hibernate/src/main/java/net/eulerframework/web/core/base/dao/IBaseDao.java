/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.eulerframework.web.core.base.dao;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hibernate.FetchMode;
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
    PageResponse<T> pageQuery(PageQueryRequest pageQueryRequest, List<Criterion> criterions, List<Order> orders, Projection projection);

    /**
     * @param pageQueryRequest
     * @param criterions
     * @param orders
     * @param projection
     * @param fetchMode
     * @return
     */
    PageResponse<T> pageQuery(PageQueryRequest pageQueryRequest, List<Criterion> criterions, List<Order> orders,
            Projection projection, Map<String, FetchMode> fetchMode);

    void flushSession();

    public boolean isMyEntity(Class<? extends T> clazz);
}
