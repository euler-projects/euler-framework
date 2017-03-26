package net.eulerframework.web.core.base.response.easyuisupport;

import net.eulerframework.common.base.log.LogSupport;
import net.eulerframework.web.core.base.response.BaseResponse;

public class EasyUIAjaxResponse<T> extends LogSupport implements BaseResponse {
    
    public final static EasyUIAjaxResponse<String> SUCCESS_RESPONSE = new EasyUIAjaxResponse<>("SUCCESS");

    private T data;

    public EasyUIAjaxResponse(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }
}
