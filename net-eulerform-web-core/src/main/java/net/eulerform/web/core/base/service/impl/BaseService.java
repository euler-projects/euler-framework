package net.eulerform.web.core.base.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.eulerform.web.core.base.service.IBaseService;

public abstract class BaseService implements IBaseService {
    
    protected final Logger logger = LogManager.getLogger(this.getClass());

}
