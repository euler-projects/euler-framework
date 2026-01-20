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

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.eulerframework.common.util.Assert;
import org.eulerframework.web.config.WebConfig;
import org.eulerframework.web.core.base.WebContextAccessible;
import org.eulerframework.web.core.base.response.ErrorResponse;
import org.eulerframework.web.core.exception.web.PageNotFoundException;
import org.eulerframework.web.core.exception.web.SystemWebError;
import org.eulerframework.web.core.exception.web.UndefinedWebException;
import org.eulerframework.web.core.exception.web.WebException;
import org.eulerframework.web.core.exception.web.api.ResourceNotFoundException;
import org.eulerframework.web.servlet.util.ServletUtils;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

public abstract class PageSupportWebController extends AbstractWebController {
    private static final String THEME_PARAM_NAME = "_theme";
    private static final String THEME_COOKIE_NAME = "EULER_THEME";
    private static final int THEME_COOKIE_AGE = 10 * 365 * 24 * 60 * 60;

    private final PageRender pageRender;

    protected PageSupportWebController(PageRender pageRender) {
        this.pageRender = pageRender;
    }

    /**
     * 获取主题名称,会优获取请求参数_theme,然后从cookie中获取,从请求参数中获取的_theme会被放到cookie中
     *
     * @return 主题名称, 默认为default
     */
    protected String theme() {
        String theme = ServletUtils.getRequest().getParameter(THEME_PARAM_NAME);
        if (!StringUtils.hasText(theme)) {
            Cookie[] cookies = ServletUtils.getRequest().getCookies();

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
            ServletUtils.getResponse().addCookie(cookie);
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
     * @return A {@link ModelAndView} object include system attributes
     */
    protected ModelAndView display(String view) {
        return this.display(view, true);
    }

    protected ModelAndView display(String view, boolean themeView) {
        return this.display(view, themeView, null);
    }

    /**
     * open a view
     *
     * @param view      view name
     * @param themeView <code>true</code> to use theme view
     *                  or <code>false</code> to open view without theme
     * @return A {@link ModelAndView} object include system attributes
     */
    protected ModelAndView display(String view, boolean themeView, Map<String, Object> model) {
        return this.pageRender.display(view, themeView ? this.theme() : null, model);
    }

    /**
     * Redirect to a same site url path
     * <p>
     * <pre>{@code
     *     url: http://example.com/path/123
     *     action: abc => http://example.com/path/abc
     *     action: /abc => http://example.com/abc
     *
     *     url: http://example.com/some/path/
     *     action: abc => http://example.com/some/path/abc
     *     action: /abc => http://example.com/abc
     *
     *     url: http://example.com/
     *     action: abc => http://example.com/abc
     *     action: /abc => http://example.com/abc
     *
     *     url: http://example.com/context-path/path/123
     *     action: abc => http://example.com/context-path/path/abc
     *     action: /abc => http://example.com/context-path/abc
     * }</pre>
     *
     * @param action start with <code>/</code> to redirect to an absolute path,
     *               or start without <code>/</code> to a relative path.
     *               if redirect to an absolute path, <code>context-path</code> is must not provide
     * @return The redirect target
     */
    protected ModelAndView redirect(String action) {
        Assert.notNull(action, "action path is empty");
        String view;
        if (action.startsWith("/")) {
            view = "redirect:" + action;
        } else {
            String realUrl = ServletUtils.findRealURI(this.getRequest());
            int lastSlash = realUrl.lastIndexOf('/');
            if (lastSlash < 0) {
                view = "redirect:/" + action;
            } else if (lastSlash == realUrl.length() - 1) {
                view = "redirect:" + realUrl + action;
            } else {
                view = "redirect:" + realUrl.substring(0, lastSlash) + "/" + action;
            }
        }
        return new ModelAndView(view);
    }

    /**
     * 显示跳转页面
     *
     * @param message 显示信息
     * @param target  跳转目标
     * @param wait    等待时间(秒)
     * @return 跳转页面
     */
    protected ModelAndView jump(String message, Target target, int wait) {
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
    protected ModelAndView error() {
        return this.error(new UndefinedWebException());
    }

    /**
     * 显示自定义错误信息，含有一个指向首页的链接
     *
     * @return 错误页面
     */
    protected ModelAndView error(String message) {
        return this.error(new UndefinedWebException(message));
    }

    /**
     * 显示错误页面
     * 自定义错误信息可在jsp中用{@code ${error.error}}获取
     * 自定义错误代码可在jsp中用{@code ${error.code}}获取
     * 自定义错误详情可在jsp中用{@code ${error.message}}获取
     *
     * @param webException 错误异常
     * @return 错误页面
     */
    private ModelAndView error(WebException webException) {
        Assert.notNull(webException, "Error exception can not be null");
        ErrorResponse errorResponse = new ErrorResponse(webException);
        return this.error(errorResponse);
    }

    protected ModelAndView error(ErrorResponse errorResponse) {
        Map<String, Object> model = new HashMap<>();
        model.put("error", errorResponse);
        return this.display("/error/default", true, model);
    }

    /**
     * 显示成功页面,不指定信息，含有一个指向首页的链接
     *
     * @return 成功页面
     */
    protected ModelAndView success() {
        return this.success(null, Target.HOME_TARGET);
    }

    /**
     * 显示成功页面,指定信息，含有一个指向首页的链接
     *
     * @param message 未国际化前的信息,为<code>null</code>时不显示
     * @return 成功页面
     */
    protected ModelAndView success(String message) {
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
    protected ModelAndView success(String message, Target... target) {
        Map<String, Object> model = new HashMap<>();
        model.put("__message", message);
        model.put("__targets", target);
        return this.display("/common/success", true, model);
    }

    /**
     * 显示404页面
     *
     * @return 对应主题的404页面
     */
    protected ModelAndView notfound() {
        this.getResponse().setStatus(HttpStatus.NOT_FOUND.value());
        return this.display("/error/404");
    }

    /**
     * 用于在程序发生{@link ResourceNotFoundException}异常时统一返回错误信息
     *
     * @return 对应主题的404页面
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView resourceNotFoundException(ResourceNotFoundException e) {
        this.logger.warn(e.getMessage(), e);
        return this.notfound();
    }

    /**
     * 用于在程序发生{@link PageNotFoundException}异常时统一返回错误信息
     *
     * @return 对应主题的404页面
     */
    @ExceptionHandler(PageNotFoundException.class)
    public ModelAndView pageNotFoundException(PageNotFoundException e) {
        return this.notfound();
    }

    /**
     * 用于在程序发生{@link WebException}异常时统一返回错误信息
     *
     * @return
     */
    @ExceptionHandler(WebException.class)
    public ModelAndView webException(WebException e) {
        this.logger.debug("Error Code: {} message: {}", e.getCode(), e.getMessage(), e);
        return this.error(e);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ModelAndView missingServletRequestParameterException(MissingServletRequestParameterException e) {
        return this.error(new WebException(e.getMessage(), SystemWebError.PARAMETER_NOT_MEET_REQUIREMENT));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ModelAndView methodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return this.error(new WebException("Parameter '" + e.getParameter().getParameterName() + "' has an invalid value: " + e.getValue(), SystemWebError.PARAMETER_NOT_MEET_REQUIREMENT));
    }

    public static class Target extends WebContextAccessible {
        private final String href;
        private final String name;

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
            if (!contextPath.endsWith("/")) contextPath += "/";

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
