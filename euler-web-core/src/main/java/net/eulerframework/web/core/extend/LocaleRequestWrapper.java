package net.eulerframework.web.core.extend;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;

public class LocaleRequestWrapper extends HttpServletRequestWrapper {
    protected final Logger logger = LogManager.getLogger();
    
    private final static String LOCALE_PARAM_NAME = "_locale";
    private final static String LOCALE_SESSION_ATTR_NAME = "__EULER_LOCALE__";
    private Locale locale;

    public LocaleRequestWrapper(HttpServletRequest request) {
        super(request);
        
        try {
        HttpSession session = request.getSession();
        
        if(session != null) {          
            String localeParamValue = this.getRequest().getParameter(LOCALE_PARAM_NAME);
            
            if (StringUtils.hasText(localeParamValue)) {
                Locale locale;
                if(localeParamValue.indexOf('_') < 0) {          
                    locale = new Locale(localeParamValue);
                } else {
                    String[] localStr = localeParamValue.split("_");                
                    locale = new Locale(localStr[0], localStr[1]);                    
                }
                session.setAttribute(LOCALE_SESSION_ATTR_NAME, locale);
                this.locale = locale;
            } else {
                Object locale = request.getSession().getAttribute(LOCALE_SESSION_ATTR_NAME);
                if(locale != null) {
                    this.locale = (Locale) locale;
                } else {
                    this.locale = request.getLocale();                    
                }
            }           
        } else {
            this.locale = request.getLocale();
        }
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
            this.locale = request.getLocale();            
        }
        request.setAttribute("__LOCALE", this.getLocale().toString());
    }

    @Override
    public Locale getLocale() { 
        return locale;  
    } 
}
