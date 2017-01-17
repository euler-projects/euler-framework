package net.eulerframework.web.core.base.entity;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * 具有逻辑删除和修改人记录功能的<strong>UUID主键</strong>实体<br>
 * 
 * @param <T> 实现类类名&nbsp;例如:<br><code>public class Example extends IDLogicDelEntity&lt;Example&gt;<code>
 * @author cFrost
 * @see NonIDLogicDelEntity
 * @see IDLogicDelEntity
 */
@SuppressWarnings("serial")
@MappedSuperclass
public abstract class UUIDLogicEntity<T extends UUIDLogicEntity<?>> extends UUIDModifyInfoEntity<T> implements BaseLogicDelEntity<T> {    

    @Column(name="IF_DEL", nullable=false)
    private Boolean ifDel;

    @Override
    public Boolean getIfDel() {
        return ifDel;
    }

    @Override
    public void setIfDel(Boolean ifDel) {
        this.ifDel = ifDel;
    }
}
