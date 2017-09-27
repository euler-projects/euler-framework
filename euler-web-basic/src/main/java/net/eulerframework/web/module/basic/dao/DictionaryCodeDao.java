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
