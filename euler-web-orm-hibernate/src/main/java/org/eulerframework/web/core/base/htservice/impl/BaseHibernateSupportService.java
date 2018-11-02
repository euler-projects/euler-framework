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
package org.eulerframework.web.core.base.htservice.impl;

import java.util.Collection;

import org.eulerframework.web.core.base.dao.IBaseDao;
import org.eulerframework.web.core.base.entity.BaseEntity;
import org.eulerframework.web.core.base.service.impl.BaseService;

public class BaseHibernateSupportService extends BaseService {
    
    protected <D extends IBaseDao<ABSTRACT_E>, E extends ABSTRACT_E, ABSTRACT_E extends BaseEntity<ABSTRACT_E, ?>> D getEntityDao(Collection<D> daoCollection, Class<E> entityClass) {
        
        for(D dao : daoCollection) {
            if(dao.isMyEntity(entityClass)) {
                return dao;
            }
        }

        throw new RuntimeException("Cannot find dao for " + entityClass.getName());   
        
    }

}
