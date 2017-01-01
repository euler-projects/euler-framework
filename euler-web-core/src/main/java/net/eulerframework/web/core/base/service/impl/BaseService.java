package net.eulerframework.web.core.base.service.impl;

import javax.servlet.ServletContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import net.eulerframework.web.core.base.service.IBaseService;

/**
 * 基本业务逻辑实现类，除非明确不需要权限控制，否则业务逻辑层实现不应继承此类<br>
 * 而应继承{@link BaseSecurityService}
 * @author cFrost
 *
 */
public abstract class BaseService implements IBaseService {
    
    protected final Logger logger = LogManager.getLogger(this.getClass());
    
    protected ServletContext getServletContext(){
        WebApplicationContext webApplicationContext = ContextLoader.getCurrentWebApplicationContext();  
        return webApplicationContext.getServletContext();
    }

}
