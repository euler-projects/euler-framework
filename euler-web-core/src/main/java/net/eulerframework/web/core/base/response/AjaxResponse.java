package net.eulerframework.web.core.base.response;

import java.util.List;

public class AjaxResponse<T> extends WebServiceResponse<T> {
    public AjaxResponse(T data) {
        super(data);
    }
    public AjaxResponse(List<T> dataList) {
        super(dataList);
    }
}
