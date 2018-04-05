package net.eulerframework.web.core.base.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

import net.eulerframework.web.core.base.response.ErrorResponse;
import net.eulerframework.web.core.exception.web.PageNotFoundException;
import net.eulerframework.web.core.exception.web.SystemWebError;
import net.eulerframework.web.core.exception.web.WebException;

@ResponseBody
public abstract class AjaxSupportWebController extends AbstractWebController {

    /**
     * 用于在程序发生{@link AccessDeniedException}异常时统一返回错误信息
     * 
     * @return 包含错误信息的Ajax响应体
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public Object accessDeniedException(AccessDeniedException e) {
        return new ErrorResponse(new WebException(e.getMessage(), SystemWebError.ACCESS_DENIED, e));
    }

    /**
     * 用于在程序发生{@link MissingServletRequestParameterException}异常时统一返回错误信息
     * 
     * @return 包含错误信息的Ajax响应体
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Object missingServletRequestParameterException(MissingServletRequestParameterException e) {
        return new ErrorResponse(new WebException(e.getMessage(), SystemWebError.ILLEGAL_PARAMETER, e));
    }

    /**
     * 用于在程序发生{@link IllegalArgumentException}异常时统一返回错误信息
     * 
     * @return 包含错误信息的Ajax响应体
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public Object illegalArgumentException(IllegalArgumentException e) {
        return new ErrorResponse(new WebException(e.getMessage(), SystemWebError.ILLEGAL_ARGUMENT, e));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UnrecognizedPropertyException.class)
    public Object unrecognizedPropertyException(UnrecognizedPropertyException e) {
        return new ErrorResponse(new WebException("JSON parse error: Unrecognized field \"" + e.getPropertyName() + "\"", SystemWebError.ILLEGAL_PARAMETER, e));
    }
    
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Object httpMessageNotReadableException(HttpMessageNotReadableException e) {
        if(e.getCause().getClass().equals(UnrecognizedPropertyException.class)) {
            return this.unrecognizedPropertyException((UnrecognizedPropertyException) e.getCause());
        }
        
        return new ErrorResponse(new WebException(e.getMessage(), SystemWebError.ILLEGAL_PARAMETER, e));
    }

    /**
     * 用于在程序发生{@link PageNotFoundException}异常时统一返回错误信息
     * 
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(PageNotFoundException.class)
    public void pageNotFoundException(PageNotFoundException e) {
    }

    /**
     * 用于在程序发生{@link WebException}异常时统一返回错误信息
     * 
     * @return 包含错误信息的Ajax响应体
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(WebException.class)
    public Object webException(WebException e) {
        return new ErrorResponse(e);
    }

    /**
     * 用于在程序发生{@link Exception}异常时统一返回错误信息
     * 
     * @return 包含错误信息的Ajax响应体
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public Object exception(Exception e) {
        this.logger.error(e.getMessage(), e);
        return new ErrorResponse();
    }
}
