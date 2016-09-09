package net.eulerform.web.module.basedata.service.impl;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.springframework.web.context.ContextLoader;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.eulerform.common.util.FileReader;
import net.eulerform.web.core.base.request.QueryRequest;
import net.eulerform.web.core.base.response.PageResponse;
import net.eulerform.web.core.base.service.impl.BaseService;
import net.eulerform.web.core.cache.ObjectCache;
import net.eulerform.web.module.basedata.dao.ICodeTableDao;
import net.eulerform.web.module.basedata.dao.IModuleDao;
import net.eulerform.web.module.basedata.dao.IPageDao;
import net.eulerform.web.module.basedata.dao.impl.CodeTableDao;
import net.eulerform.web.module.basedata.entity.CodeTable;
import net.eulerform.web.module.basedata.entity.EmailConfig;
import net.eulerform.web.module.basedata.entity.Module;
import net.eulerform.web.module.basedata.entity.Page;
import net.eulerform.web.module.basedata.service.IBaseDataService;

public class BaseDataService extends BaseService implements IBaseDataService {
    
    private final ObjectCache<String, CodeTable> allConfigs = new ObjectCache<>(86_400_000L);//所有配置缓存一天
    
    private ICodeTableDao codeTableDao;
    
    private IModuleDao moduleDao;
    private IPageDao pageDao;
    
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

    public void setModuleDao(IModuleDao moduleDao) {
        this.moduleDao = moduleDao;
    }

    public void setPageDao(IPageDao pageDao) {
        this.pageDao = pageDao;
    }

    public void setCodeTableDao(ICodeTableDao codeTableDao) {
        this.codeTableDao = codeTableDao;
    }

    @Override
    public void loadBaseData() {
        
        //allModules
        this.refreshModules();
        
        //allConfigs
        this.refreshConfigs();
        
        //contextPaht
        ServletContext sc = ContextLoader.getCurrentWebApplicationContext().getServletContext();
        String contextPath = sc.getContextPath();
        sc.setAttribute("contextPath", contextPath);
        
        sc.setAttribute("eulerformVersion", "1.0.1");
    }

