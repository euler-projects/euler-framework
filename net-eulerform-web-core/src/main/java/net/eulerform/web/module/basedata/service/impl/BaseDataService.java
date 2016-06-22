package net.eulerform.web.module.basedata.service.impl;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.eulerform.common.FileReader;
import net.eulerform.web.core.base.entity.QueryRequest;
import net.eulerform.web.core.base.entity.PageResponse;
import net.eulerform.web.core.base.service.impl.BaseService;
import net.eulerform.web.core.cache.ObjectCache;
import net.eulerform.web.module.authentication.util.UserContext;
import net.eulerform.web.module.basedata.dao.ICodeTableDao;
import net.eulerform.web.module.basedata.dao.IModuleDao;
import net.eulerform.web.module.basedata.entity.CodeTable;
import net.eulerform.web.module.basedata.entity.Module;
import net.eulerform.web.module.basedata.service.IBaseDataService;

public class BaseDataService extends BaseService implements IBaseDataService {
    
    private static List<Module> allModules;
    private static ObjectCache<String, CodeTable> allConfigs = new ObjectCache<>(86_400_000L);//所有配置缓存一天
    
    private ICodeTableDao codeTableDao;
    
    private IModuleDao moduleDao;    

    public void setModuleDao(IModuleDao moduleDao) {
        this.moduleDao = moduleDao;
    }

    public void setCodeTableDao(ICodeTableDao codeTableDao) {
        this.codeTableDao = codeTableDao;
    }

    @Override
    public void loadBaseData() {
        
        //allModules
        allModules = this.moduleDao.findAllInOrder(); 
        
        //allConfigs
        List<CodeTable> allConfigs = this.codeTableDao.findAllConfig();        
        if(allConfigs !=null){
            for(CodeTable each : allConfigs){
                BaseDataService.allConfigs.put(each.getKey(), each);
            }
        }       
    }

    @Override
    public List<Module> findAllModule() {
        return allModules;
    }

    @Override
    public String findConfigValue(String key) {
        CodeTable resultCodeTable = allConfigs.get(key);
        
        if(resultCodeTable == null) {
            resultCodeTable = this.codeTableDao.findConfig(key);
            if(resultCodeTable != null){
                BaseDataService.allConfigs.put(resultCodeTable.getKey(), resultCodeTable);                
            }
        }
        
        if(resultCodeTable == null)
            return null;
        
        return resultCodeTable.getValue();
    }

    @Override
    public void saveCodeTable(CodeTable codeTable) {
        this.codeTableDao.saveOrUpdate(codeTable);
    }
    
    
    //==================================================================================
    
    private String codeTableJsFilePath = "resources/scripts/lib/common-dict.js";
    private ObjectMapper objectMapper = new ObjectMapper();
    private String webRootRealPath;

    @Override
    public void setWebRootRealPath(String webRootRealPath) {
        this.webRootRealPath = webRootRealPath;
    }

    public void setCodeTableJsFilePath(String codeTableJsFilePath) {
        this.codeTableJsFilePath = codeTableJsFilePath;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @Override
    public void createCodeDict() throws IOException {
        List<CodeTable> codes = this.codeTableDao.findAllCodeOrderByName();
        Map<String, List<Dict>> codeTableMap = new HashMap<>();
        List<Dict> dict = null;
        String name = "";
        String codeTableJsFileRealPath = this.webRootRealPath+this.codeTableJsFilePath;
        System.out.println("createCodeDict:"+codeTableJsFileRealPath);
        for(CodeTable code : codes) {
            if(!name.equals(code.getName())){
                if(dict != null && !dict.isEmpty()){
                    codeTableMap.put(name, dict);
                }                
                dict = new ArrayList<>();
                name=code.getName();
            }
            dict.add(new Dict(code));
        }

        if(dict != null && !dict.isEmpty()){
            codeTableMap.put(name, dict);
        }
        FileReader.deleteFile(codeTableJsFileRealPath);
        this.writeCodeTableToJs(codeTableMap, codeTableJsFileRealPath);
    }
    
    private void writeCodeTableToJs(Map<String, List<Dict>> codeTableMap, String path) throws IOException{
        StringBuffer resutlBuffer = new StringBuffer();

        for (Map.Entry<String, List<Dict>> entry : codeTableMap.entrySet()) {
            if(resutlBuffer.length() > 0)
                resutlBuffer.delete(0,resutlBuffer.length());
            resutlBuffer.append(entry.getKey());
            resutlBuffer.append('=');
            resutlBuffer.append(this.objectMapper.writer().writeValueAsString(entry.getValue()));
            resutlBuffer.append(';');
            resutlBuffer.append('\n');
            FileReader.writeFile(path, resutlBuffer.toString());
        }        
    }

    @SuppressWarnings("unused")
    private class Dict{
        private String key;
        private String value;
        public String getKey() {
            return key;
        }
        public void setKey(String key) {
            this.key = key;
        }
        public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
        }
        
        private Dict(CodeTable codeTable){
            this.key = codeTable.getKey();
            this.value = codeTable.getValue();
        }
    }

    @Override
    public PageResponse<CodeTable> findCodeTableByPage(QueryRequest queryRequest, int pageIndex, int pageSize) {
        PageResponse<CodeTable> result = new PageResponse<>();
        long total = this.codeTableDao.findCount();
        List<CodeTable> data = this.codeTableDao.findCodeTableByPage(queryRequest, pageIndex, pageSize);
        
        for(CodeTable each :data) {
            each.setCreateByName(UserContext.getUserNameAndCodeById(each.getCreateBy()));
            each.setModifyByName(UserContext.getUserNameAndCodeById(each.getModifyBy()));
        }
        
        result.setTotal(total);
        result.setRows(data);
        result.setPageIndex(pageIndex);
        result.setPageSize(pageSize);
        return result;
    }

    @Override
    public PageResponse<Module> findModuleByPage(QueryRequest queryRequest, int pageIndex, int pageSize) {
        PageResponse<Module> result = new PageResponse<>();
        long total = this.moduleDao.findCount();
        List<Module> data = this.moduleDao.findModuleByPage(queryRequest, pageIndex, pageSize);
        
        result.setTotal(total);
        result.setRows(data);
        result.setPageIndex(pageIndex);
        result.setPageSize(pageSize);
        return result;
    }

    @Override
    public void deleteCodeTables(Serializable[] idArray) {
        this.codeTableDao.deleteByIds(idArray);
    }

    @Override
    public void deleteCodeTable(Serializable id) {
        this.codeTableDao.deleteById(id);
    }

}
