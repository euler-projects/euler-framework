package net.eulerform.config.boot;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import net.eulerform.common.FilePathTool;
import net.eulerform.common.GlobalProperties;
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
    private static final Logger log = LogManager.getLogger();
    
    private static final String WEB_SECURITY_LOCAL_ENABLED = "local";
    private static final String WEB_SECURITY_LOCAL = "web-security-local";
    private static final String WEB_SECURITY_LDAP_ENABLED = "ldap";
    private static final String WEB_SECURITY_LDAP = "web-security-ldap";
    private static final String WEB_SECURITY_CAS_ENABLED = "cas";
    private static final String WEB_SECURITY_CAS = "web-security-cas";
    private static final String WEB_SECURITY_NONE_ENABLED = "none";
    private static final String WEB_SECURITY_NONE = "web-security-none";
    
    private static final String REST_SECURITY_OAUTH_ENABLED = "oauth";
    private static final String REST_SECURITY_OAUTH = "rest-security-oauth";
    private static final String REST_SECURITY_BASIC_ENABLED = "basic";
    private static final String REST_SECURITY_BASIC = "rest-security-basic";
    private static final String REST_SECURITY_NONE_ENABLED = "none";
    private static final String REST_SECURITY_NONE = "rest-security-none";    

    private static final String OAUTH_AUTHORIZATION_SERVER_ENABLED = "oauth-authorization-server";
    private static final String OAUTH_AUTHORIZATION_SERVER = "oauth-authorization-server";
    private static final String OAUTH_RESOURCE_SERVER_ENABLED = "oauth-resource-server";
    private static final String OAUTH_RESOURCE_SERVER = "oauth-resource-server";
    private static final String OAUTH_SERVER_BOTH_ENABLED = "both";
    private static final String OAUTH_SERVER_NONE_ENABLED = "none";
    
    @Override
    public void onStartup(ServletContext container) throws ServletException {
        log.info("Executing framework bootstrap.");
        
        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.register(net.eulerform.config.RootContextConfiguration.class);
        
        String webAuthentication = GlobalProperties.get("web.authenticationType");
        String restAuthentication = GlobalProperties.get("rest.authenticationType");
        String oauthSeverType = GlobalProperties.get("oauth.severType");
        
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
            if(REST_SECURITY_OAUTH_ENABLED.equals(restAuthentication))
                configurableEnvironment.addActiveProfile(OAUTH_RESOURCE_SERVER);
            break;
        case OAUTH_SERVER_BOTH_ENABLED:
            configurableEnvironment.addActiveProfile(OAUTH_AUTHORIZATION_SERVER);
            if(REST_SECURITY_OAUTH_ENABLED.equals(restAuthentication))
                configurableEnvironment.addActiveProfile(OAUTH_RESOURCE_SERVER);
            break;
        case OAUTH_SERVER_NONE_ENABLED:
            break;
        default: 
            rootContext.close();
            throw new WebInitException("不支持的OAUTH SERVER TYPE: "+restAuthentication);   
        }
        
        container.addListener(new ContextLoaderListener(rootContext));

        AnnotationConfigWebApplicationContext springWebDispatcherServletContext = new AnnotationConfigWebApplicationContext();
        springWebDispatcherServletContext.register(net.eulerform.config.SpringWebDispatcherServletContextConfiguration.class);
        DispatcherServlet springWebDispatcher = new DispatcherServlet(springWebDispatcherServletContext);
        ServletRegistration.Dynamic dispatcher = container.addServlet("springWebDispatcher", springWebDispatcher);
        dispatcher.setLoadOnStartup(1);
        dispatcher.setMultipartConfig(new MultipartConfigElement(null, 1024L*1024L*1024L, 1024L*1024L*1024L, 1024*1024*1024));
        dispatcher.addMapping("/");
        
        AnnotationConfigWebApplicationContext springRestDispatcherServletContext = new AnnotationConfigWebApplicationContext();
        springRestDispatcherServletContext.register(net.eulerform.config.SpringRestDispatcherServletContextConfiguration.class);
        DispatcherServlet springRestDispatcher = new DispatcherServlet(springRestDispatcherServletContext);
        springRestDispatcher.setDispatchOptionsRequest(true);
        dispatcher = container.addServlet(
                "springRestDispatcher", springRestDispatcher
        );
        dispatcher.setLoadOnStartup(2);
        
        String restRootUrl = GlobalProperties.get("rest.rooturl");
        while(restRootUrl.endsWith("*")){
            restRootUrl = restRootUrl.substring(0, restRootUrl.length()-1);
        }
        restRootUrl = FilePathTool.changeToUnixFormat(restRootUrl);
        dispatcher.addMapping(restRootUrl+"/*");
    }
}
