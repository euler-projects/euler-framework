package net.eulerframework.web.core.base.response;

import net.eulerframework.common.util.log.LogSupport;

public class AjaxResponse<T> extends LogSupport implements BaseResponse {

    private T responseData;

    public AjaxResponse(T data) {
        this.responseData = data;
    }

    public T getResponseData() {
        return responseData;
    }
}
