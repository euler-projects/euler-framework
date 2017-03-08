package net.eulerframework.web.module.basic.dao;

import java.util.List;

import net.eulerframework.web.core.base.dao.impl.hibernate5.BaseDao;
import net.eulerframework.web.module.basic.entity.Dictionary;
import net.eulerframework.web.module.basic.entity.DictionaryCode;

public class DictionaryCodeDao extends BaseDao<DictionaryCode> {

    public List<Dictionary> findDictionaryByCode(String code) {
        DictionaryCode dictionaryCode = this.load(code);
        
        if(dictionaryCode == null) {
            return null;
        }
        
        return dictionaryCode.getDictionarys();
    }
    
//    public void addDictionary(String code, Dictionary dictionary) {
//        Assert.notNull(code);
//        Assert.notNull(dictionary);
//        JavaObjectUtils.clearEmptyProperty(dictionary);
//        
//        DictionaryCode dictionaryCode = this.load(code);
//        
//        if(dictionaryCode == null) {
//            throw new RuntimeException("Dictionary code is '" + code + "' not exists");
//        }
//        
//        List<Dictionary> dictionaries = dictionaryCode.getDictionarys();
//        
//        if(dictionaries == null) {
//            dictionaries = new ArrayList<>();
//        }
//        
//        dictionaries.add(dictionary);
//        
//        this.saveOrUpdate(dictionaryCode);        
//    }
//    
//    public void saveDictionary(String code, List<Dictionary> dictionaries) {
//        Assert.notNull(code);
//        Assert.notNull(dictionaries);
//        for (Dictionary entity : dictionaries)
//                JavaObjectUtils.clearEmptyProperty(entity);
//        
//        DictionaryCode dictionaryCode = this.load(code);
//        
//        if(dictionaryCode == null) {
//            throw new RuntimeException("Dictionary code is '" + code + "' not exists");
//        }
//        
//        dictionaryCode.setDictionarys(dictionaries);
//        
//        this.saveOrUpdate(dictionaryCode);        
//    }
}
