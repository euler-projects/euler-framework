package net.eulerform.web.core.base.dao.hibernate5.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import net.eulerform.common.Generic;
import net.eulerform.web.core.base.dao.hibernate5.IBaseDao;
import net.eulerform.web.core.base.entity.BaseEntity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Example;

public abstract class BaseDao<T extends BaseEntity<?>> implements IBaseDao<T> {
    
    protected final Logger logger = LogManager.getLogger(this.getClass());

    protected final Logger log = LogManager.getLogger();

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
    public Serializable save(T entity) {
        return this.getSessionFactory().getCurrentSession().save(entity);
    }

    @Override
    public void update(T entity) {
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
        this.getSessionFactory().getCurrentSession().saveOrUpdate(entity);
    }

    @Override
    public void saveOrUpdate(Collection<T> entities) {
        if(entities == null || entities.isEmpty()) return;
        
        for (T entity : entities) {
            this.saveOrUpdate(entity);
        }
    }

    @Override
    public void delete(T entity) {
        this.getSessionFactory().getCurrentSession().delete(entity);
    }

    @Override
    public void delete(Serializable id) {
        StringBuffer hqlBuffer = new StringBuffer();
        hqlBuffer.append("delete ");
        hqlBuffer.append(this.entityClass.getSimpleName());
        hqlBuffer.append(" en where en.id = ?0");
        final String hql = hqlBuffer.toString();
        this.update(hql, id);
    }

    @Override
    public void deleteAll(Collection<T> entities) {
        if(entities == null || entities.isEmpty()) return;
        
        Collection<Serializable> idList = new HashSet<>();
        for(T entity : entities){
            idList.add(entity.getId());
        }
        
        this.delete(idList);
    }

    @Override
    public void delete(Collection<Serializable> ids) {
        StringBuffer hqlBuffer = new StringBuffer();
        hqlBuffer.append("delete ");
        hqlBuffer.append(this.entityClass.getSimpleName());
        hqlBuffer.append(" en where ");
        Serializable[] idArray = ids.toArray(new Serializable[0]);
        for(int i=0;i<idArray.length;i++) {
            if(i==0) {
                hqlBuffer.append("en.id= '");
            } else {
                hqlBuffer.append(" or en.id= '");
            }
            hqlBuffer.append(idArray[i]);
            hqlBuffer.append("'");
        }
        final String hql = hqlBuffer.toString();
        System.out.println(hql);
        this.update(hql);
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
    protected List<T> findPageBy(DetachedCriteria detachedCriteria, int pageIndex, int pageSize) {
        Criteria criteria = detachedCriteria.getExecutableCriteria(this.getSessionFactory().getCurrentSession());
        criteria.setFirstResult(pageIndex);
        criteria.setMaxResults(pageSize);
        List<T> result = criteria.list();
        evict(result);
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

    protected void evict(T entity) {
        if (entity == null)
            return;
        this.getCurrentSession().evict(entity);
    }

    protected void evict(Collection<T> entities) {
        if (entities == null)
            return;

        for (T entity : entities) {
            if (entity == null)
                continue;
            this.getCurrentSession().evict(entity);
        }
    }
}
