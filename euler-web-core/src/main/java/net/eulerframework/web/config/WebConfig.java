package net.eulerframework.web.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.context.ContextLoader;

import net.eulerframework.common.util.FilePathTool;
import net.eulerframework.common.util.GlobalProperties1;
import net.eulerframework.common.util.GlobalPropertyReadException;
import net.eulerframework.common.util.StringTool;

public abstract class WebConfig {
    
    private static class WebConfigKey {
        private final static String CORE_I18N_REFRESH_FREQ = "core.i18nRefreshFreq";
        
        private final static String WEB_UPLOAD_PATH = "web.uploadPath";
        private final static String WEB_JSP_PATH = "web.jspPath";
        private final static String WEB_ADMIN_JSP_PATH = "web.adminJspPath";
//        private final static String WEB_ENABLE_JSP_AUTO_DEPLOY = "web.enableJspAutoDeploy";
        
        private final static String ADMIN_ROOT_PATH = "admin.rootPath";
        
        private final static String API_ROOT_PATH = "api.rootPath";        

        private final static String CACHE_RAM_CAHCE_CLEAN_FREQ = "cache.ramCacheCleanFreq";
        private final static String CACHE_USERCONTEXT_CAHCE_LIFE = "cache.userContextCacheLife";      

        private static final String MULITPART_LOCATION = "multiPart.location";
        private static final String MULITPART_MAX_FILE_SIZE = "multiPart.maxFileSize";
        private static final String MULITPART_MAX_REQUEST_SIZE = "multiPart.maxRequestSize";
        private static final String MULITPART_FILE_SIZE_THRESHOLD = "multiPart.fileSizeThreshold";        

        private static final String SEC_WEB_AUTHENTICATION_TYPE = "sec.web.authenticationType";
        private static final String SEC_API_AUTHENTICATION_TYPE = "sec.api.authenticationType";        
        private static final String SEC_OAUTH_SERVER_TYPE = "sec.oauth.severType";   
        private static final String SEC_MIN_PASSWORD_LENGTH = "sec.minPasswordLength";
    }
    
    private static class WebConfigDefault {
        private final static int CORE_I18N_REFRESH_FREQ = 86_400;
        
        private final static String WEB_UPLOAD_PATH_UNIX = "file:///var/lib/euler-framework/archive/files";
        private final static String WEB_UPLOAD_PATH_WIN = "file://C:\\euler-framework-data\\archive\files";
        private final static String WEB_JSP_PATH = "/WEB-INF/jsp/themes";
        private final static String WEB_ADMIN_JSP_PATH = "/WEB-INF/jsp/admin/themes";
//        private final static boolean WEB_ENABLE_JSP_AUTO_DEPLOY = false;

        private final static String ADMIN_ROOT_PATH = "/admin";
        private final static String API_ROOT_PATH = "/api";
        
        private final static long CACHE_RAM_CAHCE_CLEAN_FREQ = 60_000L;
        private final static long CACHE_USERCONTEXT_CAHCE_LIFE = 600_000L; 
        
        private static final String MULITPART_LOCATION = null;
        private static final long MULITPART_MAX_FILE_SIZE = 51_200L;
        private static final long MULITPART_MAX_REQUEST_SIZE = 51_200L;
        private static final int MULITPART_FILE_SIZE_THRESHOLD = 1_024;
        
        private static final WebAuthenticationType SEC_WEB_AUTHENTICATION_TYPE = WebAuthenticationType.LOCAL;
        private static final ApiAuthenticationType SEC_API_AUTHENTICATION_TYPE = ApiAuthenticationType.NONE;
        private static final int SEC_MIN_PASSWORD_LENGTH = 6;
        
        private static final OAuthServerType SEC_OAUTH_SERVER_TYPE = OAuthServerType.NEITHER;
    }
    
    protected final static Logger log = LogManager.getLogger();

    private static Integer i18nRefreshFreq;
    
    private static String uploadPath;
    private static String jspPath;
    private static String adminJspPath;
//    private static Boolean enableJspAutoDeploy;

    private static String adminRootPath;
    private static String apiRootPath;

    private static Long ramCacheCleanFreq;
    private static Long userContextCacheLife;
    
    private static WebAuthenticationType webAuthenticationType;
    private static ApiAuthenticationType apiAuthenticationType;
    private static OAuthServerType oauthServerType;

