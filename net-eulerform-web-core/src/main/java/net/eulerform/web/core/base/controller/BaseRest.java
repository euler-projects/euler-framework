package net.eulerform.web.core.base.controller;

import net.eulerform.web.core.base.entity.WebServiceResponse;
import net.eulerform.web.core.base.exception.ResourceNotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

public abstract class BaseRest {
    
    protected final Logger logger = LogManager.getLogger(this.getClass());
    
    /**  
     * 用于在程序发生{@link ResourceNotFoundException}异常时统一返回错误信息 
     * @return  
     */  
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({ResourceNotFoundException.class})   
    public WebServiceResponse<String> resourceNotFoundException(ResourceNotFoundException e) {
        return new WebServiceResponse<>(HttpStatus.NOT_FOUND);
    }
    
	/**  
     * 用于在程序发生{@link AccessDeniedException}异常时统一返回错误信息 
     * @return  
     */  
    @ResponseBody
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler({AccessDeniedException.class})   
    public WebServiceResponse<String> accessDeniedException(AccessDeniedException e) {
        return new WebServiceResponse<>(HttpStatus.FORBIDDEN);
    }
    
    /**  
     * 用于在程序发生{@link Exception}异常时统一返回错误信息 
     * @return  
     */  
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({Exception.class})   
    public WebServiceResponse<String> exception(Exception e) {
        e.printStackTrace();
        return new WebServiceResponse<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
