package net.eulerframework.web.core.base.controller;

import java.util.Date;

import javax.servlet.http.HttpServletResponse;

public abstract class AbstractRestEndpoint extends BaseController {

    
    protected void setNoCacheHeader() {
        HttpServletResponse response = this.getResponse();
        response.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Date", new Date().getTime());
        response.setIntHeader("Expires", 0);
    }
}
