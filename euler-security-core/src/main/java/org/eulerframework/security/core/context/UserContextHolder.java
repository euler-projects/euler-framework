package org.eulerframework.security.core.context;

import org.eulerframework.common.util.Assert;
import org.eulerframework.context.ApplicationContextHolder;
import org.springframework.context.ApplicationContext;

public class UserContextHolder {
    private static UserContext userContext;

    public static UserContext getUserContext() {
        if (userContext != null) {
            return userContext;
        }

        synchronized (UserContextHolder.class) {
            if (userContext != null) {
                return userContext;
            }

            return new DeferredUserContext(UserContextHolder::getUserContextBean);
        }
    }

    private static UserContext getUserContextBean() {
        ApplicationContext applicationContext = ApplicationContextHolder.getApplicationContext();
        Assert.notNull(applicationContext, "ApplicationContextHolder not initialized");
        userContext = applicationContext.getBean(UserContext.class);
        Assert.notNull(userContext, "No UserContext was defined");
        return userContext;
    }
}
