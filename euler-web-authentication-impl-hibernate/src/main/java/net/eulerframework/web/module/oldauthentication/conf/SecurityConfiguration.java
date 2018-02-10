package net.eulerframework.web.module.oldauthentication.conf;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
@ImportResource({"classpath:config/beans-security-exclude-conf.xml", "classpath:config/beans-security-web-conf.xml"})
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    
}
