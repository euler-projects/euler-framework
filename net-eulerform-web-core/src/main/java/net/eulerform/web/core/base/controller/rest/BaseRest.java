package net.eulerform.web.core.base.controller.rest;

import net.eulerform.web.core.base.entity.RestResponseEntity;
import net.eulerform.web.core.base.entity.RestResponseStatus;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

public abstract class BaseRest {
    /**  
     * 用于在程序发生异常时统一返回错误信息 
     * @return  
     */  
    @ResponseBody
    @ExceptionHandler({Exception.class})   
    public RestResponseEntity<String> exception(Exception e) {
        e.printStackTrace();
        return new RestResponseEntity<>(RestResponseStatus.UNKNOWN_ERR);
    }
}
