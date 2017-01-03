package net.eulerframework.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.stereotype.Controller;

import net.eulerframework.web.core.annotation.AdminWebController;
import net.eulerframework.web.core.annotation.ApiController;
import net.eulerframework.web.core.annotation.WebController;

@Configuration
@ImportResource({"classpath*:config/beans.xml"})
@ComponentScan(
        basePackages = {"net.eulerframework.web",
                        "com.eulerframework.web"},
        excludeFilters = {@ComponentScan.Filter(Controller.class),
                          @ComponentScan.Filter(WebController.class),
                          @ComponentScan.Filter(AdminWebController.class),
                          @ComponentScan.Filter(ApiController.class)}
)

public class RootContextConfiguration {
    
}
