package net.eulerframework.web.core.base.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.core.JsonProcessingException;

import net.eulerframework.web.core.base.response.HttpStatusResponse;
import net.eulerframework.web.core.base.response.Status;
import net.eulerframework.web.core.exception.api.BadRequestException;
import net.eulerframework.web.core.exception.api.ResourceExistsException;
import net.eulerframework.web.core.exception.api.ResourceNotFoundException;

public abstract class AbstractApiEndpoint extends BaseController {/**  
     * 用于在程序发生{@link BadRequestException}异常时统一返回错误信息 
     * @return  
     */  
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({BadRequestException.class})   
    public Object badRequestException(BadRequestException e) {
        this.logger.error(e.getMessage(), e);
        return new HttpStatusResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }
    
    /**  
     * 用于在程序发生{@link ResourceExistsException}异常时统一返回错误信息 
     * @return  
     */  
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ResourceExistsException.class})   
    public Object exception(ResourceExistsException e) {
        this.logger.error(e.getMessage(), e);
        return new HttpStatusResponse(Status.RESOURCE_EXIST, e.getMessage());
    }
    
    /**  
     * 用于在程序发生{@link IllegalArgumentException}异常时统一返回错误信息 
     * @return  
     */  
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({IllegalArgumentException.class})   
    public Object illegalArgumentException(IllegalArgumentException e) {
        this.logger.error(e.getMessage(), e);
        return new HttpStatusResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }
    
    /**  
     * 用于在程序发生{@link ResourceNotFoundException}异常时统一返回错误信息
     * @return  
     */  
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({ResourceNotFoundException.class})   
    public Object resourceNotFoundException(ResourceNotFoundException e) {
        this.logger.error(e.getMessage(), e);
        return new HttpStatusResponse(HttpStatus.NOT_FOUND);
    }
    
    /**  
     * 用于在程序发生{@link AccessDeniedException}异常时统一返回错误信息 
     * @return  
     */  
    @ResponseBody
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler({AccessDeniedException.class})   
    public Object accessDeniedException(AccessDeniedException e) {
        this.logger.error(e.getMessage(), e);
        return new HttpStatusResponse(HttpStatus.FORBIDDEN);
    }
    
    /**  
     * 用于在程序发生{@link BindException}异常时统一返回错误信息 
     * @return  
     */  
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({BindException.class})   
    public Object bindException(BindException e) {
        this.logger.error(e.getMessage(), e);
//        this.logger.error(e.getMessage(), e);
//        List<ObjectError> errors = e.getAllErrors();
//        List<String> errMsg = new ArrayList<>();
//        for(ObjectError err : errors){
//            if(FieldError.class.isAssignableFrom(err.getClass()))
//                errMsg.add(((FieldError)err).getField()+ ": " + err.getDefaultMessage());
//            else
//                errMsg.add(err.getDefaultMessage());
//        }
        try {
            return new HttpStatusResponse(Status.FIELD_VALID_FAILED, this.getObjectMapper().writeValueAsString(e.getAllErrors()));
        } catch (JsonProcessingException e1) {
            return new HttpStatusResponse(Status.FIELD_VALID_FAILED, e.getMessage());
        }
    }
    
    /**  
     * 用于在程序发生{@link MissingServletRequestParameterException}异常时统一返回错误信息 
     * @return  
     */  
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MissingServletRequestParameterException.class})   
    public Object missingServletRequestParameterException(MissingServletRequestParameterException e) {
        this.logger.error(e.getMessage(), e);
        return  new HttpStatusResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }
    
    /**  
     * 用于在程序发生{@link Exception}异常时统一返回错误信息 
     * @return  
     */  
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({Exception.class})   
    public Object exception(Exception e) {
        this.logger.error(e.getMessage(), e);
        return new HttpStatusResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

}
