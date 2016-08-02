package net.eulerform.web.core.security.entity;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * 具有逻辑删除和修改人记录功能的<strong>自增主键</strong>实体<br>
 * 
 * @param <T> 实现类类名&nbsp;例如:<br><code>public class Example extends IDLogicDelEntity&lt;Example&gt;<code>
 * @author cFrost
 * @see net.eulerform.web.core.security.entity.NonIDLogicDelEntity
 * @see net.eulerform.web.core.security.entity.UUIDLogicEntity
 */
@SuppressWarnings("serial")
@MappedSuperclass
public abstract class IDLogicDelEntity<T extends IDLogicDelEntity<?>> extends IDModifyInfoEntity<T> implements BaseLogicDelEntity<T> {    

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
