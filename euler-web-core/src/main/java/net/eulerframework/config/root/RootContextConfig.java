package net.eulerframework.config.root;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.stereotype.Controller;

import net.eulerframework.web.core.annotation.ApiEndpoint;
import net.eulerframework.web.core.annotation.WebController;

@Configuration
@ImportResource({"classpath*:config/beans.xml"})
@ComponentScan(
        basePackages = {"net.eulerframework.web",
                        "com.eulerframework.web"},
        excludeFilters = {@ComponentScan.Filter(Controller.class),
                          @ComponentScan.Filter(WebController.class),
                          @ComponentScan.Filter(ApiEndpoint.class)}
)
public class RootContextConfig {
    
}
