package net.eulerform.config.boot;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import net.eulerform.common.FilePathTool;
import net.eulerform.common.GlobalProperties;
import net.eulerform.common.GlobalPropertyReadException;
import net.eulerform.web.core.base.exception.WebInitException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

@Order(0)
public class FrameworkBootstrap implements WebApplicationInitializer {
    private final Logger log = LogManager.getLogger();
    
    private static final String WEB_AUTHENTICATION_TYPE = "web.authenticationType";
    private static final String WEB_SECURITY_LOCAL_ENABLED = "local";
    private static final String WEB_SECURITY_LOCAL = "web-security-local";
    private static final String WEB_SECURITY_LDAP_ENABLED = "ldap";
    private static final String WEB_SECURITY_LDAP = "web-security-ldap";
    private static final String WEB_SECURITY_CAS_ENABLED = "cas";
    private static final String WEB_SECURITY_CAS = "web-security-cas";
    private static final String WEB_SECURITY_NONE_ENABLED = "none";
    private static final String WEB_SECURITY_NONE = "web-security-none";
    
    private static final String REST_ROOT_URL = "rest.rooturl";

    private static final String REST_AUTHENTICATION_TYPE = "rest.authenticationType";
    private static final String REST_SECURITY_OAUTH_ENABLED = "oauth";
    private static final String REST_SECURITY_OAUTH = "rest-security-oauth";
    private static final String REST_SECURITY_BASIC_ENABLED = "basic";
    private static final String REST_SECURITY_BASIC = "rest-security-basic";
    private static final String REST_SECURITY_NONE_ENABLED = "none";
    private static final String REST_SECURITY_NONE = "rest-security-none";    

    private static final String OAUTH_SERVER_TYPE = "oauth.severType";
    private static final String OAUTH_AUTHORIZATION_SERVER_ENABLED = "oauth-authorization-server";
    private static final String OAUTH_AUTHORIZATION_SERVER = "oauth-authorization-server";
    private static final String OAUTH_RESOURCE_SERVER_ENABLED = "oauth-resource-server";
    private static final String OAUTH_RESOURCE_SERVER = "oauth-resource-server";
    private static final String OAUTH_SERVER_BOTH_ENABLED = "both";
    private static final String OAUTH_SERVER_NEITHER_ENABLED = "neither";

    private static final String MULITPART_LOCATION = "multiPart.location";
    private static final String MULITPART_MAX_FILE_SIZE = "multiPart.maxFileSize";
    private static final String MULITPART_MAX_REQUEST_SIZE = "multiPart.maxRequestSize";
    private static final String MULITPART_FILE_SIZE_THRESHOLD = "multiPart.fileSizeThreshold";
    
