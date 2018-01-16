package net.eulerframework.config.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.eulerframework.web.core.annotation.AjaxWebController;

@Configuration
@EnableWebMvc
@ComponentScan(
        basePackages = {"**.web.**.controller"},
        useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(AjaxWebController.class)
)
@ImportResource({"classpath*:config/controller-security.xml"})
public class AjaxServletContextConfig extends WebMvcConfigurerAdapter {
    
    @Resource(name="objectMapper") ObjectMapper objectMapper;

    @Override
    public void configureMessageConverters(
            List<HttpMessageConverter<?>> converters
    ) {
        converters.add(new SourceHttpMessageConverter<>());

        MappingJackson2HttpMessageConverter jsonConverter =
                new MappingJackson2HttpMessageConverter();
        jsonConverter.setSupportedMediaTypes(Arrays.asList(
                MediaType.APPLICATION_JSON_UTF8,
                MediaType.valueOf("text/json;charset=UTF-8")
        ));
        jsonConverter.setObjectMapper(this.objectMapper);
        converters.add(jsonConverter);
        
    }

    @Override
    public void configureContentNegotiation(
            ContentNegotiationConfigurer configurer)
    {
        Map<String, MediaType> mediaTypes = new HashMap<>();;
        mediaTypes.put("json", MediaType.APPLICATION_JSON_UTF8);
        //mediaTypes.put("xml", MediaType.APPLICATION_XML);
        
        configurer.favorPathExtension(true).favorParameter(true)
                .ignoreAcceptHeader(false)
                .defaultContentType(MediaType.APPLICATION_JSON_UTF8)
                .mediaTypes(mediaTypes);
    }
}
