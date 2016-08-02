package net.eulerform.web.core.security.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import net.eulerform.web.core.base.entity.NonIDEntity;

/**
 * 具有修改人记录功能的<strong>无主键</strong>实体<br>
 * 
 * @param <T> 实现类类名&nbsp;例如:<br><code>public class Example extends NonIDModifyInfoEntity&lt;Example&gt;<code>
 * @author cFrost
 * @see net.eulerform.web.core.security.entity.IDModifyInfoEntity
 * @see net.eulerform.web.core.security.entity.UUIDModifyInfoEntity
 */
@SuppressWarnings("serial")
@MappedSuperclass
public abstract class NonIDModifyInfoEntity<T extends NonIDModifyInfoEntity<?>> extends NonIDEntity<T> implements BaseModifyInfoEntity<T> {    

    @Column(name="CREATE_BY", nullable=false)
    private String createBy;
    @Column(name="MODIFY_BY", nullable=false)
    private String modifyBy;
    @Column(name="CREATE_DATE", nullable=false)
    private Date createDate;
    @Column(name="MODIFY_DATE", nullable=false)
    private Date modifyDate;

    @Override
    public String getCreateBy() {
        return createBy;
    }

    @Override
    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    @Override
    public String getModifyBy() {
        return modifyBy;
    }

    @Override
    public void setModifyBy(String modifyBy) {
        this.modifyBy = modifyBy;
    }

    @Override
    public Date getCreateDate() {
        return createDate;
    }

    @Override
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Override
    public Date getModifyDate() {
        return modifyDate;
    }

    @Override
    public void setModifyDate(Date modifyDate) {
        this.modifyDate = modifyDate;
    }
    
    @Transient
    private String createByName;
    @Transient
    private String modifyByName;

    @Override
    public String getCreateByName() {
        return createByName;
    }

    @Override
    public void setCreateByName(String createByName) {
        this.createByName = createByName;
    }

    @Override
    public String getModifyByName() {
        return modifyByName;
    }

    @Override
    public void setModifyByName(String modifyByName) {
        this.modifyByName = modifyByName;
    }
}
