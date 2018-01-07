/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2013-2017 cFrost.sun(孙宾, SUN BIN) 
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * For more information, please visit the following website
 * 
 * https://eulerproject.io
 * https://github.com/euler-projects/euler-framework
 * https://cfrost.net
 */
package net.eulerframework.web.core.extend;

import java.util.Arrays;
import java.util.Locale;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import net.eulerframework.constant.LocaleCookies;
import net.eulerframework.web.config.WebConfig;

/**
 * 支持手动切换语言的RequestWrapper
 * 
 * @author cFrost
 *
 */
public class WebLanguageRequestWrapper extends HttpServletRequestWrapper {
    private final static String LOCALE_PARAM_NAME = "_locale";
    private final static String LOCALE_SESSION_ATTR_NAME = "__EULER_LOCALE__";
    
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private Locale locale;

    /**
     * 本构造函数会先检查请求中有无{@link WebLanguageRequestWrapper#LOCALE_PARAM_NAME}参数。
     * 如有，则以此参数值确定语言，并将语言信息放入Cookie和Session中 如没有，则依次尝试从Cookie和Session中获取
     * 
     * @param request
     *            请求，不会对请求做任何修改
     * @param response
     *            响应，只会向响应中添加关于语言的Cookie
     */
    public WebLanguageRequestWrapper(HttpServletRequest request, HttpServletResponse response) {
        super(request);
        try {
            this.locale = this.getLocaleFromParam(request);
            
            if(this.locale == null) {
                this.locale = this.getLocaleFromCookie(request);
            }
            
            if(this.locale == null) {
                this.locale = this.getLocaleFromSession(request);
            }
            
            if(this.locale == null) {
                this.locale = WebConfig.getDefaultLanguage();
            }
            
            Locale[] supportLocales = WebConfig.getSupportLanguages();
            
            if(!Arrays.asList(supportLocales).contains(this.locale)) {
                this.locale = WebConfig.getDefaultLanguage();
            }
            
            this.addLocaleIntoCookie(request, response);
            this.addLocaleIntoSession(request);
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
            this.locale = request.getLocale();
        }
    }

    /**
     * @param request
     * @return
     */
    private Locale getLocaleFromParam(HttpServletRequest request) {
        String localeParamValue = this.getRequest().getParameter(LOCALE_PARAM_NAME);
        if (StringUtils.hasText(localeParamValue)) {
            return this.generateLocale(localeParamValue);
        }
        return null;
    }

    /**
     * @param request
     * @return
     */
    private Locale getLocaleFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession();
        if(session != null) {
            Object locale = session.getAttribute(LOCALE_SESSION_ATTR_NAME);
            if (locale != null) {
                this.locale = (Locale) locale;
            }            
        }        
        return null;
    }

    private Locale getLocaleFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(LocaleCookies.LOCALE.getCookieName())) {
                    String localeStr = cookie.getValue();
                    return this.generateLocale(localeStr);
                }
            }
        }

        return null;
    }

    private Locale generateLocale(String localeStr) {
        Locale locale;
        if (localeStr.indexOf('-') < 0) {
            locale = new Locale(localeStr);
        } else {
            String[] localeStrs = localeStr.split("-");
            locale = new Locale(localeStrs[0], localeStrs[1]);
        }
        return locale;
    }

    private void addLocaleIntoCookie(HttpServletRequest request, HttpServletResponse response) {
        String language = this.locale.getLanguage();
        String country = this.locale.getCountry();
        String localeStr = null;
        
        if(StringUtils.hasText(language) && StringUtils.hasText(country)) {
            localeStr = language.toLowerCase() + "-" + country.toLowerCase();
        } else if(StringUtils.hasText(language)){
            localeStr = language.toLowerCase();
        } else if(StringUtils.hasText(country)) {
            localeStr = country.toLowerCase();
        }
        
        if(StringUtils.hasText(localeStr)) {
            Cookie cookie = new Cookie(LocaleCookies.LOCALE.getCookieName(), localeStr);
            cookie.setMaxAge(LocaleCookies.LOCALE.getCookieAge());
            cookie.setPath(request.getContextPath() + LocaleCookies.LOCALE.getCookiePath());
            response.addCookie(cookie);            
        }
    }

    /**
     * @param request
     */
    private void addLocaleIntoSession(HttpServletRequest request) {
        request.getSession().setAttribute(LOCALE_SESSION_ATTR_NAME, this.locale);
    }

    @Override
    public Locale getLocale() {
        return locale;
    }
}
