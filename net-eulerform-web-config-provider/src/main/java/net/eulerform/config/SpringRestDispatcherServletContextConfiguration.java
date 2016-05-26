package net.eulerform.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import net.eulerform.web.core.annotation.RestEndpoint;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableWebMvc
@ComponentScan(
        basePackages = "net.eulerform.web.**.controller",
        useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(RestEndpoint.class)
)
public class SpringRestDispatcherServletContextConfiguration extends WebMvcConfigurerAdapter {
    @Resource(name="objectMapper") ObjectMapper objectMapper;
    @Resource(name="jaxb2Marshaller") Marshaller marshaller;
    @Resource(name="jaxb2Marshaller") Unmarshaller unmarshaller;
    
    @Bean(name="objectMapper")
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
    
    @Bean(name="jaxb2Marshaller")
    public Jaxb2Marshaller jaxb2Marshaller() {
        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setPackagesToScan("net.eulerform.**.entity");
        return jaxb2Marshaller;
    }

    @Override
    public void configureMessageConverters(
            List<HttpMessageConverter<?>> converters
    ) {
        converters.add(new SourceHttpMessageConverter<>());

        MarshallingHttpMessageConverter xmlConverter =
                new MarshallingHttpMessageConverter();
        xmlConverter.setSupportedMediaTypes(Arrays.asList(
                MediaType.APPLICATION_XML,
                MediaType.TEXT_XML
        ));
        xmlConverter.setMarshaller(this.marshaller);
        xmlConverter.setUnmarshaller(this.unmarshaller);
        converters.add(xmlConverter);

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
        mediaTypes.put("xml", MediaType.APPLICATION_XML);
        
        configurer.favorPathExtension(true).favorParameter(true)
                .ignoreAcceptHeader(false)
                .defaultContentType(MediaType.APPLICATION_JSON_UTF8)
                .mediaTypes(mediaTypes);
    }

 /*   @Bean
    public LocaleResolver localeResolver()
    {
        return new AcceptHeaderLocaleResolver();
    }*/
}
