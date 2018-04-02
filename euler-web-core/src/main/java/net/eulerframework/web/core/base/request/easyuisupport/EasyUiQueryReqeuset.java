package net.eulerframework.web.core.base.request.easyuisupport;

import javax.servlet.http.HttpServletRequest;

import net.eulerframework.web.core.base.request.PageQueryRequest;

public class EasyUiQueryReqeuset extends PageQueryRequest {

    public EasyUiQueryReqeuset(HttpServletRequest request) {
        super(request, "page", "rows");
    }

}
