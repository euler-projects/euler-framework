package net.eulerform.web.core.base.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import net.eulerform.web.core.base.exception.NotFoundException;

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
    
}
