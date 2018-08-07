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
package net.eulerframework.web.module.basic.dao;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import net.eulerframework.web.core.base.dao.impl.hibernate5.BaseDao;
import net.eulerframework.web.module.basic.entity.Dictionary;
import net.eulerframework.web.module.basic.entity.DictionaryCode;

public class DictionaryCodeDao extends BaseDao<DictionaryCode> {

    public List<Dictionary> findDictionariesByCode(String code, Locale locale) {
        Assert.hasText(code, "code is empty");
        Assert.notNull(locale, "locale is null");
        DictionaryCode dictionaryCode = this.load(code);
        
        if(dictionaryCode == null || CollectionUtils.isEmpty(dictionaryCode.getDictionarys())) {
            return null;
        }
        
        return dictionaryCode.getDictionarys()
                .stream()
                .filter(dict -> locale.equals(dict.getLocale()))
                .collect(Collectors.toList());
    }
}
