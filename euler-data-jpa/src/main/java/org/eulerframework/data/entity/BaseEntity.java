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
package org.eulerframework.data.entity;

import java.io.Serializable;

/**
 * 简单实体接口<br>
 * 实现类应覆盖<code>Serializable getId();<code>以实现主键<br>
 * 主键可采用任何实现{@link java.io.Serializable}接口的类,包括复合主键组件类
 * 
 * @param <T> 实现类类名&nbsp;例如:<br><code>public class Example implements BaseEntity&lt;Example&gt;<code>
 * @author cFrost
 * @see NonIDEntity
 * @see IDEntity
 * @see UUIDEntity
 */
public interface BaseEntity<T extends BaseEntity<?, ?>, ID_TYPE extends Serializable> extends Serializable, Comparable<T> {
    
    /**
     * 获取主键<br>
     * 一般采用自增ID或UUID，由实现类指定，亦可自定义主键
     * 
     * @return
     */
    ID_TYPE getId();
    
    void setId(ID_TYPE id);
    
    @SuppressWarnings("unchecked")
    default void setSerializable(Serializable id) {
        this.setId((ID_TYPE) id);
    }
}
