package net.eulerframework.web.core.base.dao.impl.hibernate5;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;

import net.eulerframework.common.util.BeanTool;
import net.eulerframework.common.util.CalendarTool;
import net.eulerframework.common.util.Generic;
import net.eulerframework.common.util.StringTool;
import net.eulerframework.common.util.log.LogSupport;
import net.eulerframework.web.core.base.dao.IBaseDao;
import net.eulerframework.web.core.base.entity.BaseEntity;
import net.eulerframework.web.core.base.request.PageQueryRequest;
import net.eulerframework.web.core.base.request.QueryRequest;
import net.eulerframework.web.core.base.request.QueryRequest.OrderMode;
import net.eulerframework.web.core.base.request.QueryRequest.QueryMode;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.core.extend.hibernate5.RestrictionsX;

public abstract class BaseDao<T extends BaseEntity<?>> extends LogSupport implements IBaseDao<T> {

    private SessionFactory sessionFactory;

    public Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    protected final Class<T> entityClass;

    @SuppressWarnings("unchecked")
    public BaseDao() {
        this.entityClass = (Class<T>) Generic.findSuperClassGenricType(this.getClass(), 0);
    }

    @Override
    public T load(Serializable id) {
        T entity = this.getCurrentSession().get(this.entityClass, id);
        evict(entity);
        return entity;
    }

    @Override
    public List<T> load(Collection<Serializable> ids) {
        Serializable[] idArray = ids.toArray(new Serializable[0]);
        return this.load(idArray);
    }

    @Override
    public List<T> load(Serializable[] idArray) {
        StringBuffer hqlBuffer = new StringBuffer();
        hqlBuffer.append("select en from ");
        hqlBuffer.append(this.entityClass.getSimpleName());
        hqlBuffer.append(" en where ");
        for (int i = 0; i < idArray.length; i++) {
            if (i == 0) {
                hqlBuffer.append("en.id= ?");
            } else {
                hqlBuffer.append(" or en.id= ?");
            }
        }
        final String hql = hqlBuffer.toString();
        return this.findBy(hql, (Object[])idArray);
    }

    @Override
    public Serializable save(T entity) {
        this.eraseEmptyProperty(entity);
        return this.getSessionFactory().getCurrentSession().save(entity);
    }

    @Override
    public void update(T entity) {
        this.eraseEmptyProperty(entity);
        this.getSessionFactory().getCurrentSession().update(entity);

    }

    protected void update(String hql) {
        this.getSessionFactory().getCurrentSession().createQuery(hql).executeUpdate();
    }

    protected void update(String hql, Object... params) {
        Query query = this.getSessionFactory().getCurrentSession().createQuery(hql);

        for (int i = 0; i < params.length; i++) {
            query.setParameter(i, params[i]);
        }

        query.executeUpdate();
    }

    @Override
    public void saveOrUpdate(T entity) {
        this.eraseEmptyProperty(entity);
        this.getSessionFactory().getCurrentSession().saveOrUpdate(entity);
    }

    @Override
    public void saveOrUpdate(Collection<T> entities) {
        if (entities == null || entities.isEmpty())
            return;
        this.eraseEmptyProperty(entities);

        for (T entity : entities) {
            this.saveOrUpdate(entity);
        }
    }

    @Override
    public void delete(T entity) {
        this.getSessionFactory().getCurrentSession().delete(entity);
    }

    @Override
    public void deleteById(Serializable id) {
        StringBuffer hqlBuffer = new StringBuffer();
        hqlBuffer.append("delete ");
        hqlBuffer.append(this.entityClass.getSimpleName());
        hqlBuffer.append(" en where en.id = ?");
        final String hql = hqlBuffer.toString();
        this.update(hql, id);
    }

    @Override
    public void deleteAll(Collection<T> entities) {
        if (entities == null || entities.isEmpty())
            return;

        Collection<Serializable> idList = new HashSet<>();
        for (T entity : entities) {
            idList.add(entity.getId());
        }

        this.deleteByIds(idList);
    }

    @Override
    public void deleteByIds(Collection<Serializable> ids) {
        Serializable[] idArray = ids.toArray(new Serializable[0]);
        this.deleteByIds(idArray);
    }

    @Override
    public void deleteByIds(Serializable[] idArray) {
        
        for(Serializable id : idArray) {
            this.deleteById(id);
        }
        
//        StringBuffer hqlBuffer = new StringBuffer();
//        hqlBuffer.append("delete ");
//        hqlBuffer.append(this.entityClass.getSimpleName());
//        hqlBuffer.append(" en where ");
//        for (int i = 0; i < idArray.length; i++) {
//            if (i == 0) {
//                hqlBuffer.append("en.id= ?");
//            } else {
//                hqlBuffer.append(" or en.id= ?");
//            }
//        }
//        final String hql = hqlBuffer.toString();
//        this.update(hql, (Object[])idArray);
    }

