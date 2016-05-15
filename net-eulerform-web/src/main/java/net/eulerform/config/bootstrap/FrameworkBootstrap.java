package net.eulerform.config.bootstrap;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import net.eulerform.web.core.filter.PreLoggingFilter;
import net.eulerform.web.core.util.PropertyReader;

import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.DispatcherServlet;

@Order(1)
public class FrameworkBootstrap implements WebApplicationInitializer {
    
    @Override
    public void onStartup(ServletContext container) throws ServletException {
        
        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.register(net.eulerform.config.RootContextConfiguration.class);
        
        PropertyReader propertyReader;
        String authenticationProvider;
        try {
            propertyReader = new PropertyReader("config.properties");
            authenticationProvider = (String) propertyReader.getProperty("springSecurity.authenticationProvider");
        } catch (Exception e) {
            rootContext.close();
            throw new ServletException(e);
        }        
        
        if(!"disable".equalsIgnoreCase(authenticationProvider)) {
            ConfigurableEnvironment configurableEnvironment = rootContext.getEnvironment();
            configurableEnvironment.setActiveProfiles(authenticationProvider.toLowerCase());
        }
        
        container.addListener(new ContextLoaderListener(rootContext));

        AnnotationConfigWebApplicationContext springWebDispatcherServletContext = new AnnotationConfigWebApplicationContext();
        springWebDispatcherServletContext.register(net.eulerform.config.SpringWebDispatcherServletContextConfiguration.class);
        DispatcherServlet springWebDispatcher = new DispatcherServlet(springWebDispatcherServletContext);
        ServletRegistration.Dynamic dispatcher = container.addServlet("springWebDispatcher", springWebDispatcher);
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping("/");
        
        AnnotationConfigWebApplicationContext springRestDispatcherServletContext = new AnnotationConfigWebApplicationContext();
        springRestDispatcherServletContext.register(net.eulerform.config.SpringRestDispatcherServletContextConfiguration.class);
        DispatcherServlet springRestDispatcher = new DispatcherServlet(springRestDispatcherServletContext);
        springRestDispatcher.setDispatchOptionsRequest(true);
        dispatcher = container.addServlet(
                "springRestDispatcher", springRestDispatcher
        );
        dispatcher.setLoadOnStartup(2);
        dispatcher.addMapping("/webapi/*");

        FilterRegistration.Dynamic characterEncodingFilter = container.addFilter(
                "characterEncodingFilter", new CharacterEncodingFilter("UTF-8")
        );
        characterEncodingFilter.addMappingForUrlPatterns(null, false, "/*");
        
        FilterRegistration.Dynamic preLoggingFilter = container.addFilter(
                "preLoggingFilter", new PreLoggingFilter()
        );
        preLoggingFilter.addMappingForUrlPatterns(null, false, "/*");
    }
}
