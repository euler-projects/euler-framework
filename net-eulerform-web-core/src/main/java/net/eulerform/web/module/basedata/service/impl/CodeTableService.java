package net.eulerform.web.module.basedata.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.eulerform.common.FileReader;
import net.eulerform.web.core.base.service.impl.BaseService;
import net.eulerform.web.module.basedata.dao.ICodeTableDao;
import net.eulerform.web.module.basedata.entity.CodeTable;
import net.eulerform.web.module.basedata.service.ICodeTableService;

public class CodeTableService extends BaseService implements ICodeTableService {
    
    private ICodeTableDao codeTableDao;

    public void setCodeTableDao(ICodeTableDao codeTableDao) {
        this.codeTableDao = codeTableDao;
    }
    
    private String codeTableJsFilePath = "resources/scripts/lib/common-dict.js";
    private ObjectMapper objectMapper;
    private String webRootRealPath;
    
    public void setWebRootRealPath(String webRootRealPath) {
        this.webRootRealPath = webRootRealPath;
    }

    public void setCodeTableJsFilePath(String codeTableJsFilePath) {
        this.codeTableJsFilePath = codeTableJsFilePath;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public CodeTableService(){
        this.objectMapper = new ObjectMapper();
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
    public void saveCodeTable(CodeTable codeTable) {
        this.codeTableDao.saveOrUpdate(codeTable);
    }

}