    private static Integer minPasswordLength;
    
    public static int getI18nRefreshFreq() {
        if(i18nRefreshFreq == null) {
            try {
                i18nRefreshFreq = Integer.parseInt(GlobalProperties1.get(WebConfigKey.CORE_I18N_REFRESH_FREQ));
            } catch (GlobalPropertyReadException e) {
                i18nRefreshFreq = WebConfigDefault.CORE_I18N_REFRESH_FREQ;
                log.warn("Couldn't load " + WebConfigKey.CORE_I18N_REFRESH_FREQ + " , use " + i18nRefreshFreq + " for default.");
            }
        }
        return i18nRefreshFreq;        
    }
    
    public static WebAuthenticationType getWebAuthenticationType() {
        if(webAuthenticationType == null) {
            try {
                webAuthenticationType = WebAuthenticationType.valueOf(GlobalProperties1.get(WebConfigKey.SEC_WEB_AUTHENTICATION_TYPE).toUpperCase());
            } catch (Exception e) {
                webAuthenticationType = WebConfigDefault.SEC_WEB_AUTHENTICATION_TYPE;
                log.warn("Couldn't load " + WebConfigKey.SEC_WEB_AUTHENTICATION_TYPE + " , use " + webAuthenticationType + " for default.");
            }
        }
        return webAuthenticationType;
    }
    
    public static ApiAuthenticationType getApiAuthenticationType() {
        if(apiAuthenticationType == null) {
            try {
                apiAuthenticationType = ApiAuthenticationType.valueOf(GlobalProperties1.get(WebConfigKey.SEC_API_AUTHENTICATION_TYPE).toUpperCase());
            } catch (Exception e) {
                apiAuthenticationType = WebConfigDefault.SEC_API_AUTHENTICATION_TYPE;
                log.warn("Couldn't load " + WebConfigKey.SEC_API_AUTHENTICATION_TYPE + " , use " + apiAuthenticationType + " for default.");
            }
        }
        return apiAuthenticationType;
    }

    public static OAuthServerType getOAuthSeverType() {
        if(oauthServerType == null) {
            try {
                oauthServerType = OAuthServerType.valueOf(GlobalProperties1.get(WebConfigKey.SEC_OAUTH_SERVER_TYPE).toUpperCase());
            } catch (Exception e) {
                oauthServerType = WebConfigDefault.SEC_OAUTH_SERVER_TYPE;
                log.warn("Couldn't load " + WebConfigKey.SEC_OAUTH_SERVER_TYPE + " , use " + oauthServerType + " for default.");
            }
        }
        return oauthServerType;
    }  
    
    public static String getApiRootPath() {
        if(apiRootPath == null) {
            try {
                apiRootPath = GlobalProperties1.get(WebConfigKey.API_ROOT_PATH);
                
                if(StringTool.isNull(apiRootPath))
                    throw new RuntimeException(WebConfigKey.API_ROOT_PATH + "不能为空");

                while(apiRootPath.endsWith("*")){
                    apiRootPath = apiRootPath.substring(0, apiRootPath.length()-1);
                }

                apiRootPath = FilePathTool.changeToUnixFormat(apiRootPath);
                
                if(!apiRootPath.startsWith("/"))
                    apiRootPath = "/" + apiRootPath;
                
            } catch (GlobalPropertyReadException e) {
                apiRootPath = WebConfigDefault.API_ROOT_PATH;
                log.warn("Couldn't load " + WebConfigKey.API_ROOT_PATH + " , use " + apiRootPath + " for default.");
            }
        }
        return apiRootPath;
        
    }

    public static String getAdminRootPath() {
        if(adminRootPath == null) {
            try {
                adminRootPath = GlobalProperties1.get(WebConfigKey.ADMIN_ROOT_PATH);
                
                if(StringTool.isNull(adminRootPath))
                    throw new RuntimeException(WebConfigKey.API_ROOT_PATH + "不能为空");

                while(adminRootPath.endsWith("*")){
                    adminRootPath = adminRootPath.substring(0, adminRootPath.length()-1);
                }

                adminRootPath = FilePathTool.changeToUnixFormat(adminRootPath);
                
                if(!adminRootPath.startsWith("/"))
                    adminRootPath = "/" + adminRootPath;
                
            } catch (GlobalPropertyReadException e) {
                adminRootPath = WebConfigDefault.ADMIN_ROOT_PATH;
                log.warn("Couldn't load " + WebConfigKey.ADMIN_ROOT_PATH + " , use " + adminRootPath + " for default.");
            }
        }
        return adminRootPath;
    }