    @Override
    public void onStartup(ServletContext container) throws ServletException {
        log.info("Executing framework bootstrap.");
        
        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.register(net.eulerform.config.RootContextConfiguration.class);
        
        String webAuthentication = GlobalProperties.get(WEB_AUTHENTICATION_TYPE);
        String restAuthentication = GlobalProperties.get(REST_AUTHENTICATION_TYPE);
        String oauthSeverType = GlobalProperties.get(OAUTH_SERVER_TYPE);
        
        ConfigurableEnvironment configurableEnvironment = rootContext.getEnvironment();
        
        switch(webAuthentication){
        case WEB_SECURITY_LOCAL_ENABLED:
            configurableEnvironment.addActiveProfile(WEB_SECURITY_LOCAL);break;
        case WEB_SECURITY_LDAP_ENABLED:
            configurableEnvironment.addActiveProfile(WEB_SECURITY_LDAP);break;
        case WEB_SECURITY_CAS_ENABLED:
            configurableEnvironment.addActiveProfile(WEB_SECURITY_CAS);break;
        case WEB_SECURITY_NONE_ENABLED:
            configurableEnvironment.addActiveProfile(WEB_SECURITY_NONE);break;
        default: 
            rootContext.close();
            throw new WebInitException("不支持的WEB验证方式: "+restAuthentication);   
        }
        
        switch(restAuthentication){
        case REST_SECURITY_OAUTH_ENABLED:
            configurableEnvironment.addActiveProfile(REST_SECURITY_OAUTH);break;
        case REST_SECURITY_BASIC_ENABLED:
            configurableEnvironment.addActiveProfile(REST_SECURITY_BASIC);break;
        case REST_SECURITY_NONE_ENABLED:
            configurableEnvironment.addActiveProfile(REST_SECURITY_NONE);break;
        default: 
            rootContext.close();
            throw new WebInitException("不支持的REST验证方式: "+restAuthentication);   
        }
        
        switch(oauthSeverType){
        case OAUTH_AUTHORIZATION_SERVER_ENABLED:
            configurableEnvironment.addActiveProfile(OAUTH_AUTHORIZATION_SERVER);break;
        case OAUTH_RESOURCE_SERVER_ENABLED:
            configurableEnvironment.addActiveProfile(OAUTH_RESOURCE_SERVER);break;
        case OAUTH_SERVER_BOTH_ENABLED:
            configurableEnvironment.addActiveProfile(OAUTH_AUTHORIZATION_SERVER);
            configurableEnvironment.addActiveProfile(OAUTH_RESOURCE_SERVER);
            break;
        case OAUTH_SERVER_NEITHER_ENABLED:
            break;
        default: 
            rootContext.close();
            throw new WebInitException("不支持的OAUTH SERVER TYPE: "+oauthSeverType);   
        }
        
        container.addListener(new ContextLoaderListener(rootContext));
        
        String location = null;
        long maxFileSize = -1L;
        long maxRequestSize = -1L;
        int fileSizeThreshold = 0;
        
        try {
            location = GlobalProperties.get(MULITPART_LOCATION);
        } catch (GlobalPropertyReadException e) {
            // DO NOTHING
            log.info("Couldn't load "+MULITPART_LOCATION+" , use null for default.");
        }
        try {
            maxFileSize = Long.parseLong(GlobalProperties.get(MULITPART_MAX_FILE_SIZE));
        } catch (GlobalPropertyReadException e) {
            // DO NOTHING
            log.info("Couldn't load "+MULITPART_MAX_FILE_SIZE+" , use -1L for default.");
        }
        try {
            maxRequestSize = Long.parseLong(GlobalProperties.get(MULITPART_MAX_REQUEST_SIZE));
        } catch (GlobalPropertyReadException e) {
            // DO NOTHING
            log.info("Couldn't load "+MULITPART_MAX_REQUEST_SIZE+" , use -1L for default.");
        }
        try {
            fileSizeThreshold = Integer.parseInt(GlobalProperties.get(MULITPART_FILE_SIZE_THRESHOLD));
        } catch (GlobalPropertyReadException e) {
            // DO NOTHING
            log.info("Couldn't load "+MULITPART_FILE_SIZE_THRESHOLD+" , use 0 for default.");
        }
        
        AnnotationConfigWebApplicationContext springWebDispatcherServletContext = new AnnotationConfigWebApplicationContext();
        springWebDispatcherServletContext.register(net.eulerform.config.SpringWebDispatcherServletContextConfiguration.class);
        DispatcherServlet springWebDispatcherServlet = new DispatcherServlet(springWebDispatcherServletContext);
        ServletRegistration.Dynamic springWebDispatcher = container.addServlet("springWebDispatcherServlet", springWebDispatcherServlet);
        springWebDispatcher.setLoadOnStartup(1);
        springWebDispatcher.setMultipartConfig(new MultipartConfigElement(location, maxFileSize, maxRequestSize, fileSizeThreshold));
        springWebDispatcher.addMapping("/");

        String restRootUrl = GlobalProperties.get(REST_ROOT_URL);
        while(restRootUrl.endsWith("*")){
            restRootUrl = restRootUrl.substring(0, restRootUrl.length()-1);
        }
        restRootUrl = FilePathTool.changeToUnixFormat(restRootUrl);
        
        AnnotationConfigWebApplicationContext springRestDispatcherServletContext = new AnnotationConfigWebApplicationContext();
        springRestDispatcherServletContext.register(net.eulerform.config.SpringRestDispatcherServletContextConfiguration.class);
        DispatcherServlet springRestDispatcherServlet = new DispatcherServlet(springRestDispatcherServletContext);
        springRestDispatcherServlet.setDispatchOptionsRequest(true);
        ServletRegistration.Dynamic springRestDispatcher = container.addServlet("springRestDispatcherServlet", springRestDispatcherServlet);
        springRestDispatcher.setLoadOnStartup(2);
        springRestDispatcher.setMultipartConfig(new MultipartConfigElement(location, maxFileSize, maxRequestSize, fileSizeThreshold));        
        springRestDispatcher.addMapping(restRootUrl+"/*");
    }
}
