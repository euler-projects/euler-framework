package net.eulerframework.web.core.base;

import java.nio.charset.StandardCharsets;

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

import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.i18n.ClassPathReloadableResourceBundleMessageSource;

@Configuration
public class BaseBean {

    @Bean
    public MessageSource messageSource() {
        ClassPathReloadableResourceBundleMessageSource messageSource = new ClassPathReloadableResourceBundleMessageSource();
        messageSource.setCacheSeconds(WebConfig.getI18nRefreshFreq());
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        messageSource.setUseCodeAsDefaultMessage(true);
        messageSource.setBasename("classpath*:language/**/*");
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
    // jaxb2Marshaller.setPackagesToScan("net.eulerframework.**.entity",
    // "net.eulerframework.web.core.base.response");
    // return jaxb2Marshaller;
    // }
}