    public static String getUploadPath() {
        if(uploadPath == null) {
            try {
                uploadPath = FilePathTool.changeToUnixFormat(GlobalProperties1.get(WebConfigKey.WEB_UPLOAD_PATH));
            } catch (GlobalPropertyReadException e) {
                if(System.getProperty("os.name").toLowerCase().indexOf("windows") > -1) {
                    log.info("OS is windows");
                    uploadPath = WebConfigDefault.WEB_UPLOAD_PATH_WIN;
                }
                else {
                    log.info("OS isn't windows");
                    uploadPath = WebConfigDefault.WEB_UPLOAD_PATH_UNIX;
                }
                log.warn("Couldn't load " + WebConfigKey.WEB_UPLOAD_PATH + " , use " + uploadPath + " for default.");
            }
            
            if(!uploadPath.startsWith("/") && !uploadPath.startsWith("file://")) {
                uploadPath = ContextLoader.getCurrentWebApplicationContext().getServletContext().getRealPath(uploadPath);
            } else {
                if(uploadPath.startsWith("file://")) {
                    uploadPath = uploadPath.substring("file://".length());
                }
            }
        }
        return uploadPath;
        
    }
    
    public static String getJspPath() {
        if(jspPath == null) {
            try {
                jspPath = FilePathTool.changeToUnixFormat(GlobalProperties1.get(WebConfigKey.WEB_JSP_PATH));
                jspPath = FilePathTool.changeToUnixFormat(jspPath);
            } catch (GlobalPropertyReadException e) {
                jspPath = WebConfigDefault.WEB_JSP_PATH;
                log.warn("Couldn't load " + WebConfigKey.WEB_JSP_PATH + " , use " + jspPath + " for default.");
            }
            //统一添加/结尾，这样在controller中就可以不加/前缀
            jspPath = jspPath + "/";
        }
        return jspPath;
    }
    
    public static String getAdminJspPath() {
        if(adminJspPath == null) {
            try {
                adminJspPath = FilePathTool.changeToUnixFormat(GlobalProperties1.get(WebConfigKey.WEB_ADMIN_JSP_PATH));
                adminJspPath = FilePathTool.changeToUnixFormat(adminJspPath);
            } catch (GlobalPropertyReadException e) {
                adminJspPath = WebConfigDefault.WEB_ADMIN_JSP_PATH;
                log.warn("Couldn't load " + WebConfigKey.WEB_ADMIN_JSP_PATH + " , use " + adminJspPath + " for default.");
            }
            adminJspPath = adminJspPath + "/";
        }
        return adminJspPath;
    }
//    
//    public static boolean isJspAutoDeployEnabled() {
//        if(enableJspAutoDeploy == null) {
//            try {
//                enableJspAutoDeploy = Boolean.parseBoolean(GlobalProperties1.get(WebConfigKey.WEB_ENABLE_JSP_AUTO_DEPLOY));
//            } catch (GlobalPropertyReadException e) {
//                enableJspAutoDeploy = WebConfigDefault.WEB_ENABLE_JSP_AUTO_DEPLOY;
//                log.warn("Couldn't load " + WebConfigKey.WEB_ENABLE_JSP_AUTO_DEPLOY + " , use " + enableJspAutoDeploy + " for default.");
//            }
//        }
//        return enableJspAutoDeploy;
//    }
    
    public static long getRamCacheCleanFreq() {
        if(ramCacheCleanFreq == null) {
            try {
                ramCacheCleanFreq = Long.parseLong(GlobalProperties1.get(WebConfigKey.CACHE_RAM_CAHCE_CLEAN_FREQ));
            } catch (GlobalPropertyReadException e) {
                ramCacheCleanFreq = WebConfigDefault.CACHE_RAM_CAHCE_CLEAN_FREQ;
                log.warn("Couldn't load " + WebConfigKey.CACHE_RAM_CAHCE_CLEAN_FREQ + " , use " + ramCacheCleanFreq + " for default.");
            }
        }
        return ramCacheCleanFreq;        
    }
    
