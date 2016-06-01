package net.eulerform.config;

import java.nio.charset.StandardCharsets;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

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
        ReloadableResourceBundleMessageSource messageSource =
                new ReloadableResourceBundleMessageSource();
        messageSource.setCacheSeconds(-1);
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        messageSource.setBasenames(
                "/WEB-INF/i18n/titles", "/WEB-INF/i18n/messages",
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
}
