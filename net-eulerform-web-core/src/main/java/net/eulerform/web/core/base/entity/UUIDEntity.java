package net.eulerform.web.core.base.entity;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import net.eulerform.web.core.base.exception.EntityCompareException;

import org.hibernate.annotations.GenericGenerator;

/**
 * <strong>UUID主键</strong>实体<br>
 * 适用于任何数据库,如Oracle等
 * 
 * @param <T> 实现类类名&nbsp;例如:<br><code>public class Example extends UUIDEntity&lt;Example&gt;<code>
 * @author cFrost
 * @see net.eulerform.web.core.base.entity.NonIDEntity
 * @see net.eulerform.web.core.base.entity.IDEntity
 */
@SuppressWarnings("serial")
@MappedSuperclass
public abstract class UUIDEntity<T extends UUIDEntity<?>> implements BaseEntity<T> {    

    @Id
    @Column(name="ID")
    @GenericGenerator(name="systemUUID",strategy="uuid")
    @GeneratedValue(generator="systemUUID")
    private String id;
    
    @Override 
    public String getId() {
        return id;
    }

    public void setId(String id) {
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
        UUIDEntity<?> other = (UUIDEntity<?>) obj;
        if (this.getId() == null) {
            if (other.getId() != null)
                return false;
        } else if (!this.getId().equals(other.getId()))
            return false;
        return true;
    }
}
