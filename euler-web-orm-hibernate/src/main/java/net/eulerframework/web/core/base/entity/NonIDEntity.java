package net.eulerframework.web.core.base.entity;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;

/**
 * <strong>无主键</strong>实体<br>
 * 需自行实现主键
 * 
 * @param <T> 实现类类名&nbsp;例如:<br><code>public class Example extends NonIDEntity&lt;Example&gt;<code>
 * @author cFrost
 * @see IDEntity
 * @see UUIDEntity
 */
@MappedSuperclass
public abstract class NonIDEntity<T extends NonIDEntity<?, ?>, ID_TYPE extends Serializable> implements BaseEntity<T, ID_TYPE> {

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
        @SuppressWarnings("unchecked")
        T other = (T) obj;
        if (this.getId() == null) {
            if (other.getId() != null)
                return false;
        } else if (!this.getId().equals(other.getId()))
            return false;
        return true;
    }
    
}
