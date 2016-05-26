package net.eulerform.web.core.base.controller.rest;

import net.eulerform.web.core.base.entity.WebServiceResponse;
import net.eulerform.web.core.base.entity.WebServiceResponseStatus;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

public abstract class BaseRest {
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
