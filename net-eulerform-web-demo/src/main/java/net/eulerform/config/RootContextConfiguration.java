package net.eulerform.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.eulerform.web.core.i18n.ClassPathReloadableResourceBundleMessageSource;

@Configuration
@ImportResource({"classpath*:beans.xml","classpath:spring-security.xml"})
//@org.springframework.context.annotation.Import({SecurityConfiguration.class})
@ComponentScan(
        basePackages = "net.eulerform.web",
        excludeFilters = @ComponentScan.Filter(Controller.class)
)

public class RootContextConfiguration {
    
    @Bean
    public MessageSource messageSource() {
        ClassPathReloadableResourceBundleMessageSource messageSource =
                new ClassPathReloadableResourceBundleMessageSource();
        messageSource.setCacheSeconds(1);
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        messageSource.setUseCodeAsDefaultMessage(true);
        messageSource.setBasenames(
                "classpath*:i18n/messages",
                "/WEB-INF/i18n/titles",
                "/WEB-INF/i18n/messages",
                "/WEB-INF/i18n/errors", "/WEB-INF/i18n/validation",
                "classpath:org/springframework/security/messages"
        );
        
        return messageSource;
    }
    
    @Bean
    public MultipartResolver multipartResolver()
    {
        return new StandardServletMultipartResolver();
    }
    
    @Bean(name="objectMapper")
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
        return mapper;
    }

    @Bean
    public LocaleResolver localeResolver()
    {
        return new AcceptHeaderLocaleResolver();
    }
    
//    @Bean(name="jaxb2Marshaller")
//    public Jaxb2Marshaller jaxb2Marshaller() {
//        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
//        jaxb2Marshaller.setPackagesToScan("com.sfa.framework.**.entity", "com.sfa.maoc.**.entity");
//        return jaxb2Marshaller;
//    }
    
}
