/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eulerframework.web.core.extend;

import java.util.Arrays;
import java.util.Locale;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import org.eulerframework.common.util.CommonUtils;
import org.eulerframework.web.config.WebConfig;
import org.eulerframework.web.core.cookie.LocaleCookies;

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
    
    private String staticRequestPrefix;
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
    public WebLanguageRequestWrapper(HttpServletRequest request, HttpServletResponse response) throws NeedRedirectException {
        super(request);
        
        this.staticRequestPrefix = request.getContextPath() + WebConfig.getStaticPagesRootPath() + "/";
        
        try {
            this.locale = this.getLocaleFromPath(request);
            
            if(this.locale == null) {
                this.locale = this.getLocaleFromParam(request);
                
                if(this.locale == null) {
                    this.locale = this.getLocaleFromCookie(request);
                    
                    if(this.locale == null) {
                        this.locale = this.getLocaleFromSession(request);
                    }
                }
            } else { 
                
                /*
                 * getLocaleFromPath不为空说明是前端页面访问, 这时检查是否指定了_locale参数,
                 * 如果指定了_locale参数, 切指定的语言和path中的不一致, 以_locale参数的为准,
                 * 此时将发生页面重定向
                 */
                Locale paramLocale = this.getLocaleFromParam(request);
                if(paramLocale != null && !paramLocale.equals(this.locale)) {
                    String redirectUri = "/h" + request.getRequestURI().substring((this.staticRequestPrefix + CommonUtils.formatLocal(this.locale, '-')).length());
                    redirectUri = redirectUri + "?" + request.getQueryString(); //因为是从前端页面的请求参数中取出了语言, 所以queryString一定有值
                    throw new NeedRedirectException(request.getContextPath() + redirectUri);
                }
            }
            
            if(this.locale == null || !Arrays.asList(WebConfig.getSupportLanguages()).contains(this.locale)) {
                this.locale = WebConfig.getDefaultLanguage();
            }
            
            this.addLocaleIntoCookie(request, response);
            this.addLocaleIntoSession(request);
        } catch (NeedRedirectException e) {
            throw e;
        }catch (Exception e) {
            this.logger.error(e.getMessage(), e);
            this.locale = request.getLocale();
        }
    }
    
    private boolean isStaticPageRequest(HttpServletRequest request) {
        return "GET".equalsIgnoreCase(request.getMethod()) 
                && request.getRequestURI().startsWith(this.staticRequestPrefix);
    }
    
    private Locale returnLocaleFromLocaleString(String localeStr) {
        try {
            Locale locale = CommonUtils.parseLocale(localeStr);

            /*
             * 判断请求路径是否为受支持的语言, 若不受支持则返回null, 
             * 这样设计的目的是因为path, cookie, param的方式都是由访问者指定的, 
             * 如果访问者指定了一个不支持的语言可以让后面的获取逻辑尝试获取访问者先前指定的语言,
             * 如果有合适的语言, 网站可以保持访问者指定错误语言前的样子
             */
            if(!Arrays.asList(WebConfig.getSupportLanguages()).contains(locale)) {
                return null;
            }
            
            return locale;
        } catch (Exception e) {
            this.logger.info(e.getMessage());
        }
        
        return null;
    }

    private Locale getLocaleFromPath(HttpServletRequest request) {
        if(this.isStaticPageRequest(request)) {
            String path = request.getRequestURI().substring(this.staticRequestPrefix.length());
            if(path.indexOf("/") > 0) {
                String localeStr = path.substring(0, path.indexOf("/"));
                return this.returnLocaleFromLocaleString(localeStr);
            }
        }
        return null;
    }

    private Locale getLocaleFromParam(HttpServletRequest request) {
        String localeParamValue = request.getParameter(LOCALE_PARAM_NAME);
        if (StringUtils.hasText(localeParamValue)) {
            return this.returnLocaleFromLocaleString(localeParamValue);
        }
        return null;
    }

    private Locale getLocaleFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(LocaleCookies.LOCALE.getCookieName())) {
                    String localeStr = cookie.getValue();
                    return this.returnLocaleFromLocaleString(localeStr);
                }
            }
        }

        return null;
    }

    private Locale getLocaleFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession();
        if(session != null) {
            Object locale = session.getAttribute(LOCALE_SESSION_ATTR_NAME);
            if (locale != null) {
                return (Locale) locale;
            }            
        }        
        return null;
    }

    private void addLocaleIntoCookie(HttpServletRequest request, HttpServletResponse response) {
        String localeStr = CommonUtils.formatLocal(this.locale, '-');
        
        if(StringUtils.hasText(localeStr)) {
            Cookie cookie = new Cookie(LocaleCookies.LOCALE.getCookieName(), localeStr);
            cookie.setMaxAge(LocaleCookies.LOCALE.getCookieAge());
            cookie.setPath(request.getContextPath() + LocaleCookies.LOCALE.getCookiePath());
            response.addCookie(cookie);            
        }
    }

    private void addLocaleIntoSession(HttpServletRequest request) {
        request.getSession().setAttribute(LOCALE_SESSION_ATTR_NAME, this.locale);
    }

    @Override
    public Locale getLocale() {
        return locale;
    }
    
    public static class NeedRedirectException extends Exception {
        private String redirectUrl;
        
        private NeedRedirectException(String redirectUrl) {
            this.redirectUrl = redirectUrl;
        }
        
        public String getRedirectUrl() {
            return this.redirectUrl;
        }
    }
}
