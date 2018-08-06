package net.eulerframework.config.root;

import javax.annotation.Resource;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

import net.eulerframework.web.core.annotation.ApiEndpoint;
import net.eulerframework.web.core.annotation.JspController;

@Configuration
@ImportResource({"classpath*:config/beans.xml"})
@ComponentScan(
        basePackages = {"net.eulerframework.**.web"},
        excludeFilters = {@ComponentScan.Filter(Controller.class),
                          @ComponentScan.Filter(JspController.class),
                          @ComponentScan.Filter(ApiEndpoint.class)}
)
@EnableTransactionManagement
public class RootContextConfig implements TransactionManagementConfigurer {
    
    @Resource(name="transactionManager")
    PlatformTransactionManager transactionManager;
    
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return this.transactionManager;
    }
}
