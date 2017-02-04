package net.eulerframework.web.core.base.response;

import net.eulerframework.common.util.log.LogSupport;

public class AjaxResponse<T> extends LogSupport implements BaseResponse {

    private T data;

    public AjaxResponse(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }
}
