package net.eulerframework.web.core.base.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.base.response.ErrorAjaxResponse;
import net.eulerframework.web.core.exception.web.AjaxException;
import net.eulerframework.web.core.exception.web.DefaultAjaxException;

@ResponseBody
public abstract class AjaxSupportWebController extends AbstractWebController {

    /**
     * 用于在程序发生{@link AjaxException}异常时统一返回错误信息
     * 
     * @return 包含错误信息的Ajax响应体
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ AjaxException.class })
    public ErrorAjaxResponse ajaxException(AjaxException e) {
        if (WebConfig.isDebugMode()) {
            this.logger.error("Error Code: " + e.getCode() + "message: " + e.getMessage(), e);
        }
        return new ErrorAjaxResponse(e);
    }
    
    /**
     * 用于在程序发生{@link Exception}异常时统一返回错误信息
     * 
     * @return 包含错误信息的Ajax响应体
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({ Exception.class })
    public ErrorAjaxResponse exception(Exception e) {
        this.logger.error(e.getMessage(), e);
        return new ErrorAjaxResponse(new DefaultAjaxException(e));
    }
}
