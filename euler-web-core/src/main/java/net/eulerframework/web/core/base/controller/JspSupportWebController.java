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
 */
package net.eulerframework.web.core.base.controller;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import net.eulerframework.common.util.Assert;
import net.eulerframework.common.util.StringUtils;
import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.base.WebContextAccessable;
import net.eulerframework.web.core.exception.web.BadCredentialsWebException;
import net.eulerframework.web.core.exception.web.PageNotFoundException;
import net.eulerframework.web.core.exception.web.SystemWebError;
import net.eulerframework.web.core.exception.web.UndefinedWebException;
import net.eulerframework.web.core.exception.web.WebException;
import net.eulerframework.web.core.exception.web.api.ResourceNotFoundException;

public abstract class JspSupportWebController extends AbstractWebController {
    private final static String THEME_PARAM_NAME = "_theme";
    private final static String THEME_COOKIE_NAME = "EULER_THEME";
    private final static String CONTROLLER_NAME_SUFFIX = "JspController";
    private final static int THEME_COOKIE_AGE = 10 * 365 * 24 * 60 * 60;
    
    private String webControllerName;
    
    protected void setWebControllerName(String webControllerName) {
        Assert.notNull(webControllerName);
        
        while(webControllerName.startsWith("/"))
            webControllerName = webControllerName.substring(1);
        
        Assert.hasText(webControllerName);
        this.webControllerName = webControllerName;
    }
    
    /**
     * 获取WebController的名字<br>
     * 要使用此方法, WebController必须以WebController结尾<br>
     * 获取到的名字为WebController的类名去掉WebController后缀,首字母变为小写
     * 
     * @return ExampleControllerWebController的名字为exampleController
     */
    private String getWebControllerName() {
        if(StringUtils.hasText(this.webControllerName))
            return this.webControllerName;
        
        String className = this.getClass().getSimpleName();

        int indexOfWebController = className.lastIndexOf(CONTROLLER_NAME_SUFFIX);

        if (indexOfWebController <= 0)
            throw new RuntimeException(
                    "If you want to use this.display(), JspController's class name must end with '" + CONTROLLER_NAME_SUFFIX + "'");

        return StringUtils.toLowerCaseFirstChar(className.substring(0, className.lastIndexOf("JspController")));
    }

