package net.eulerframework.web.core.security.dao.impl.hibernate5;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import net.eulerframework.web.core.base.dao.impl.hibernate5.BaseDao;
import net.eulerframework.web.core.security.dao.IBaseModifyInfoDao;
import net.eulerframework.web.core.security.entity.BaseModifyInfoEntity;
import net.eulerframework.web.module.authentication.entity.User;
import net.eulerframework.web.module.authentication.util.UserContext;

public abstract class BaseModifyInfoDao<T extends BaseModifyInfoEntity<?>> extends BaseDao<T> implements IBaseModifyInfoDao<T> {

    protected User getCurrentUser(){
        return UserContext.getCurrentUser();
    }
    
    @Override
    public Serializable save(T entity) {
        this.setModifyInfo(entity);
        return super.save(entity);
    }

    @Override
    public void update(T entity) {
        this.setModifyInfo(entity);
        super.update(entity);

    }

    @Override
    public void saveOrUpdate(T entity) {
        this.setModifyInfo(entity);
        super.saveOrUpdate(entity);
    }
    
    @Override
    public void saveOrUpdate(Collection<T> entities){
        for(T entity : entities){
            this.setModifyInfo(entity);
        }
        super.saveOrUpdate(entities);
    }
    
    private void setModifyInfo(T entity){
        if(entity.getId() != null){
            T oldEntity = super.load(entity.getId());
            if(oldEntity != null) {
                entity.setCreateBy(oldEntity.getCreateBy());
                entity.setCreateDate(oldEntity.getCreateDate());
                //this.getSessionFactory().getCurrentSession().evict(oldEntity);//注释掉此处:load方法已经改为将查出实体置为游离态,此处不再需要
            }
        }
        
        Serializable currentUserId = this.getCurrentUser().getId();;
        Date date = new Date();
        if(entity.getCreateDate() == null)
            entity.setCreateDate(date);
        if(entity.getCreateBy() == null)
            entity.setCreateBy(String.valueOf(currentUserId));
        entity.setModifyDate(date);
        entity.setModifyBy(String.valueOf(currentUserId));
    }
}
