package net.eulerframework.web.module.basic.service.impl;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.eulerframework.common.util.io.file.SimpleFileIOUtils;
import net.eulerframework.web.config.SystemProperties;
import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.base.request.QueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.core.base.service.impl.BaseService;
import net.eulerframework.web.module.basic.dao.IDictionaryDao;
import net.eulerframework.web.module.basic.entity.Dictionary;
import net.eulerframework.web.module.basic.service.IDictionaryService;

@Service
public class DictionaryService extends BaseService implements IDictionaryService {
    
    @Resource private IDictionaryDao dictionaryDao;
    
    @Resource private ObjectMapper objectMapper;

    @Override
    public void loadBaseData() {
        
        //contextPaht
        ServletContext sc = this.getServletContext();
        String contextPath = sc.getContextPath();
        
        sc.setAttribute("__CONTEXT_PATH", contextPath);
        sc.setAttribute("__ASSETS_PATH", contextPath + WebConfig.getAssetsPath());
        
        sc.setAttribute("__FILE_DOWNLOAD_PATH", contextPath + "/file");
        sc.setAttribute("__FILE_UPLOAD_ACTION", contextPath + "/uploadFile");

        sc.setAttribute("__DEBUG_MODE", WebConfig.isDebugMode());
        sc.setAttribute("__PROJECT_VERSION", WebConfig.getProjectVersion());
        sc.setAttribute("__PROJECT_MODE", WebConfig.getProjectMode());
        sc.setAttribute("__PROJECT_BUILDTIME", WebConfig.getProjectBuildtime());

        sc.setAttribute("__SITENAME", WebConfig.getSitename());
        sc.setAttribute("__COPYRIGHT_HOLDER", WebConfig.getCopyrightHolder());
        
        sc.setAttribute("__ADMIN_DASHBOARD_BRAND_ICON", contextPath + WebConfig.getAdminDashboardBrandIcon());
        sc.setAttribute("__ADMIN_DASHBOARD_BRAND_TEXT", WebConfig.getAdminDashboardBrandText());
        
        sc.setAttribute("__FRAMEWORK_VERSION", SystemProperties.frameworkVersion());
    }
    
    @Override
    public void createCodeDict() throws IOException {
        String webRootRealPath = this.getServletContext().getRealPath("/");
        String codeTableJsFilePath = "resources/scripts/lib/common-dict.js";
        
        String codeTableJsFileRealPath = webRootRealPath+codeTableJsFilePath;
        this.logger.info("createCodeDict:"+codeTableJsFileRealPath);

        List<Dictionary> codes = this.dictionaryDao.findAllDictionaryOrderByName();
        Map<String, List<Dict>> codeTableMap = new HashMap<>();
        List<Dict> dict = null;
        String name = "";
        for(Dictionary code : codes) {
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
        SimpleFileIOUtils.deleteFile(codeTableJsFileRealPath);
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
            SimpleFileIOUtils.writeFile(path, resutlBuffer.toString(), true);
        }        
    }

    @SuppressWarnings("unused")
    private class Dict{
        private String key;
        private String value;
        private String valuei18n;
        private String style;
                
        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public String getValuei18n() {
            return valuei18n;
        }

        public String getStyle() {
            return style;
        }

        private Dict(Dictionary dictionary){
            this.key = dictionary.getKey();
            this.value = dictionary.getValue();
            this.style = dictionary.getCssStyle();
            this.valuei18n = dictionary.getValuei18n();
        }
    }
    
    //==================================================================================

    @Override
    public PageResponse<Dictionary> findCodeTableByPage(QueryRequest queryRequest, int pageIndex, int pageSize) {
        return this.dictionaryDao.findDictionaryByPage(queryRequest, pageIndex, pageSize);
    }

    @Override
    public void saveCodeTable(Dictionary dictionary) {
        this.dictionaryDao.saveOrUpdate(dictionary);
    }

    @Override
    public void deleteCodeTables(Serializable[] idArray) {
        this.dictionaryDao.deleteByIds(idArray);
        
    }
}
