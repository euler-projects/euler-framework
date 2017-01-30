package net.eulerframework.web.module.authentication.bootstrap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.annotation.Order;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

@Order(200)
public class SecurityBootstrap extends AbstractSecurityWebApplicationInitializer
{
    private final Logger logger = LogManager.getLogger();

    @Override
    protected boolean enableHttpSessionEventPublisher()
    {
        this.logger.info("Executing security bootstrap.");

        return true;
    }
}
