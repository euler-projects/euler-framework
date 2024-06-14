package org.eulerframework.core.context;

import org.springframework.context.ApplicationContext;

public class ApplicationContextHolder {
    private static ApplicationContext applicationContext;

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void setApplicationContext(ApplicationContext applicationContext) {
        if(ApplicationContextHolder.applicationContext != null) {
            throw new IllegalStateException("ApplicationContext is already initialized");
        }

        synchronized (ApplicationContextHolder.class) {
            if(ApplicationContextHolder.applicationContext != null) {
                throw new IllegalStateException("ApplicationContext is already initialized");
            }

            ApplicationContextHolder.applicationContext = applicationContext;
        }
    }
}
