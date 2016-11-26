package net.eulerframework.web.core.base.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import net.eulerframework.web.core.base.exception.NotFoundException;
import net.eulerframework.web.core.base.response.HttpStatusResponse;

public abstract class AbstractWebController extends BaseController {
    
    /**  
     * 用于在程序发生{@link NotFoundException}异常时统一返回错误信息 
     * @return  
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({NotFoundException.class})   
    public String notFoundException(NotFoundException e) {
        return "/404";
    }
    
    /**  
     * 用于在程序发生{@link AccessDeniedException}异常时统一返回错误信息 
     * @return  
     */
    @ExceptionHandler({AccessDeniedException.class})   
    public HttpStatusResponse accessDeniedException(AccessDeniedException e) {
        e.printStackTrace();
        throw e;
    }
    
}
