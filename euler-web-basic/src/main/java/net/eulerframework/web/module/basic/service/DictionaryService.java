package net.eulerframework.web.module.basic.service;

import javax.annotation.Resource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.eulerframework.web.core.base.service.impl.BaseService;

@Service
public class DictionaryService extends BaseService {
    
    //@Resource private DictionaryDao dictionaryDao;
    
    @Resource private ObjectMapper objectMapper;

//    public EasyUIPageResponse<Dictionary> findCodeTableByPage(QueryRequest queryRequest, int pageIndex, int pageSize) {
//        return this.dictionaryDao.findDictionaryByPage(queryRequest, pageIndex, pageSize);
//    }
//
//    public void saveCodeTable(Dictionary dictionary) {
//        this.dictionaryDao.saveOrUpdate(dictionary);
//    }
//
//    public void deleteCodeTables(Serializable[] idArray) {
//        this.dictionaryDao.deleteByIds(idArray);
//        
//    }
}
