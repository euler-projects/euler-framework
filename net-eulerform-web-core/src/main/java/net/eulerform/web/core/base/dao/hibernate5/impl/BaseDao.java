package net.eulerform.web.core.base.dao.hibernate5.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import net.eulerform.common.Generic;
import net.eulerform.web.core.base.dao.hibernate5.IBaseDao;
import net.eulerform.web.core.base.entity.BaseEntity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Example;

public abstract class BaseDao<T extends BaseEntity<?>> implements IBaseDao<T> {
    
    protected final Logger log = LogManager.getLogger();

    private SessionFactory sessionFactory;

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    protected final Class<T> entityClass;

    @SuppressWarnings("unchecked")
    public BaseDao(){
        this.entityClass = (Class<T>) Generic.findSuperClassGenricType(this.getClass(), 0);
    }

    @Override
    public T load(Serializable id) {
        return this.getSessionFactory().getCurrentSession().get(this.entityClass, id);
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
        this.getSessionFactory().getCurrentSession()
            .createQuery(hql)
            .executeUpdate();
    }
    
    protected void update(String hql, Object... params) {
        Query query = this.getSessionFactory().getCurrentSession().createQuery(hql);

        for(int i = 0; i < params.length; i++){
            query.setParameter(i, params[i]);
        }

        query.executeUpdate();
    }

    @Override
    public void saveOrUpdate(T entity) {
        this.getSessionFactory().getCurrentSession().saveOrUpdate(entity);
    }
    
    @Override
    public void saveOrUpdate(Collection<T> entities){
        for(T entity : entities){
            this.saveOrUpdate(entity);
        }
    }

    @Override
    public void delete(T entity) {
        this.getSessionFactory().getCurrentSession().delete(entity);
    }

    @Override
    public void delete(Serializable id) {
        this.getSessionFactory().getCurrentSession()
            .createQuery("delete "+ this.entityClass.getSimpleName() + " en where en.id = ?0")
            .setParameter(0, id)
            .executeUpdate();
    }

    @Override
    public List<T> findBy(T entity) {        
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(this.entityClass);
        detachedCriteria.add(Example.create(entity));
        return this.findBy(detachedCriteria);
    }

    @Override
    public List<T> findAll() {
        return this.findBy("select en from "+ this.entityClass.getSimpleName() + " en");
    }


    @SuppressWarnings("unchecked")
    protected List<T> findBy(String hql){
        return this.getSessionFactory().getCurrentSession().createQuery(hql).list();
    }


    @SuppressWarnings("unchecked")
    protected List<T> findBy(String hql, Object... params){
        Query query = this.getSessionFactory().getCurrentSession().createQuery(hql);

        for(int i = 0; i < params.length; i++){
            query.setParameter(i, params[i]);
        }

        return query.list();
    }


    @SuppressWarnings("unchecked")
    protected List<T> findBy(DetachedCriteria detachedCriteria) {
        return detachedCriteria.getExecutableCriteria(this.getSessionFactory().getCurrentSession()).list();
    }

    @SuppressWarnings("unchecked")
    protected List<T> findPageBy(DetachedCriteria detachedCriteria, int pageIndex, int pageSize) {
        Criteria criteria = detachedCriteria.getExecutableCriteria(this.getSessionFactory().getCurrentSession());
        criteria.setFirstResult(pageIndex);
        criteria.setMaxResults(pageSize);
        return criteria.list();
    }

    @Override
    public long findCount() {
        List<?> l = this.findBy("select count(*) from "+ this.entityClass.getSimpleName());

        if(l!=null && l.size() == 1)
            return (Long)l.get(0);
        return 0;
    }
}
