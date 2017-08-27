package net.eulerframework.web.core.i18n;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.support.RequestContext;

import net.eulerframework.web.util.ServletUtils;

public class Tag {

    public static String i18n(String msgKey) {
        if(msgKey == null)
            return null;
        
        HttpServletRequest request = ServletUtils.getRequest();  

        RequestContext requestContext = new RequestContext(request);
        String message = requestContext.getMessage(msgKey);
        return message;
    }
}
