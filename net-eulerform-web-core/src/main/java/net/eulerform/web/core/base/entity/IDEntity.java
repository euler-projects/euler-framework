package net.eulerform.web.core.base.entity;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import net.eulerform.web.core.base.exception.EntityCompareException;

/**
 * <strong>自增主键</strong>实体<br>
 * 适用与带有主键自增功能的数据库
 * 
 * @param <T> 实现类类名&nbsp;例如:<br><code>public class Example extends IDEntity&lt;Example&gt;<code>
 * @author cFrost
 * @see net.eulerform.web.core.base.entity.NonIDEntity
 * @see net.eulerform.web.core.base.entity.UUIDEntity
 */
@SuppressWarnings("serial")
@MappedSuperclass
public abstract class IDEntity<T extends IDEntity<?>> implements BaseEntity<T> {    

    @Id
    @Column(name="ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Override 
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override 
    public int compareTo(T obj) {
        if (this == obj)
            return 0;
        
        if(obj == null)
            return 1;
        
        if (getClass() != obj.getClass())
            throw new EntityCompareException("两比较对象类型不一致");
        
        if(this.getId() == null)
            return 1;
        
        if(obj.getId() == null)
            return -1;
        
        return this.getId().compareTo(obj.getId());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.getId() == null) ? 0 : this.getId().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        IDEntity<?> other = (IDEntity<?>) obj;
        if (this.getId() == null) {
            if (other.getId() != null)
                return false;
        } else if (!this.getId().equals(other.getId()))
            return false;
        return true;
    }
}
