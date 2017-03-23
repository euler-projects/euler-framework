package net.eulerframework.web.core.base.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;

import net.eulerframework.web.core.base.response.ErrorResponse;
import net.eulerframework.web.core.exception.web.AjaxException;
import net.eulerframework.web.core.exception.web.DefaultAjaxException;
import net.eulerframework.web.core.exception.web.ViewException;

@ResponseBody
public abstract class AjaxSupportWebController extends AbstractWebController {
    /**
     * 用于在程序发生{@link AccessDeniedException}异常时统一返回错误信息
     * 
     * @return 包含错误信息的Ajax响应体
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler({ AccessDeniedException.class })
    public ErrorResponse accessDeniedException(AccessDeniedException e) {
        return new ErrorResponse(new DefaultAjaxException(e.getMessage(), e));
    }
    
    /**
     * 用于在程序发生{@link MissingServletRequestParameterException}异常时统一返回错误信息
     * 
     * @return 包含错误信息的Ajax响应体
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ MissingServletRequestParameterException.class })
    public ErrorResponse missingServletRequestParameterException(MissingServletRequestParameterException e) {
        return new ErrorResponse(new DefaultAjaxException(e.getMessage(), e));
    }
    
    /**
     * 用于在程序发生{@link IllegalArgumentException}异常时统一返回错误信息
     * 
     * @return 包含错误信息的Ajax响应体
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ IllegalArgumentException.class })
    public ErrorResponse illegalArgumentException(IllegalArgumentException e) {
        return new ErrorResponse(new DefaultAjaxException(e.getMessage(), e));
    }
    
    /**
     * 用于在程序发生{@link AjaxException}异常时统一返回错误信息
     * 
     * @return 包含错误信息的Ajax响应体
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ AjaxException.class })
    public ErrorResponse ajaxException(AjaxException e) {
        return new ErrorResponse(e);
    }
    
    /**
     * 用于在程序发生{@link ViewException}异常时统一返回错误信息
     * 
     * @return 包含错误信息的Ajax响应体
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ ViewException.class })
    public ErrorResponse viewException(ViewException e) {
        return new ErrorResponse(new DefaultAjaxException(e.getMessage(), e));
    }
    
    /**
     * 用于在程序发生{@link Exception}异常时统一返回错误信息
     * 
     * @return 包含错误信息的Ajax响应体
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({ Exception.class })
    public ErrorResponse exception(Exception e) {
        this.logger.error(e.getMessage(), e);
        return new ErrorResponse(new DefaultAjaxException("_UNKNOWN_ERROR", e));
    }
}
