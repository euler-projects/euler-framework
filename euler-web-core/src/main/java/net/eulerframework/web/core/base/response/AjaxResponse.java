package net.eulerframework.web.core.base.response;

import net.eulerframework.web.core.exception.AjaxException;
import net.eulerframework.web.core.i18n.Tag;

public class AjaxResponse<T> implements BaseResponse {

    private T responseData;
    private int errorCode;
    private String errorMsg;
    private String localizedErrorMsg;
    
    public AjaxResponse(T data) {
        this.responseData = data;
    }
    public AjaxResponse(AjaxException ajaxException) {
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
