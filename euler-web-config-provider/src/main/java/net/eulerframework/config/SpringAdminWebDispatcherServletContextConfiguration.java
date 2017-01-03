package net.eulerframework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.annotation.AdminWebController;

@Configuration
@EnableWebMvc
@ComponentScan(
        basePackages = { "**.web.**.controller" }, 
        useDefaultFilters = false, 
        includeFilters = @ComponentScan.Filter(AdminWebController.class)
)
@ImportResource({"classpath*:config/controller-security.xml"})
public class SpringAdminWebDispatcherServletContextConfiguration
        extends SpringWebDispatcherServletContextConfiguration {
    
    @Bean
    @Override
    public ViewResolver viewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setViewClass(org.springframework.web.servlet.view.JstlView.class);

        resolver.setPrefix(WebConfig.getAdminJspPath());
        resolver.setSuffix(".jsp");
        return resolver;
    }
}
