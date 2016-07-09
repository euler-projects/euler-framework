package net.eulerform.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import net.eulerform.web.core.annotation.WebController;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.eulerform.common.FilePathTool;
import net.eulerform.common.GlobalProperties;
import net.eulerform.common.GlobalPropertyReadException;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = { "net.eulerform.web.**.controller" }, 
               useDefaultFilters = false, 
               includeFilters = @ComponentScan.Filter(WebController.class))
public class SpringWebDispatcherServletContextConfiguration extends WebMvcConfigurerAdapter {

    private final Logger log = LogManager.getLogger();

    @Bean
    public ViewResolver viewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setViewClass(org.springframework.web.servlet.view.JstlView.class);

        String jspPath = "/WEB-INF/modulePages";
        try {
            jspPath = FilePathTool.changeToUnixFormat(GlobalProperties.get("web.jspPath"));
        } catch (GlobalPropertyReadException e) {
            log.warn("Couldn't load web.jspPath , use '/WEB-INF/modulePages' for default.");
        }

        resolver.setPrefix(jspPath);
        resolver.setSuffix(".jsp");
        return resolver;
    }
    
    @Resource
    private SpringValidatorAdapter validator;
    
    @Override
    public Validator getValidator() {
        return this.validator;
    }

    @Resource(name = "objectMapper")
    ObjectMapper objectMapper;

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new SourceHttpMessageConverter<>());

        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setSupportedMediaTypes(
                Arrays.asList(MediaType.APPLICATION_JSON_UTF8, MediaType.valueOf("text/json;charset=UTF-8")));
        jsonConverter.setObjectMapper(this.objectMapper);
        converters.add(jsonConverter);

    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        Map<String, MediaType> mediaTypes = new HashMap<>();
        ;
        mediaTypes.put("json", MediaType.APPLICATION_JSON_UTF8);

        configurer.favorPathExtension(false).favorParameter(false).ignoreAcceptHeader(false)
                .defaultContentType(MediaType.APPLICATION_JSON_UTF8).mediaTypes(mediaTypes);
    }
}