    @Override
    public String findConfigValue(String key) {
        CodeTable resultCodeTable = this.allConfigs.get(key);
        
        if(resultCodeTable == null) {
            resultCodeTable = this.codeTableDao.findConfig(key);
            if(resultCodeTable != null){
                this.allConfigs.put(resultCodeTable.getKey(), resultCodeTable);                
            }
        }
        
        if(resultCodeTable == null)
            return null;
        
        return resultCodeTable.getValue();
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
            FileReader.writeFile(path, resutlBuffer.toString(), true);
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
    
    //==================================================================================

    @Override
    public PageResponse<CodeTable> findCodeTableByPage(QueryRequest queryRequest, int pageIndex, int pageSize) {
        return this.codeTableDao.findCodeTableByPage(queryRequest, pageIndex, pageSize);
    }

    @Override
    public List<Module> findAllModuleFromDB() {
        return this.moduleDao.findAllInOrder();
    }

    @Override
    public void saveCodeTable(CodeTable codeTable) {
        this.codeTableDao.saveOrUpdate(codeTable);
        this.refreshConfigs();
    }

    @Override
    public void deleteCodeTables(Serializable[] idArray) {
        this.codeTableDao.deleteByIds(idArray);
        this.refreshConfigs();
        
    }

    @Override
    public Module findModuleById(String id) {
        return this.moduleDao.load(id);
    }

    @Override
    public Page findPageById(String id) {
        return this.pageDao.load(id);
    }

    @Override
    public void savePage(Page page) {
        if(page.getModuleId() == null)
            throw new RuntimeException("Module Id 不能为空");
        this.pageDao.saveOrUpdate(page);
        this.refreshModules();
    }

    @Override
    public void deletePage(Serializable id) {
        this.pageDao.deleteById(id);
        this.refreshModules();
    }

    @Override
    public void saveModule(Module module) {
        if(module.getId() != null){
            List<Page> pages = this.moduleDao.load(module.getId()).getPages();
            if(module.getPages() == null || module.getPages().isEmpty()) {
                module.setPages(pages);
            }
        }
        this.moduleDao.saveOrUpdate(module);
        this.refreshModules();
    }

    @Override
    public void deleteModule(Serializable id) {
        List<Page> pages = this.moduleDao.load(id).getPages();
        this.pageDao.deleteAll(pages);
        this.moduleDao.deleteById(id);
        this.refreshModules();
    }
    
    private void refreshModules(){
        this.moduleDao.flushSession();
        List<Module> allModules = this.moduleDao.findAllInOrder();
        ContextLoader.getCurrentWebApplicationContext().getServletContext().setAttribute("menu", allModules);        
    }

    private void refreshConfigs() {
        List<CodeTable> allConfigs = this.codeTableDao.findAllConfig();        
        if(allConfigs !=null){
            for(CodeTable each : allConfigs){
                this.allConfigs.put(each.getKey(), each);
            }
        }
    }

    private final ObjectCache<String, EmailConfig> mailConfigCache = new ObjectCache<>(86_400_000L);//所有配置缓存一天
    @Override
    public void saveSystemEmail(EmailConfig mailConfig) {
        String username = mailConfig.getUsername();
        String password = mailConfig.getPassword();
        String smtp = mailConfig.getSmtp();
        String sender = mailConfig.getSender();
        
        CodeTable usernameCode = this.codeTableDao.findConfig("sysEmaiUsername");
        CodeTable passwordCode = this.codeTableDao.findConfig("sysEmaiPassword");
        CodeTable smtpCode = this.codeTableDao.findConfig("sysEmaiSmtp");
        CodeTable senderCode = this.codeTableDao.findConfig("sysEmaiSender");
        
        if(usernameCode == null){
            usernameCode = new CodeTable();
            usernameCode.setCodeType(CodeTableDao.PROPERTY_TYPE);
            usernameCode.setKey("sysEmaiUsername");
            usernameCode.setName("sysEmaiUsername");
            usernameCode.setValue(username);
        } else {
            usernameCode.setValue(username);
        }
        if(passwordCode == null){
            passwordCode = new CodeTable();
            passwordCode.setCodeType(CodeTableDao.PROPERTY_TYPE);
            passwordCode.setKey("sysEmaiPassword");
            passwordCode.setName("sysEmaiPassword");
            passwordCode.setValue(password);
        } else {
            passwordCode.setValue(password);
        }
        if(smtpCode == null){
            smtpCode = new CodeTable();
            smtpCode.setCodeType(CodeTableDao.PROPERTY_TYPE);
            smtpCode.setKey("sysEmaiSmtp");
            smtpCode.setName("sysEmaiSmtp");
            smtpCode.setValue(smtp);
        } else {
            smtpCode.setValue(smtp);
        }
        if(senderCode == null){
            senderCode = new CodeTable();
            senderCode.setCodeType(CodeTableDao.PROPERTY_TYPE);
            senderCode.setKey("sysEmaiSender");
            senderCode.setName("sysEmaiSender");
            senderCode.setValue(sender);
        } else {
            senderCode.setValue(sender);
        }
        
        this.codeTableDao.saveOrUpdate(usernameCode);
        this.codeTableDao.saveOrUpdate(passwordCode);
        this.codeTableDao.saveOrUpdate(smtpCode);
        this.codeTableDao.saveOrUpdate(senderCode);
        mailConfigCache.put("1", this.findEmailConfig());
        
    }

    @Override
    public EmailConfig findEmailConfig() {
        EmailConfig config = mailConfigCache.get("1");
        if(config != null) {
            return config;
        }
        CodeTable usernameCode = this.codeTableDao.findConfig("sysEmaiUsername");
        CodeTable passwordCode = this.codeTableDao.findConfig("sysEmaiPassword");
        CodeTable smtpCode = this.codeTableDao.findConfig("sysEmaiSmtp");
        CodeTable senderCode = this.codeTableDao.findConfig("sysEmaiSender");
        

        if(usernameCode == null || passwordCode == null || smtpCode == null || senderCode == null)
            return null;
        
        EmailConfig emailConfig = new EmailConfig();
        emailConfig.setUsername(usernameCode.getValue());
        emailConfig.setPassword(passwordCode.getValue());
        emailConfig.setSmtp(smtpCode.getValue());
        emailConfig.setSender(senderCode.getValue());
        mailConfigCache.put("1", emailConfig);
        return emailConfig;
    }
}
