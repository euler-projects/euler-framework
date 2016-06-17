package net.eulerform.config;

import net.eulerform.common.FilePathTool;
import net.eulerform.common.GlobalProperties;
import net.eulerform.common.GlobalPropertyReadException;
import net.eulerform.web.core.annotation.WebController;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "net.eulerform.web.**.controller", useDefaultFilters = false, includeFilters = @ComponentScan.Filter(WebController.class))
public class SpringWebDispatcherServletContextConfiguration {

    private final Logger log = LogManager.getLogger();

    @Bean
    public ViewResolver viewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setViewClass(org.springframework.web.servlet.view.JstlView.class);

        String jspPath = "/WEB-INF/modulePages";
        try {
            jspPath = FilePathTool.changeToUnixFormat(GlobalProperties.get("web.jspPath"));
        } catch (GlobalPropertyReadException e) {
            log.info("Couldn't load web.jspPath , use '/WEB-INF/modulePages' for default.");
        }

        resolver.setPrefix(jspPath);
        resolver.setSuffix(".jsp");
        return resolver;
    }
}
