package net.eulerform.web.core.base.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import net.eulerform.web.core.base.exception.IllegalParamException;
import net.eulerform.web.core.base.exception.ResourceExistException;
import net.eulerform.web.core.base.exception.ResourceNotFoundException;
import net.eulerform.web.core.base.response.HttpStatusResponse;
import net.eulerform.web.core.base.response.WebResponseStatus;

public abstract class BaseController {
    
    protected final Logger logger = LogManager.getLogger(this.getClass());
    
    protected ServletContext getServletContext(){
        WebApplicationContext webApplicationContext = ContextLoader.getCurrentWebApplicationContext();  
        return webApplicationContext.getServletContext();
    }
    
    protected void writeString(HttpServletResponse httpServletResponse, String str) throws IOException{
        httpServletResponse.getOutputStream().write(str.getBytes("UTF-8"));
    }
    
    /**  
     * 用于在程序发生{@link ResourceExistException}异常时统一返回错误信息 
     * @return  
     */  
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ResourceExistException.class})   
    public HttpStatusResponse exception(ResourceExistException e) {
        e.printStackTrace();
        return new HttpStatusResponse(WebResponseStatus.RESOURCE_EXIST.value(), e.getLocalizedMessage());
    }
    
    /**  
     * 用于在程序发生{@link IllegalParamException}异常时统一返回错误信息 
     * @return  
     */  
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({IllegalParamException.class})   
    public HttpStatusResponse illegalParamException(IllegalParamException e) {
        e.printStackTrace();
        return new HttpStatusResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }
    
    /**  
     * 用于在程序发生{@link ResourceNotFoundException}异常时统一返回错误信息 
     * @return  
     */  
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({ResourceNotFoundException.class})   
    public HttpStatusResponse resourceNotFoundException(ResourceNotFoundException e) {
        e.printStackTrace();
        return new HttpStatusResponse(HttpStatus.NOT_FOUND);
    }
    
    /**  
     * 用于在程序发生{@link AccessDeniedException}异常时统一返回错误信息 
     * @return  
     */  
    @ResponseBody
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler({AccessDeniedException.class})   
    public HttpStatusResponse accessDeniedException(AccessDeniedException e) {
        return new HttpStatusResponse(HttpStatus.FORBIDDEN);
    }
    
    /**  
     * 用于在程序发生{@link BindException}异常时统一返回错误信息 
     * @return  
     */  
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({BindException.class})   
    public HttpStatusResponse bindException(BindException e) {
        e.printStackTrace();
        List<ObjectError> errors = e.getAllErrors();
        List<String> errMsg = new ArrayList<>();
        for(ObjectError err : errors){
            if(FieldError.class.isAssignableFrom(err.getClass()))
                errMsg.add(((FieldError)err).getField()+ ": " + err.getDefaultMessage());
            else
                errMsg.add(err.getDefaultMessage());
        }
        return new HttpStatusResponse(WebResponseStatus.FIELD_VALID_FAILED.value(), errMsg.toString());
    }
    
    /**  
     * 用于在程序发生{@link MissingServletRequestParameterException}异常时统一返回错误信息 
     * @return  
     */  
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MissingServletRequestParameterException.class})   
    public HttpStatusResponse missingServletRequestParameterException(MissingServletRequestParameterException e) {
        e.printStackTrace();
        return  new HttpStatusResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }
    
    /**  
     * 用于在程序发生{@link Exception}异常时统一返回错误信息 
     * @return  
     */  
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({Exception.class})   
    public HttpStatusResponse exception(Exception e) {
        e.printStackTrace();
        return new HttpStatusResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getLocalizedMessage());
    }
}
