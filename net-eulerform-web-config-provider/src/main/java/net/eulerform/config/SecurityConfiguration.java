package net.eulerform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
//@ImportResource("classpath*:springSecurity.xml")
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
}
