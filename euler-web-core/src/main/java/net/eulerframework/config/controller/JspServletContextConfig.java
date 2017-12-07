package net.eulerframework.config.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ImportResource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.annotation.JspController;

@Configuration
@EnableWebMvc
@ComponentScan(
        basePackages = { "**.web.**.controller" }, 
        useDefaultFilters = false, 
        includeFilters = @ComponentScan.Filter(JspController.class),
        excludeFilters = @ComponentScan.Filter(
                type=FilterType.ASPECTJ, 
                pattern={
                        "*..web..controller.admin..*",
                        "*..web..controller.api..*",
                        "*..web..controller.ajax..*"
                        })
)
@ImportResource({"classpath*:config/controller-security.xml"})
public class JspServletContextConfig extends WebMvcConfigurerAdapter {

    @Bean
    public ViewResolver viewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setViewClass(org.springframework.web.servlet.view.JstlView.class);

        resolver.setPrefix(WebConfig.getJspPath());
        resolver.setSuffix(".jsp");
        return resolver;
    }
    
    
//    @Resource
//    private SpringValidatorAdapter validator;
//    
//    @Override
//    public Validator getValidator() {
//        return this.validator;
//    }

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
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUseSuffixPatternMatch(false);
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        Map<String, MediaType> mediaTypes = new HashMap<>();
        
        mediaTypes.put("json", MediaType.APPLICATION_JSON_UTF8);

        configurer.favorPathExtension(false).favorParameter(false).ignoreAcceptHeader(false)
                .defaultContentType(MediaType.APPLICATION_JSON_UTF8).mediaTypes(mediaTypes);
    }
}
