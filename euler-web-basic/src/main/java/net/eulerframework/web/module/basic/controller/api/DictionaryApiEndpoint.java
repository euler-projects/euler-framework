/**
 * 
 */
package net.eulerframework.web.module.basic.controller.api;

import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import net.eulerframework.web.core.annotation.ApiEndpoint;
import net.eulerframework.web.core.base.controller.AbstractApiEndpoint;
import net.eulerframework.web.module.basic.entity.Dictionary;
import net.eulerframework.web.module.basic.service.DictionaryService;

/**
 * @author cFrost
 *
 */
@ApiEndpoint
@RequestMapping("/dict")
public class DictionaryApiEndpoint extends AbstractApiEndpoint {
    
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
