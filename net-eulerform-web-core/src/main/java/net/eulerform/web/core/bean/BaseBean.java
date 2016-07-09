package net.eulerform.web.core.bean;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.eulerform.web.core.i18n.ClassPathReloadableResourceBundleMessageSource;

@Configuration
public class BaseBean {
    
    @Value("${i18n.cacheSeconds}")
    private int i18nCacheSeconds;
    
    @Bean
    public MessageSource messageSource() {
        ClassPathReloadableResourceBundleMessageSource messageSource = new ClassPathReloadableResourceBundleMessageSource();
        messageSource.setCacheSeconds(this.i18nCacheSeconds);
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        messageSource.setUseCodeAsDefaultMessage(true);
        messageSource.setBasenames(
                "classpath*:i18n/messages", 
                "/WEB-INF/i18n/titles", 
                "/WEB-INF/i18n/messages",
                "/WEB-INF/i18n/errors", 
                "/WEB-INF/i18n/validation", 
                "classpath:org/springframework/security/messages", 
                "classpath:org/hibernate/validator/ValidationMessages");
        return messageSource;
    }
    
    @Bean
    public LocalValidatorFactoryBean localValidatorFactoryBean() throws ClassNotFoundException {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setProviderClass(Class.forName("org.hibernate.validator.HibernateValidator"));
        validator.setValidationMessageSource(this.messageSource());
        return validator;
    }
    
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() throws ClassNotFoundException {
        MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
        processor.setValidator(this.localValidatorFactoryBean());
        return processor;
    }

    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    @Bean
    public LocaleResolver localeResolver() {
        return new AcceptHeaderLocaleResolver();
    }

    @Bean(name = "objectMapper")
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
        return mapper;
    }

    // @Bean(name="jaxb2Marshaller")
    // public Jaxb2Marshaller jaxb2Marshaller() {
    // Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
    // jaxb2Marshaller.setPackagesToScan("net.eulerform.**.entity");
    // return jaxb2Marshaller;
    // }
}
