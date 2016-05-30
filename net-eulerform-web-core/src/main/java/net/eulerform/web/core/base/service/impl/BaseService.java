package net.eulerform.web.core.base.service.impl;

import javax.servlet.ServletContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import net.eulerform.web.core.base.service.IBaseService;

public abstract class BaseService implements IBaseService {
    
    protected final Logger logger = LogManager.getLogger(this.getClass());
    
    protected ServletContext getServletContext(){
        WebApplicationContext webApplicationContext = ContextLoader.getCurrentWebApplicationContext();  
        return webApplicationContext.getServletContext();
    }

}
