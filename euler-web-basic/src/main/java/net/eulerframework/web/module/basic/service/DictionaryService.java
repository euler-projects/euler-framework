package net.eulerframework.web.module.basic.service;

import java.io.Serializable;
import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.eulerframework.web.config.SystemProperties;
import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.base.request.QueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.core.base.service.impl.BaseService;
import net.eulerframework.web.module.basic.dao.DictionaryDao;
import net.eulerframework.web.module.basic.entity.Dictionary;

@Service
public class DictionaryService extends BaseService {
    
    @Resource private DictionaryDao dictionaryDao;
    
    @Resource private ObjectMapper objectMapper;

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

    public PageResponse<Dictionary> findCodeTableByPage(QueryRequest queryRequest, int pageIndex, int pageSize) {
        return this.dictionaryDao.findDictionaryByPage(queryRequest, pageIndex, pageSize);
    }

    public void saveCodeTable(Dictionary dictionary) {
        this.dictionaryDao.saveOrUpdate(dictionary);
    }

    public void deleteCodeTables(Serializable[] idArray) {
        this.dictionaryDao.deleteByIds(idArray);
        
    }
}
