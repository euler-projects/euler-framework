package net.eulerform.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.stereotype.Controller;

@Configuration
@ImportResource({ "classpath*:beans.xml", "classpath:spring-security.xml" })
// @org.springframework.context.annotation.Import({SecurityConfiguration.class})
@ComponentScan(basePackages = "net.eulerform.web", excludeFilters = @ComponentScan.Filter(Controller.class))

public class RootContextConfiguration {

}
