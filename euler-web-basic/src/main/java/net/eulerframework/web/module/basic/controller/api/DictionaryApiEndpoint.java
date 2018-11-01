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
package net.eulerframework.web.module.basic.controller.api;

import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import net.eulerframework.web.core.annotation.ApiEndpoint;
import net.eulerframework.web.core.base.controller.ApiSupportWebController;
import net.eulerframework.web.module.basic.entity.Dictionary;
import net.eulerframework.web.module.basic.htservice.DictionaryService;

/**
 * @author cFrost
 *
 */
@ApiEndpoint
@RequestMapping("/dict")
public class DictionaryApiEndpoint extends ApiSupportWebController {
    
    @Resource DictionaryService dictionaryService;
    
    @RequestMapping(value = "code/{code}", method = RequestMethod.GET) 
    public List<Dictionary> findDictionariesByCode(@PathVariable("code") String code, Locale locale) {
        return this.dictionaryService.findDictionariesByCode(code, locale);
    }

    @RequestMapping(value = "key/{key}", method = RequestMethod.GET) 
    public Dictionary findDictionaryByKey(@PathVariable("key") String key, Locale locale) {
        return this.dictionaryService.findDictionaryByKey(key, locale);
    }

}
