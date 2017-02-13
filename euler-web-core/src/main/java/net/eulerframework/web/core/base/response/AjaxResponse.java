package net.eulerframework.web.core.base.response;

import net.eulerframework.common.base.log.LogSupport;

public class AjaxResponse<T> extends LogSupport implements BaseResponse {
    
    public final static AjaxResponse<String> SUCCESS_RESPONSE = new AjaxResponse<>("SUCCESS");

    private T data;

    public AjaxResponse(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }
}
