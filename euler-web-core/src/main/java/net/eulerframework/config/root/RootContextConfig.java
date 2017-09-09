package net.eulerframework.config.root;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.stereotype.Controller;

import net.eulerframework.web.core.annotation.ApiEndpoint;
import net.eulerframework.web.core.annotation.WebController;

@SuppressWarnings("unused")
@Configuration
@ImportResource({"classpath*:config/beans.xml"})
@ComponentScan(
        basePackages = {"net.eulerframework.web",
                        "com.eulerframework.web",
                        "net.eulerframework.bean",
                        "net.eulerframework.config.root"},
        excludeFilters = {@ComponentScan.Filter(Controller.class),
                          @ComponentScan.Filter(WebController.class),
                          @ComponentScan.Filter(ApiEndpoint.class)}
//                          @ComponentScan.Filter(
//                                  type = FilterType.ASSIGNABLE_TYPE,
//                                  classes={
//                                          AdminAjaxServletContextConfig.class,
//                                          AjaxServletContextConfig.class,
//                                          WebServletContextConfig.class,
//                                          AdminWebServletContextConfig.class,
//                                          ApiServletContextConfig.class,
//                                          })}
)
public class RootContextConfig {
    
}
