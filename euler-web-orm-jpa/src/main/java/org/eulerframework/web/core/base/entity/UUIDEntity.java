/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eulerframework.web.core.base.entity;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.GenericGenerator;

/**
 * <strong>UUID主键</strong>实体<br>
 * 适用于任何数据库,如Oracle等
 * 
 * @param <T> 实现类类名&nbsp;例如:<br><code>public class Example extends UUIDEntity&lt;Example&gt;<code>
 * @author cFrost
 * @see NonIDEntity
 * @see IDEntity
 */
@MappedSuperclass
public abstract class UUIDEntity<T extends UUIDEntity<?>> implements BaseEntity<T, String> {    

    @Id
    @Column(name="id", length=36)
    @GenericGenerator(name="systemUUID",strategy="uuid2")
    @GeneratedValue(generator="systemUUID")
    private String id;
    
    @Override 
    public String getId() {
        return id;
    }

    @Override
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
            throw new RuntimeException("两比较对象类型不一致");
        
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
