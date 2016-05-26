package net.eulerform.web.core.base.controller;

import net.eulerform.web.core.base.entity.WebServiceResponse;
import net.eulerform.web.core.base.entity.WebServiceResponseStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

public abstract class BaseRest {
    
    protected final Logger logger = LogManager.getLogger(this.getClass());
    /**  
     * 用于在程序发生异常时统一返回错误信息 
     * @return  
     */  
    @ResponseBody
    @ExceptionHandler({Exception.class})   
    public WebServiceResponse<String> exception(Exception e) {
        e.printStackTrace();
        return new WebServiceResponse<>(WebServiceResponseStatus.UNKNOWN_ERR);
    }
}