    /**
     * 获取主题名称,会优获取请求参数_theme,然后从cookie中获取,从请求参数中获取的_theme会被放到cookie中
     * 
     * @return 主题名称,默认为default
     */
    protected String theme() {

        String theme = this.getRequest().getParameter(THEME_PARAM_NAME);
        if (StringUtils.isEmpty(theme)) {
            Cookie[] cookies = this.getRequest().getCookies();

            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals(THEME_COOKIE_NAME)) {
                        theme = cookie.getValue();
                    }
                }
            }

        } else {
            Cookie cookie = new Cookie(THEME_COOKIE_NAME, theme);
            cookie.setMaxAge(THEME_COOKIE_AGE);
            this.getResponse().addCookie(cookie);
        }

        if (StringUtils.isNull(theme)) {
            theme = WebConfig.getDefaultTheme();
        }

        return theme;
    }

    /**
     * 显示view<br>
     * 如果只指定view名称,则默认为themeName/webControllerName/view.jsp<br>
     * 如果view以'/'开头,则显示themeName/view.jsp
     * 
     * @param view
     *            view name
     * @return view在JspPath下的路径
     */
    protected String display(String view) {
        Assert.notNull(view, "view path is empty");

        if (!view.startsWith("/"))
            return this.theme() + "/" + this.getWebControllerName() + "/" + view;
        else
            return this.theme() + view;
    }

    /**
     * 发送重定向<br>
     * 不以'/'开头表示相对路径, 以'/'表示绝对路径,不需要加contextPath
     * 
     * @param action
     *            重定向目标
     * @return 重定向字符串
     */
    protected String redirect(String action) {
        Assert.notNull(action, "action path is empty");

        if (!action.startsWith("/"))
            return "redirect:" + "/" + this.getWebControllerName() + "/" + action;
        else
            return "redirect:" + action;
    }

    /**
     * 显示跳转页面
     * 
     * @param message 显示信息
     * @param target 跳转目标
     * @param wait 等待时间(秒)
     * @return 跳转页面
     */
    protected String jump(String message, Target target, int wait) {
        Assert.notNull(message, "Jump message can not be null"); 
        
        target = target == null ? Target.HOME_TARGET : target;
        
        HttpServletRequest request = this.getRequest();
        request.setAttribute("__message", message);  
        request.setAttribute("__target", target);  
        request.setAttribute("__wait", wait);
        return this.display("/common/jump");
    }
    
    /**
     * 显示默认错误异常错误页面，含有一个指向首页的链接
     * 
     * @return 错误页面
     */
    protected String error() {
        return this.error(new UndefinedWebException());
    }
    
    /**
     * 显示自定义错误信息，含有一个指向首页的链接
     * 
     * @return 错误页面
     */
    protected String error(String message) {
        return this.error(new UndefinedWebException(message));
    }
    
    /**
     * 显示错误页面
     * 自定义错误信息可在jsp中用{@code ${__error}}获取
     * 自定义错误代码可在jsp中用{@code ${__code}}获取
     * 自定义错误详情可在jsp中用{@code ${__error_description}}获取
     * @param webException 错误异常
     * @return 错误页面
     */
    private String error(WebException webException) {
        Assert.notNull(webException, "Error exception can not be null"); 
        this.getRequest().setAttribute("__error_description", webException.getLocalizedMessage());   
        this.getRequest().setAttribute("__error", webException.getError());
        this.getRequest().setAttribute("__code", webException.getCode()); 
        return this.display("/common/error");
    }

    /**
     * 显示成功页面,不指定信息，含有一个指向首页的链接
     * 
     * @return 成功页面
     */
    protected String success() {
        return this.success(null, Target.HOME_TARGET);
    }

    /**
     * 显示成功页面,指定信息，含有一个指向首页的链接
     * 
     * @param message 未国际化前的信息,为<code>null</code>时不显示
     * @return 成功页面
     */
    protected String success(String message) {
        return this.success(message, Target.HOME_TARGET);
    }

    /**
     * 显示成功页面，指定一个自定义的文字信息、若干个自定义链接.
     * 自定义成功信息可在jsp中用<code>${__message}</code>获取
     * 链接信息可在jsp中用<code>${__targets}</code>获取
     * @param message 未国际化前的信息
     * @param target 自定义链接
     * @return 成功页面
     */
    protected String success(String message, Target... target) {
        this.getRequest().setAttribute("__message", message);   
        this.getRequest().setAttribute("__targets", target);
        //return this.redirect("/common/success");
        return this.display("/common/success");
    }

    /**
     * 显示404页面
     * 
     * @return 对应主题的404页面
     */
    protected String notfound() {
        this.getResponse().setStatus(HttpStatus.NOT_FOUND.value());
        return this.display("/error/404");
    }

    /**
     * 崩溃页面(500)
     * 
     * @return 对应主题的500错误页面
     */
    protected String crashPage(Throwable e) {
        this.logger.error(e.getMessage(), e);
        if (WebConfig.isDebugMode()) {
            this.getRequest().setAttribute("__crashInfo", e.getMessage());
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            this.getRequest().setAttribute("__crashStackTrace", sw.toString());
        } else {
            // DO_NOTHING
        }
        this.getResponse().setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return this.display("/error/500");
    }

    /**
     * 用于在程序发生{@link ResourceNotFoundException}异常时统一返回错误信息
     * 
     * @return 对应主题的404页面
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public String resourceNotFoundException(ResourceNotFoundException e) {
        this.logger.warn(e.getMessage(), e);
        return this.notfound();
    }

    /**
     * 用于在程序发生{@link PageNotFoundException}异常时统一返回错误信息
     * 
     * @return 对应主题的404页面
     */
    @ExceptionHandler(PageNotFoundException.class)
    public String pageNotFoundException(PageNotFoundException e) {
        return this.notfound();
    }

    /**
     * 用于在程序发生{@link WebException}异常时统一返回错误信息
     * 
     * @return
     */
    @ExceptionHandler(WebException.class)
    public String webException(WebException e) {
        this.logger.debug("Error Code: " + e.getCode() + "message: " + e.getMessage(), e);
        return this.error(e);
    }
    
    @ExceptionHandler(BadCredentialsException.class)
    public String badCredentialsException(BadCredentialsException e) {
        return this.error(new BadCredentialsWebException());
    }
    
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public String missingServletRequestParameterException(MissingServletRequestParameterException e) {
        return this.error(new WebException(e.getMessage(), SystemWebError.PARAMETER_NOT_MEET_REQUIREMENT));
    }
    
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String methodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return this.error(new WebException("Parameter '" + e.getParameter().getParameterName() + "' has an invalid value: " + e.getValue(), 
                SystemWebError.PARAMETER_NOT_MEET_REQUIREMENT));
    }

    /**
     * 用于在程序发生{@link Exception}异常时统一返回错误信息
     * 
     * @return 崩溃页面(500)
     */
    @ExceptionHandler(Exception.class)
    public String exception(Exception e) {
        this.logger.error(e.getMessage(), e);
        return this.crashPage(e);
    }
    
    public static class Target extends WebContextAccessable {
        private String href;
        private String name;
        
        public String getHref() {
            return href;
        }
        
        public String getName() {
            return name;
        }
        
        public Target(String href) {
            this(href, null);
        }
        
        public Target(String href, String name) {
            Assert.notNull(href, "Target href exception can not be null");
            
            String contextPath = this.getServletContext().getContextPath();
            if (!contextPath.endsWith("/"))
                contextPath += "/";

            if (href.startsWith("/")) {
                if (href.length() == 1) {
                    href = "";
                } else {
                    href = href.substring(1);
                }
            }
            
            this.href = contextPath + href;
            this.name = name == null ? href : name;
        }
        
        public final static Target HOME_TARGET = new Target("/", "_GO_HOME");
    }
}
