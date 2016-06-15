package net.eulerform.web.core.i18n;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.RequestContext;

public class Tag {

    public static String i18n(String msgKey) {
        HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();  

        RequestContext requestContext = new RequestContext(request);
        String message = requestContext.getMessage(msgKey);
        return message;
    }
}
