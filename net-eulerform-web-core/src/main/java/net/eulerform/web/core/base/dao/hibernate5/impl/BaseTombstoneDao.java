package net.eulerform.web.core.base.dao.hibernate5.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import net.eulerform.web.core.base.dao.hibernate5.IBaseTombstoneDao;
import net.eulerform.web.core.base.entity.BaseTombstoneEntity;
import net.eulerform.web.core.util.UserContext;

public abstract class BaseTombstoneDao<T extends BaseTombstoneEntity<?>> extends BaseDao<T> implements IBaseTombstoneDao<T> {

    @Override
    public T load(Serializable id) {
        T entity = super.load(id);
        if(entity == null)
            return null;
        if(entity.getIfDel())
            return null;
        return entity;
    }

    @Override
    public Serializable save(T entity) {
        this.setBaseInfo(entity);
        return super.save(entity);
    }

    @Override
    public void update(T entity) {
        this.setBaseInfo(entity);
        super.update(entity);

    }

    @Override
    public void saveOrUpdate(T entity) {
        this.setBaseInfo(entity);
        super.saveOrUpdate(entity);
    }
    
    @Override
    public void saveOrUpdate(Collection<T> entities){
        for(T entity : entities){
            this.setBaseInfo(entity);
        }
        super.saveOrUpdate(entities);
    }

    @Override
    public void delete(T entity) {
        this.setBaseInfo(entity);
        entity.setIfDel(true);
        this.update(entity);
    }

    @Override
    public void delete(Serializable id) {
        T entity = this.load(id);
        this.delete(entity);
    }
    
    @Override
    public void deletePhysical(T entity) {
        super.delete(entity);
    }

    @Override
    public void deletePhysical(Serializable id) {
        super.delete(id);
    }

    @Override
    public List<T> findBy(T entity) {        
        entity.setIfDel(false);
        return super.findBy(entity);
    }

    @Override
    public List<T> findAll() {
        return super.findBy("select en from "+ this.entityClass.getSimpleName() + " en where en.ifDel = false");
    }

    protected List<T> findBy(DetachedCriteria detachedCriteria) {
        detachedCriteria.add(Restrictions.eq("ifDel", false));
        return super.findBy(detachedCriteria);
    }

    protected List<T> findPageBy(DetachedCriteria detachedCriteria, int pageIndex, int pageSize) {
        detachedCriteria.add(Restrictions.eq("ifDel", false));
        return super.findPageBy(detachedCriteria, pageIndex, pageSize);
    }

    @Override
    public long findCount() {
        List<?> l = super.findBy("select count(*) from "+ this.entityClass.getSimpleName() + "en where en.ifDel = 0");

        if(l!=null && l.size() == 1)
            return (Long)l.get(0);
        return 0;
    }
    
    private void setBaseInfo(T entity){
        if(entity.getId() != null){
            T oldEntity = super.load(entity.getId());
            if(oldEntity != null) {
                entity.setCreateBy(oldEntity.getCreateBy());
                entity.setCreateDate(oldEntity.getCreateDate());
                entity.setIfDel(oldEntity.getIfDel());
                this.getSessionFactory().getCurrentSession().evict(oldEntity);
            }
        }
        
        Serializable currentUserId = UserContext.getCurrentUser().getId();;
        Date date = new Date();
        if(entity.getCreateDate() == null)
            entity.setCreateDate(date);
        if(entity.getCreateBy() == null)
            entity.setCreateBy(String.valueOf(currentUserId));
        entity.setModifyDate(date);
        entity.setModifyBy(String.valueOf(currentUserId));
        if(entity.getIfDel() != true)
            entity.setIfDel(false);
    }
}
