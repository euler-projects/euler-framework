package net.eulerframework.web.core.base.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import net.eulerframework.web.core.base.response.HttpStatusResponse;
import net.eulerframework.web.core.exception.NotFoundException;

public abstract class AbstractWebController extends BaseController {
    
//    @RequestMapping(value = { "{view}.html" }, method = RequestMethod.GET)
//    public String autoView(@PathVariable String view)
//    {
//        System.out.println(view);
//        return view;
//    }
    
    
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
