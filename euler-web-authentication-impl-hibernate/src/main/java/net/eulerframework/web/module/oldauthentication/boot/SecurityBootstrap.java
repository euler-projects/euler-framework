package net.eulerframework.web.module.oldauthentication.boot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.annotation.Order;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.module.oldauthentication.conf.SecurityConfiguration;

@Order(-1)
public class SecurityBootstrap extends AbstractSecurityWebApplicationInitializer
{
    private final Logger logger = LogManager.getLogger();
    
    public SecurityBootstrap() throws ClassNotFoundException {
        super(Class.forName(WebConfig.getRootContextConfigClassName()), SecurityConfiguration.class);
    }

    @Override
    protected boolean enableHttpSessionEventPublisher()
    {
        this.logger.info("Executing security bootstrap.");

        return true;
    }
}
