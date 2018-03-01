package net.eulerframework.web.module.basic.htservice;

import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import net.eulerframework.web.core.base.service.impl.BaseService;
import net.eulerframework.web.module.basic.dao.DictionaryCodeDao;
import net.eulerframework.web.module.basic.dao.DictionaryDao;
import net.eulerframework.web.module.basic.entity.Dictionary;

@Service
public class DictionaryService extends BaseService {
    
    @Resource DictionaryCodeDao dictionaryCodeDao;
    @Resource DictionaryDao dictionaryDao;
    
    public List<Dictionary> findDictionariesByCode(String code, Locale locale) {
        return this.dictionaryCodeDao.findDictionariesByCode(code, locale);
    }
    
    public Dictionary findDictionaryByKey(String key, Locale locale) {
        return this.dictionaryDao.findDictionaryByKey(key, locale);
    }
}
