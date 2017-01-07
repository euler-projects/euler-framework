package net.eulerframework.config;

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
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.eulerframework.web.core.annotation.ApiEndpoint;

@Configuration
@EnableWebMvc
@ComponentScan(
        basePackages = {"**.web.**.api"},
        useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(ApiEndpoint.class)
)
@ImportResource({"classpath*:config/controller-security.xml"})
public class SpringApiDispatcherServletContextConfiguration extends WebMvcConfigurerAdapter {
    
    @Resource(name="objectMapper") ObjectMapper objectMapper;
//    @Resource(name="jaxb2Marshaller") Marshaller marshaller;
//    @Resource(name="jaxb2Marshaller") Unmarshaller unmarshaller;

    @Override
    public void configureMessageConverters(
            List<HttpMessageConverter<?>> converters
    ) {
        converters.add(new SourceHttpMessageConverter<>());

//        MarshallingHttpMessageConverter xmlConverter =
//                new MarshallingHttpMessageConverter();
//        xmlConverter.setSupportedMediaTypes(Arrays.asList(
//                MediaType.APPLICATION_XML,
//                MediaType.TEXT_XML
//        ));
//        xmlConverter.setMarshaller(this.marshaller);
//        xmlConverter.setUnmarshaller(this.unmarshaller);
//        converters.add(xmlConverter);

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
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedMethods("PUT", "DELETE", "POST", "GET");
    }
}
