package net.eulerform.web.module.basedata.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.eulerform.common.FileReader;
import net.eulerform.web.core.base.service.impl.BaseService;
import net.eulerform.web.module.basedata.dao.ICodeTableDao;
import net.eulerform.web.module.basedata.entity.CodeTable;
import net.eulerform.web.module.basedata.service.IBaseDataService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BaseDataService extends BaseService implements IBaseDataService {
    
    private ICodeTableDao codeTableDao;

    public void setCodeTableDao(ICodeTableDao codeTableDao) {
        this.codeTableDao = codeTableDao;
    }

    @Override
    public void createCodeDict() throws IOException {
        System.out.println("createCodeDict");
        List<CodeTable> codes = this.codeTableDao.findAllCodeOrderByName();
        List<Dict> dict = null;
        String name = "";
        StringBuffer resutlBuffer = new StringBuffer();
        ObjectMapper om = new ObjectMapper();
        String path = this.getServletContext().getRealPath("/");
        System.out.println(path);
        FileReader.deleteFile("D:/test.txt");
        for(CodeTable code : codes) {
            if(!name.equals(code.getName())){
                if(dict != null && !dict.isEmpty()){
                    if(resutlBuffer.length() > 0)
                        resutlBuffer.delete(0,resutlBuffer.length());
                    resutlBuffer.append(name);
                    resutlBuffer.append('=');
                    try {
                        resutlBuffer.append(om.writer().writeValueAsString(dict));
                    } catch (JsonProcessingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    resutlBuffer.append(';');
                    resutlBuffer.append('\n');
                    FileReader.writeFile("D:/test.txt", resutlBuffer.toString());
                }                
                dict = new ArrayList<>();
                name=code.getName();
            }
            dict.add(new Dict(code));
        }

        if(resutlBuffer.length() > 0)
            resutlBuffer.delete(0,resutlBuffer.length());
        resutlBuffer.append(name);
        resutlBuffer.append('=');
        try {
            resutlBuffer.append(om.writer().writeValueAsString(dict));
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        resutlBuffer.append(';');
        resutlBuffer.append('\n');
        FileReader.writeFile("D:/test.txt", resutlBuffer.toString());
    }
    
    public class Dict{
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
        
        public Dict(CodeTable codeTable){
            this.key = codeTable.getKey();
            this.value = codeTable.getValue();
        }
    }

}
