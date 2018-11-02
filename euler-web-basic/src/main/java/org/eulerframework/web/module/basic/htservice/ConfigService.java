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
package org.eulerframework.web.module.basic.htservice;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import org.eulerframework.web.core.base.service.impl.BaseService;
import org.eulerframework.web.module.basic.dao.ConfigDao;
import org.eulerframework.web.module.basic.entity.Config;

@Service
public class ConfigService extends BaseService {

    @Resource private ConfigDao configDao;
    
    public Config findConfig(String key) {
        return this.configDao.load(key);
    }
}
