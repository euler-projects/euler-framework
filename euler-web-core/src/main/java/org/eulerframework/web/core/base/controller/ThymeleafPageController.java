/*
 * Copyright 2013-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eulerframework.web.core.base.controller;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.eulerframework.common.util.Assert;
import org.eulerframework.constant.EulerSysAttributes;
import org.eulerframework.web.config.WebConfig;
import org.eulerframework.web.core.base.WebContextAccessible;
import org.eulerframework.web.core.base.response.ErrorResponse;
import org.eulerframework.web.core.exception.web.*;
import org.eulerframework.web.core.exception.web.api.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

public abstract class ThymeleafPageController extends AbstractWebController {
    private static final String THEME_PARAM_NAME = "_theme";
    private static final String THEME_COOKIE_NAME = "EULER_THEME";
    private static final int THEME_COOKIE_AGE = 10 * 365 * 24 * 60 * 60;

    private static volatile Map<String, Object> TEMPLATE_ATTRIBUTES = null;
    private static final Object TEMPLATE_ATTRIBUTES_LOCK = new Object();

    private final String viewPathPrefix;

    protected ThymeleafPageController() {
        this(null);
    }

    protected ThymeleafPageController(String viewPath) {
        if (viewPath == null) {
            this.viewPathPrefix = "/";
        } else {
            Assert.isTrue(!viewPath.endsWith("/"), "viewPath must not end with /");
            while (viewPath.startsWith("/")) {
                viewPath = viewPath.substring(1);
            }
            this.viewPathPrefix = viewPath + "/";
        }
    }

    /**
     * 获取主题名称,会优获取请求参数_theme,然后从cookie中获取,从请求参数中获取的_theme会被放到cookie中
     *
     * @return 主题名称, 默认为default
     */
    protected String theme() {
        String theme = this.getRequest().getParameter(THEME_PARAM_NAME);
        if (!StringUtils.hasText(theme)) {
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

        if (!StringUtils.hasText(theme)) {
            theme = WebConfig.getDefaultTheme();
        }

        return theme;
    }

    /**
     * open the theme view
     *
     * @param view view name
     * @return the prefixed view that format is theme/{@link ThymeleafPageController#theme()}/{@link ThymeleafPageController#viewPathPrefix}/{view}
     */
    protected String display(String view) {
        return this.display(view, true);
    }

    /**
     * open a view
     *
     * @param view      view name
     * @param themeView <code>true</code> to use theme like {@link ThymeleafPageController#display(String)}
     *                  or <code>false</code> to open view {@link ThymeleafPageController#viewPathPrefix}/{view}
     * @return the prefixed view that format is <pre>{@code
     *      if(themeView) {
     *          prefixedView = "theme/" + this.theme() + this.viewPathPrefix + view;
     *      } else {
     *          prefixedView = this.viewPathPrefix + view;
     *      }
     * }</pre>
     */
    protected String display(String view, boolean themeView) {
        Assert.notNull(view, "view is empty");

        if (TEMPLATE_ATTRIBUTES == null) {
            synchronized (TEMPLATE_ATTRIBUTES_LOCK) {
                if (TEMPLATE_ATTRIBUTES == null) {
                    TEMPLATE_ATTRIBUTES = Map.of("ctx", getEulerAttributesContext());
                }
            }
        }

        this.getRequest().setAttribute("euler", TEMPLATE_ATTRIBUTES);

        // ensure view not start with a '/'
        while (view.startsWith("/")) {
            view = view.substring(1);
        }

        String prefixedView;
        if (themeView) {
            prefixedView = "theme/" + this.theme() + this.viewPathPrefix + view;
        } else {
            prefixedView = this.viewPathPrefix + view;
        }
        this.logger.trace("display view '{}'", prefixedView);
        return prefixedView;
    }

    /**
     * 发送重定向<br>
     * 不以'/'开头表示相对路径, 以'/'表示绝对路径,不需要加contextPath
     *
     * @param action 重定向目标
     * @return 重定向字符串
     */
    protected String redirect(String action) {
        Assert.notNull(action, "action path is empty");

        if (!action.startsWith("/"))
            return "redirect:" + "/" + this.viewPathPrefix + action;
        else
            return "redirect:" + action;
    }

    /**
     * 显示跳转页面
     *
     * @param message 显示信息
     * @param target  跳转目标
     * @param wait    等待时间(秒)
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
     *
     * @param webException 错误异常
     * @return 错误页面
     */
    private String error(WebException webException) {
        Assert.notNull(webException, "Error exception can not be null");
        ErrorResponse errorResponse = new ErrorResponse(webException);
        this.getRequest().setAttribute("__error", errorResponse);
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
     *
     * @param message 未国际化前的信息
     * @param target  自定义链接
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
        if (WebConfig.showStackTraceInCrashPage()) {
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

    private Map<String, Object> getEulerAttributesContext() {
        ServletContext servletContext = getServletContext();
        Set<String> eulerSysAttributeNames = EulerSysAttributes.getEulerSysAttributeNames();
        Map<String, Object> context = new HashMap<>();
        for (String eulerSysAttributeName : eulerSysAttributeNames) {
            Object value = servletContext.getAttribute(eulerSysAttributeName);
            if (value != null) {
                context.put(eulerSysAttributeName, value);
            }
        }
        return context;
    }

//    private static final String PAGE_CONTROLLER_NAME_SUFFIX = "PageController";
//    private static final String CONTROLLER_NAME_SUFFIX = "Controller";
//    /**
//     * 从类名自动解析ControllerName
//     * <p>
//     * 要使用此方法, Controller必须以Controller或PageController结尾,
//     * 获取到的名字为Controller的类名去掉Controller或PageController后缀,首字母变为小写
//     * <p>
//     * 例如: ExampleControllerPageController的名字为exampleController
//     */
//    private String initWebControllerName() {
//        String className = this.getClass().getSimpleName();
//
//        int indexOfWebController = className.lastIndexOf(PAGE_CONTROLLER_NAME_SUFFIX);
//        if (indexOfWebController <= 0) {
//            indexOfWebController = className.lastIndexOf(CONTROLLER_NAME_SUFFIX);
//            if (indexOfWebController <= 0) {
//                throw new RuntimeException(
//                        "If you want to use this.display(), Controller's class name must end with '" + CONTROLLER_NAME_SUFFIX + "' or '" + PAGE_CONTROLLER_NAME_SUFFIX + "'");
//            }
//        }
//
//        return org.eulerframework.common.util.StringUtils.toLowerCaseFirstChar(className.substring(0, indexOfWebController));
//    }

    public static class Target extends WebContextAccessible {
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

        public static final Target HOME_TARGET = new Target("/", "_GO_HOME");
    }
}
