package net.eulerform.web.core.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.eulerform.common.util.FilePathTool;
import net.eulerform.common.util.GlobalProperties;
import net.eulerform.common.util.GlobalPropertyReadException;
import net.eulerform.common.util.StringTool;

public abstract class WebConfig {
    
    private static class WebConfigKey {
        private final static String WEB_UPLOAD_PATH = "web.uploadPath";
        private final static String WEB_JSP_PATH = "web.jspPath";
        private final static String WEB_ENABLE_JSP_AUTO_DEPLOY = "web.enableJspAutoDeploy";

        private final static String REST_ROOT_PATH = "rest.rootPath";
    }
    
    private static class WebConfigDefault {
        private final static String WEB_UPLOAD_PATH = "/upload";
        private final static String WEB_JSP_PATH = "/WEB-INF/modulePages";
        private final static boolean WEB_ENABLE_JSP_AUTO_DEPLOY = false;

        private final static String REST_ROOT_PATH = "/api";
    }
    
    protected final static Logger log = LogManager.getLogger();
    
    private static String uploadPath;
    private static String jspPath;
    private static Boolean enableJspAutoDeploy;
    
    private static String restRootPath;
    
    public static String getRestRootPath() {
        if(restRootPath == null) {
            try {
                restRootPath = GlobalProperties.get(WebConfigKey.REST_ROOT_PATH);
                
                if(StringTool.isNull(restRootPath))
                    throw new RuntimeException(WebConfigKey.REST_ROOT_PATH + "不能为空");

                while(restRootPath.endsWith("*")){
                    restRootPath = restRootPath.substring(0, restRootPath.length()-1);
                }

                restRootPath = FilePathTool.changeToUnixFormat(restRootPath);
            } catch (GlobalPropertyReadException e) {
                restRootPath = WebConfigDefault.REST_ROOT_PATH;
                log.warn("Couldn't load " + WebConfigKey.REST_ROOT_PATH + " , use " + restRootPath + " for default.");
            }
        }
        return restRootPath;
        
    }

    public static String getUploadPath() {
        if(uploadPath == null) {
            try {
                uploadPath = FilePathTool.changeToUnixFormat(GlobalProperties.get(WebConfigKey.WEB_UPLOAD_PATH));
            } catch (GlobalPropertyReadException e) {
                uploadPath = WebConfigDefault.WEB_UPLOAD_PATH;
                log.warn("Couldn't load " + WebConfigKey.WEB_UPLOAD_PATH + " , use " + uploadPath + " for default.");
            }
        }
        return uploadPath;
        
    }
    
    public static String getJspPath() {
        if(jspPath == null) {
            try {
                jspPath = FilePathTool.changeToUnixFormat(GlobalProperties.get(WebConfigKey.WEB_JSP_PATH));
            } catch (GlobalPropertyReadException e) {
                uploadPath = WebConfigDefault.WEB_JSP_PATH;
                log.warn("Couldn't load " + WebConfigKey.WEB_JSP_PATH + " , use " + jspPath + " for default.");
            }
        }
        return jspPath;
    }
    
    public static boolean isJspAutoDeployEnabled() {
        if(enableJspAutoDeploy == null) {
            try {
                enableJspAutoDeploy = Boolean.parseBoolean(GlobalProperties.get(WebConfigKey.WEB_ENABLE_JSP_AUTO_DEPLOY));
            } catch (GlobalPropertyReadException e) {
                enableJspAutoDeploy = WebConfigDefault.WEB_ENABLE_JSP_AUTO_DEPLOY;
                log.warn("Couldn't load " + WebConfigKey.WEB_ENABLE_JSP_AUTO_DEPLOY + " , use " + enableJspAutoDeploy + " for default.");
            }
        }
        return enableJspAutoDeploy;
    }
}
