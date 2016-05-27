package net.eulerform.web.core.base.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * 具有逻辑删除功能的<strong>自增主键</strong>实体<br>
 * 
 * @param <T> 实现类类名&nbsp;例如:<br><code>public class Example extends IDTombstoneEntity&lt;Example&gt;<code>
 * @author cFrost
 * @see net.eulerform.web.core.base.entity.NonIDTombstoneEntity
 * @see net.eulerform.web.core.base.entity.UUIDTombstoneEntity
 */
@SuppressWarnings("serial")
@MappedSuperclass
public abstract class IDTombstoneEntity<T extends IDTombstoneEntity<?>> extends IDEntity<T> implements BaseTombstoneEntity<T> {    

    @Column(name="CREATE_BY")
    private String createBy;
    @Column(name="MODIFY_BY")
    private String modifyBy;
    @Column(name="CREATE_DATE")
    private Date createDate;
    @Column(name="MODIFY_DATE")
    private Date modifyDate;
    @Column(name="IF_DEL")
    private Boolean ifDel;

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

    @Override
    public Boolean getIfDel() {
        return ifDel;
    }

    @Override
    public void setIfDel(Boolean ifDel) {
        this.ifDel = ifDel;
    }
}
