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
 * https://github.com/euler-form/web-form
 * https://cfrost.net
 */
package net.eulerframework.web.core.extend;

import java.util.Locale;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import net.eulerframework.web.config.WebConfig;

/**
 * 支持手动切换语言的RequestWrapper
 * 
 * @author cFrost
 *
 */
public class WebLanguageRequestWrapper extends HttpServletRequestWrapper {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final static String LOCALE_PARAM_NAME = "_locale";
    private final static String LOCALE_SESSION_ATTR_NAME = "__EULER_LOCALE__";
    private final static String LOCALE_COOKIE_NAME = "EULER_LOCALE";
    private final static int LOCALE_COOKIE_AGE = 10 * 365 * 24 * 60 * 60;
    private Locale locale;

    /**
     * 本构造函数会先检查请求中有无{@link WebLanguageRequestWrapper#LOCALE_PARAM_NAME}参数。
     * 如有，则以此参数值确定语言，并将语言信息放入Session和Cookie中 如没有，则依次尝试从Session和Cookie中获取
     * 
     * @param request
     *            请求，不会对请求做任何修改
     * @param response
     *            响应，只会向响应中添加关于语言的Cookie
     */
    public WebLanguageRequestWrapper(HttpServletRequest request, HttpServletResponse response) {
        super(request);

        try {
            HttpSession session = request.getSession();

            if (session != null) {
                String localeParamValue = this.getRequest().getParameter(LOCALE_PARAM_NAME);

                if (StringUtils.hasText(localeParamValue)) {
                    this.locale = this.generateLocale(localeParamValue);
                    session.setAttribute(LOCALE_SESSION_ATTR_NAME, this.locale);
                    this.addLocaleIntoCookie(request, response);
                } else {
                    Object locale = request.getSession().getAttribute(LOCALE_SESSION_ATTR_NAME);
                    if (locale != null) {
                        this.locale = (Locale) locale;
                        this.addLocaleIntoCookie(request, response);
                    } else {
                        Locale localeFromCookie = this.getLocaleFromCookie(request);

                        if (localeFromCookie != null) {
                            this.locale = localeFromCookie;
                            session.setAttribute(LOCALE_SESSION_ATTR_NAME, this.locale);
                        } else {
                            if(StringUtils.hasText(WebConfig.getDefaultLanguage())) {
                                this.locale = this.generateLocale(WebConfig.getDefaultLanguage());
                            } else {
                                this.locale = request.getLocale();                                
                            }
                        }
                    }
                }
            } else {
                Locale localeFromCookie = this.getLocaleFromCookie(request);

                if (localeFromCookie != null) {
                    this.locale = localeFromCookie;
                } else {
                    this.locale = request.getLocale();
                }
            }
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
            this.locale = request.getLocale();
        }
    }

    private Locale generateLocale(String localeStr) {
        Locale locale;
        if (localeStr.indexOf('_') < 0) {
            locale = new Locale(localeStr);
        } else {
            String[] localeStrs = localeStr.split("_");
            locale = new Locale(localeStrs[0], localeStrs[1]);
        }
        return locale;
    }

    private Locale getLocaleFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(LOCALE_COOKIE_NAME)) {
                    String localeStr = cookie.getValue();
                    return this.generateLocale(localeStr);
                }
            }
        }

        return null;
    }

    private void addLocaleIntoCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = new Cookie(LOCALE_COOKIE_NAME, this.locale.toString());
        cookie.setMaxAge(LOCALE_COOKIE_AGE);
        cookie.setPath(request.getContextPath());
        response.addCookie(cookie);
    }

    @Override
    public Locale getLocale() {
        return locale;
    }
}
