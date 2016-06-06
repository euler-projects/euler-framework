package net.eulerform.web.core.base.dao.impl.hibernate5;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import net.eulerform.web.core.base.dao.IBaseTombstoneDao;
import net.eulerform.web.core.base.entity.BaseTombstoneEntity;
import net.eulerform.web.module.authentication.util.UserContext;

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
        hqlBuffer.append("update ");
        hqlBuffer.append(this.entityClass.getSimpleName());
        hqlBuffer.append(" en set en.ifDel = true where ");
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
    public void deletePhysical(T entity) {
        super.delete(entity);
    }

    @Override
    public void deletePhysical(Serializable id) {
        super.delete(id);
    }

    @Override
    public void deletePhysicalAll(Collection<T> entities){
        super.deleteAll(entities);
    }

    @Override
    public void deletePhysical(Collection<Serializable> ids){
        super.delete(ids);
    }

    @Override
    public List<T> findBy(T entity) {        
        entity.setIfDel(false);
        return super.findBy(entity);
    }

    @Override
    public List<T> findAll() {
        StringBuffer hqlBuffer = new StringBuffer();
        hqlBuffer.append("select en from ");
        hqlBuffer.append(this.entityClass.getSimpleName());
        hqlBuffer.append(" en where en.ifDel = false");
        final String hql = hqlBuffer.toString();
        return super.findBy(hql);
    }
    
//    @Override
//    protected List<T> findBy(DetachedCriteria detachedCriteria) {
//        detachedCriteria.add(Restrictions.eq("ifDel", false));
//        return super.findBy(detachedCriteria);
//    }
//    
//    @Override
//    protected List<T> findPageBy(DetachedCriteria detachedCriteria, int pageIndex, int pageSize) {
//        detachedCriteria.add(Restrictions.eq("ifDel", false));
//        return super.findPageBy(detachedCriteria, pageIndex, pageSize);
//    }

    @Override
    public long findCount() {
        StringBuffer hqlBuffer = new StringBuffer();
        hqlBuffer.append("select count(*) from ");
        hqlBuffer.append(this.entityClass.getSimpleName());
        hqlBuffer.append(" en where en.ifDel = false");
        final String hql = hqlBuffer.toString();
        List<?> l = super.findBy(hql);

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
                //entity.setIfDel(oldEntity.getIfDel());注释掉此处:再次保存已逻辑删除的对象时,删除状态以新对象为准
                //this.getSessionFactory().getCurrentSession().evict(oldEntity);//注释掉此处:load方法已经改为将查出实体置为游离态,此处不再需要
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
        if(entity.getIfDel() == null)
            entity.setIfDel(false);
    }
}
