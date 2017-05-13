package net.eulerframework.web.core.base.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import net.eulerframework.web.core.base.response.ErrorResponse;
import net.eulerframework.web.core.exception.web.DefaultWebException;
import net.eulerframework.web.core.exception.web.WebException;
import net.eulerframework.web.core.exception.web.WebException.WebError;
import net.eulerframework.web.core.exception.web.api.ResourceNotFoundException;

public abstract class AbstractApiEndpoint extends BaseController {

    /**
     * 用于在程序发生{@link BindException}异常时统一返回错误信息
     * 
     * @return
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public Object bindException(BindException e) {
        this.logger.error(e.getMessage(), e);
        return new ErrorResponse(new DefaultWebException(e.getMessage(), WebError.ILLEGAL_PARAMETER, e));
    }

    /**
     * 用于在程序发生{@link ResourceNotFoundException}异常时统一返回错误信息
     * 
     * @return
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResourceNotFoundException.class)
    public Object resourceNotFoundException(ResourceNotFoundException e) {
        this.logger.error(e.getMessage(), e);
        return new ErrorResponse();
    }

    /**
     * 用于在程序发生{@link AccessDeniedException}异常时统一返回错误信息
     * 
     * @return 包含错误信息的Ajax响应体
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public Object accessDeniedException(AccessDeniedException e) {
        return new ErrorResponse(new DefaultWebException(e.getMessage(), WebError.ACCESS_DENIED, e));
    }

    /**
     * 用于在程序发生{@link MissingServletRequestParameterException}异常时统一返回错误信息
     * 
     * @return 包含错误信息的Ajax响应体
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Object missingServletRequestParameterException(MissingServletRequestParameterException e) {
        return new ErrorResponse(new DefaultWebException(e.getMessage(), WebError.ILLEGAL_PARAMETER, e));
    }

    /**
     * 用于在程序发生{@link IllegalArgumentException}异常时统一返回错误信息
     * 
     * @return 包含错误信息的Ajax响应体
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public Object illegalArgumentException(IllegalArgumentException e) {
        return new ErrorResponse(new DefaultWebException(e.getMessage(), WebError.ILLEGAL_ARGUMENT, e));
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
