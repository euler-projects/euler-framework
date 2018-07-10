package net.eulerframework.web.module.authentication.boot;

import javax.servlet.ServletContext;

import org.springframework.core.annotation.Order;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

import net.eulerframework.boot.EulerFrameworkBootstrap;
import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.module.authentication.conf.SecurityConfiguration;

@Order(-1)
public class SecurityBootstrap extends AbstractSecurityWebApplicationInitializer {

    public SecurityBootstrap() throws ClassNotFoundException {
        super(Class.forName(WebConfig.getRootContextConfigClassName()), SecurityConfiguration.class);
    }

    @Override
    protected boolean enableHttpSessionEventPublisher() {
        return true;
    }

    @Override
    protected void afterSpringSecurityFilterChain(ServletContext servletContext) {
        servletContext.setAttribute(EulerFrameworkBootstrap.EULER_SPRING_SECURITY_ENABLED, true);
    }
}