    public static long getUserContextCacheLife() {
        if(userContextCacheLife == null) {
            try {
                userContextCacheLife = Long.parseLong(GlobalProperties1.get(WebConfigKey.CACHE_USERCONTEXT_CAHCE_LIFE));
            } catch (GlobalPropertyReadException e) {
                userContextCacheLife = WebConfigDefault.CACHE_USERCONTEXT_CAHCE_LIFE;
                log.warn("Couldn't load " + WebConfigKey.CACHE_USERCONTEXT_CAHCE_LIFE + " , use " + userContextCacheLife + " for default.");
            }
        }
        return userContextCacheLife;  
    }

    public static MultiPartConfig getMultiPartConfig() {
        String location = WebConfigDefault.MULITPART_LOCATION;
        long maxFileSize = WebConfigDefault.MULITPART_MAX_FILE_SIZE;
        long maxRequestSize = WebConfigDefault.MULITPART_MAX_REQUEST_SIZE;
        int fileSizeThreshold = WebConfigDefault.MULITPART_FILE_SIZE_THRESHOLD;
        try {
            location = GlobalProperties1.get(WebConfigKey.MULITPART_LOCATION);
        } catch (GlobalPropertyReadException e) {
            // DO NOTHING
            log.warn("Couldn't load "+WebConfigKey.MULITPART_LOCATION+" , use " + location + " for default.");
        }
        try {
            maxFileSize = Long.parseLong(GlobalProperties1.get(WebConfigKey.MULITPART_MAX_FILE_SIZE));
        } catch (GlobalPropertyReadException e) {
            // DO NOTHING
            log.warn("Couldn't load "+WebConfigKey.MULITPART_MAX_FILE_SIZE+" , use " + maxFileSize + " for default.");
        }
        try {
            maxRequestSize = Long.parseLong(GlobalProperties1.get(WebConfigKey.MULITPART_MAX_REQUEST_SIZE));
        } catch (GlobalPropertyReadException e) {
            // DO NOTHING
            log.warn("Couldn't load "+WebConfigKey.MULITPART_MAX_REQUEST_SIZE+" , use " + maxRequestSize + " for default.");
        }
        try {
            fileSizeThreshold = Integer.parseInt(GlobalProperties1.get(WebConfigKey.MULITPART_FILE_SIZE_THRESHOLD));
        } catch (GlobalPropertyReadException e) {
            // DO NOTHING
            log.warn("Couldn't load "+WebConfigKey.MULITPART_FILE_SIZE_THRESHOLD+" , use " + fileSizeThreshold + " for default.");
        }
        
        return new MultiPartConfig(location, maxFileSize, maxRequestSize, fileSizeThreshold);
    } 
    
    public static void main(String aa[]) {  
            System.out.println(System.getProperty("os.name"));
    }

    public static int getMinPasswordLength() {
        if(minPasswordLength == null) {
            try {
                minPasswordLength = Integer.parseInt(GlobalProperties1.get(WebConfigKey.SEC_MIN_PASSWORD_LENGTH));
            } catch (GlobalPropertyReadException e) {
                minPasswordLength = WebConfigDefault.SEC_MIN_PASSWORD_LENGTH;
                log.warn("Couldn't load " + WebConfigKey.SEC_MIN_PASSWORD_LENGTH + " , use " + minPasswordLength + " for default.");
            }
        }
        return minPasswordLength; 
    }

    public static String getSignUpSuccessPage() {
        // TODO Auto-generated method stub
        return "signupSuccess";
    }

    public static String getSignUpFailPage() {
        // TODO Auto-generated method stub
        return "signupFail";
    }

    public static String getUsernameFormat() {
        // TODO Auto-generated method stub
        return "^[A-Za-z0-9][A-Za-z0-9_\\-\\.]+[A-Za-z0-9]$";
    }

    public static String getEmailFormat() {
        // TODO Auto-generated method stub
        return "^[A-Za-z0-9_\\-\\.]+@[a-zA-Z0-9_\\-]+(\\.[a-zA-Z0-9_\\-]+)+$";
    }
    
    public static String getPasswordFormat() {
        // TODO Auto-generated method stub
        return "^[\\u0021-\\u007e]+$";
    }
}