    @Override
    public List<T> findBy(T entity) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(this.entityClass);
        detachedCriteria.add(Example.create(entity));
        detachedCriteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        List<T> result = this.findBy(detachedCriteria);
        evict(result);
        return result;
    }

    @Override
    public List<T> findAll() {
        StringBuffer hqlBuffer = new StringBuffer();
        hqlBuffer.append("select en from ");
        hqlBuffer.append(this.entityClass.getSimpleName());
        hqlBuffer.append(" en");
        final String hql = hqlBuffer.toString();
        List<T> result = this.findBy(hql);
        return result;
    }

    @Override
    public long findCount() {
        StringBuffer hqlBuffer = new StringBuffer();
        hqlBuffer.append("select count(*) from ");
        hqlBuffer.append(this.entityClass.getSimpleName());
        final String hql = hqlBuffer.toString();
        List<?> l = this.findBy(hql);

        if (l != null && l.size() == 1)
            return (Long) l.get(0);
        return 0;
    }
    
    @Override
    public PageResponse<T> findEntityInPage(PageQueryRequest pageQueryRequest) {
        DetachedCriteria detachedCriteria = this.generateCriteria(pageQueryRequest);
        
        int pageIndex = pageQueryRequest.getPageIndex();
        int pageSize = pageQueryRequest.getPageSize();
        
        return this.findPageBy(detachedCriteria, pageIndex, pageSize);   
    }

    @Override
    public void flushSession() {
        this.getCurrentSession().flush();
    }
    
    @Override
    public boolean isMyEntity(Class<? extends T> clazz) {
        return clazz.equals(this.entityClass);
    }
    
    @SuppressWarnings("unchecked")
    protected List<T> findBy(String hql) {
        List<T> result = this.getSessionFactory().getCurrentSession().createQuery(hql).list();
        evict(result);
        return result;
    }

    @SuppressWarnings("unchecked")
    protected List<T> findBy(String hql, Object... params) {
        Query query = this.getSessionFactory().getCurrentSession().createQuery(hql);

        for (int i = 0; i < params.length; i++) {
            query.setParameter(i, params[i]);
        }

        List<T> result = query.list();
        evict(result);
        return result;
    }

    @SuppressWarnings("unchecked")
    protected List<T> findBy(DetachedCriteria detachedCriteria) {
        List<T> result = detachedCriteria.getExecutableCriteria(this.getSessionFactory().getCurrentSession()).list();
        evict(result);
        return result;
    }

    @SuppressWarnings("unchecked")
    protected List<T> findBy(String hql, int maxResults) {
        Query query = this.getSessionFactory().getCurrentSession().createQuery(hql);
        query.setMaxResults(maxResults);
        List<T> result = query.list();
        evict(result);
        return result;
    }
    
    @SuppressWarnings("unchecked")
    protected List<T> findByWithMaxResults(String hql, int maxResults, Object... params) {
        Query query = this.getSessionFactory().getCurrentSession().createQuery(hql);
        query.setMaxResults(maxResults);
        
        for (int i = 0; i < params.length; i++) {
            query.setParameter(i, params[i]);
        }

        List<T> result = query.list();
        evict(result);
        return result;
    }

    @SuppressWarnings("unchecked")
    protected List<T> findByWithMaxResults(DetachedCriteria detachedCriteria, int maxResults) {
        Criteria criteria = detachedCriteria.getExecutableCriteria(this.getSessionFactory().getCurrentSession());

        criteria.setMaxResults(maxResults);
        
        List<T> result = criteria.list();
        evict(result);
        return result;
    }

    protected PageResponse<T> findPageBy(DetachedCriteria detachedCriteria, int pageIndex, int pageSize) {
        return this.findPageBy(detachedCriteria, pageIndex, pageSize, null);
    }
    
    @SuppressWarnings("unchecked")
    protected PageResponse<T> findPageBy(DetachedCriteria detachedCriteria, int pageIndex, int pageSize, Projection projection) {
        
        detachedCriteria.setProjection(Projections.rowCount());
        long total = ((Long)detachedCriteria.getExecutableCriteria(this.getSessionFactory().getCurrentSession()).list().get(0)).longValue();

        detachedCriteria.setProjection(projection);
        
        if(projection != null)
            detachedCriteria.setResultTransformer(Transformers.aliasToBean(this.entityClass));
        
        Criteria criteria = detachedCriteria.getExecutableCriteria(this.getSessionFactory().getCurrentSession());
        criteria.setFirstResult((pageIndex - 1) * pageSize);
        criteria.setMaxResults(pageSize);
        List<T> result = criteria.list();
        evict(result);
        return new PageResponse<>(result, total, pageIndex, pageSize);
    }

    protected final void evict(Object entity) {
        if (entity == null || !BaseEntity.class.isAssignableFrom(entity.getClass()))
            return;
        this.getCurrentSession().evict(entity);
    }

    protected void evict(Collection<?> entities) {
        if (entities == null)
            return;

        for (Object entity : entities) {
            if (entity == null)
                continue;
            evict(entity);
        }
    }

    protected void eraseEmptyProperty(Collection<T> entities) {
        for (T entity : entities)
            this.eraseEmptyProperty(entity);
    }

    protected void eraseEmptyProperty(T entity) {
        BeanTool.clearEmptyProperty(entity);
    }   
    
    protected DetachedCriteria generateCriteria(QueryRequest queryRequest) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(this.entityClass);
        
        Map<String, String> queryMap = queryRequest.getQueryMap();
        
        for (Map.Entry<String, String> entry : queryMap.entrySet()) {
            String property = entry.getKey();
            String value = entry.getValue();
            
            if(StringTool.isNull(value))
                continue;
            
            QueryMode queryMode = queryRequest.getQueryMode(property);
            detachedCriteria.add(this.generateRestriction(property, value, queryMode));
        }
        
        LinkedHashMap<String, OrderMode> sortMap = queryRequest.getSortMap();
        
        for (Map.Entry<String, OrderMode> entry : sortMap.entrySet()) {
            String property = entry.getKey();
            OrderMode value = entry.getValue();
            
            if(value == null)
                continue;
            
            detachedCriteria.addOrder(this.generateOrder(property, value));
        }
        
        return detachedCriteria;
    }
    
    private Order generateOrder(String property, OrderMode orderMode) {
        try {
            this.entityClass.getDeclaredField(property);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("Property '" + property + "' not exist");
        }
        
        switch (orderMode) {
        case ASC:
            return Order.asc(property);
        case DESC:
            return Order.desc(property);
        default:
            throw new IllegalArgumentException("Unknown order mode: " + orderMode);        
        }
    }

    private Criterion generateRestriction(String property, String value, QueryMode queryMode) {
        Field field;
        try {
            field = this.entityClass.getDeclaredField(property);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("Property '" + property + "' not exist");
        }
        
        
        switch (queryMode) {
        case ANYWHERE:
            return RestrictionsX.like(property, value, MatchMode.ANYWHERE);
        case START:
            return RestrictionsX.like(property, value, MatchMode.START);
        case END:
            return RestrictionsX.like(property, value, MatchMode.END);
        case EXACT:
            return RestrictionsX.like(property, value, MatchMode.EXACT);
        case GE:
            return Restrictions.ge(property, this.changeValueType(value, field.getType()));
        case GT:
            return Restrictions.gt(property, this.changeValueType(value, field.getType()));
        case LE:
            return Restrictions.le(property, this.changeValueType(value, field.getType()));
        case LT:
            return Restrictions.lt(property, this.changeValueType(value, field.getType()));
        case IN:
            return RestrictionsX.in(property, this.generateValueArray(value, field.getType()));
        case NOTIN:
            return Restrictions.not(RestrictionsX.in(property, this.generateValueArray(value, field.getType())));
        case IS:
            return Restrictions.eq(property, this.changeValueType(value, field.getType()));
        case NOT:
            return Restrictions.ne(property, this.changeValueType(value, field.getType()));
        case BETWEEN:
            Object[] array1 = this.generateValueArray(value, field.getType());
            return Restrictions.between(property, array1[0], array1[1]);
        case OUTSIDE:
            Object[] array2 = this.generateValueArray(value, field.getType());
            return Restrictions.not(Restrictions.between(property, array2[0], array2[1]));
        default:
            throw new IllegalArgumentException("Unknown query mode: " + queryMode);
        }
    }
    
    private Object[] generateValueArray(String value, Class<?> clazz) {
        String[] valueArray = value.split(",");
        
        Object[] result = new Object[valueArray.length];
        
        for(int i = 0; i < valueArray.length; i++) {
            result[i] = this.changeValueType(valueArray[i], clazz);
        }
        return result;
    }

    private Object changeValueType(String value, Class<?> clazz) {
        if(String.class.equals(clazz)) {
            return value;
        } else if(Integer.class.equals(clazz) || "int".equals(clazz.toString())) {
            return Integer.parseInt(value);
        } else if(Long.class.equals(clazz) || "long".equals(clazz.toString())) {
            return Long.parseLong(value);
        } else if(Short.class.equals(clazz) || "short".equals(clazz.toString())) {
            return Short.parseShort(value);
        } else if(Float.class.equals(clazz) || "float".equals(clazz.toString())) {
            return Float.parseFloat(value);
        } else if(Double.class.equals(clazz) || "double".equals(clazz.toString())) {
            return Double.parseDouble(value);
        } else if(Boolean.class.equals(clazz) || "boolean".equals(clazz.toString())) {
            return Boolean.parseBoolean(value);
        } else if(Character.class.equals(clazz) || "char".equals(clazz.toString())) {
            this.logger.warn("Query property type is Character, only use the first char of value");
            return value.toCharArray()[0];
        } else if(Date.class.equals(clazz)) {
            Date ret = null;
            try {
                ret = new Date(Long.parseLong(value));
            } catch (NumberFormatException e) {
                try {
                    ret = CalendarTool.parseDate(value, "yyyy-MM-dd HH:mm:ss");
                } catch (ParseException e1) {
                    throw new IllegalArgumentException("Date property value '" + value + "' format doesn't match timesamp(3) or 'yyyy-MM-dd HH:mm:ss'");
                }
            }
            return ret;
        } else if(BigDecimal.class.equals(clazz)) {
            return new BigDecimal(value);
        } 
        
        throw new IllegalArgumentException("Unsupport query property type: " + clazz);
    }
}
