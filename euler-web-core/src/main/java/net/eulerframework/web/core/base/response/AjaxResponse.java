package net.eulerframework.web.core.base.response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.exception.web.AjaxException;
import net.eulerframework.web.core.i18n.Tag;

public class AjaxResponse<T> implements BaseResponse {
    
    protected final Logger logger = LogManager.getLogger(this.getClass());

    private T responseData;
    private int errorCode;
    private String errorMsg;
    private String localizedErrorMsg;
    
    public AjaxResponse() {
    }
    
    public AjaxResponse(T data) {
        this.responseData = data;
    }
    public AjaxResponse(AjaxException ajaxException) {
        
        if(WebConfig.isLogDetailsMode()) {
            this.logger.error("Error Code: " + ajaxException.getCode() + "message: " + ajaxException.getMessage(), ajaxException);
        }
        
        this.errorCode = ajaxException.getCode();
        this.errorMsg = ajaxException.getMsg();
        this.localizedErrorMsg = Tag.i18n(this.errorMsg);
    }
    public T getResponseData() {
        return responseData;
    }
    public int getErrorCode() {
        return errorCode;
    }
    public String getErrorMsg() {
        return errorMsg;
    }
    public String getLocalizedErrorMsg() {
        return localizedErrorMsg;
    }
}
