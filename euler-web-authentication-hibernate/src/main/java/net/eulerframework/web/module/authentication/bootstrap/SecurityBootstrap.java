package net.eulerframework.web.module.authentication.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

@Order(200)
public class SecurityBootstrap extends AbstractSecurityWebApplicationInitializer {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected boolean enableHttpSessionEventPublisher()
    {
        this.logger.info("Executing security bootstrap.");

        return true;
    }
}
