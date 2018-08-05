package net.eulerframework.web.module.authentication.conf;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
@ImportResource({
    "classpath:config/beans-security-exclude-conf.xml", // Spring Security 白名单
    "classpath:config/beans-security-web-conf.xml" // Spring Security WEB 验证方式配置
})
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    
}
