package net.eulerframework.web.core.base.controller;

import javax.servlet.http.Cookie;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import net.eulerframework.common.util.StringTool;
import net.eulerframework.web.core.exception.ResourceNotFoundException;
import net.eulerframework.web.core.exception.WebControllerException;

public abstract class AbstractWebController extends BaseController {

    /**
     * 获取WebController的名字<br>
     * 要使用此方法, WebController必须以WebController结尾<br>
     * 获取到的名字为WebController的类名去掉WebController后缀,首字母变为小写
     * @return ExampleControllerWebController的名字为exampleController
     */
    private String getWebControllerName() {
        String className = this.getClass().getSimpleName();

        int indexOfWebController = className.lastIndexOf("WebController");

        if(indexOfWebController <= 0)
            throw new WebControllerException("If you want to use this.display(), WebController's class name must end with 'WebController'");

        return StringTool.toLowerCaseFirstChar(className.substring(0, className.lastIndexOf("WebController")));
    }

    /**
     * 获取主题名称,会优获取请求参数_theme,然后从cookie中获取,从请求参数中获取的_theme会被放到cookie中
     * @return 主题名称,默认为default
     */
    protected String theme() {
        String themeParamName = "_theme";

        String theme = this.getRequest().getParameter(themeParamName);
        if(StringTool.isNull(theme)) {
            Cookie[] cookies = this.getRequest().getCookies();

            if(cookies != null) {
                for(Cookie cookie : cookies) {
                    if(cookie.getName().equals(themeParamName)) {
                        theme = cookie.getValue();
                    }
                }
            }

        } else {
            Cookie cookie = new Cookie(themeParamName, theme);
            this.getResponse().addCookie(cookie);
        }

        if(StringTool.isNull(theme)) {
            theme = "default";
        }

        return theme;
    }

    /**
     * 显示view<br>
     * 如果只指定view名称,则默认为themeName/webControllerName/view.jsp<br>
     * 如果view以'/'开头,则显示themeName/view.jsp
     * @param view view name
     * @return view在JspPath下的路径
     */
    protected String display(String view) {
        Assert.isTrue(!StringTool.isNull(view), "view path is empty");

        if(!view.startsWith("/"))
            return this.theme() + "/" + this.getWebControllerName() + "/" + view;
        else
            return this.theme() + view;
    }

    /**
     * 用于在程序发生{@link ResourceNotFoundException}异常时统一返回错误信息
     * @return 404页面
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({ResourceNotFoundException.class})
    @Override
    public String resourceNotFoundException(ResourceNotFoundException e) {
        return this.display("/exception/404");
    }

    /**
     * 用于在程序发生{@link AccessDeniedException}异常时统一返回错误信息
     * @return 403页面
     */
    @ExceptionHandler({AccessDeniedException.class})
    @Override
    public String accessDeniedException(AccessDeniedException e) {
        return this.display("/exception/403");
    }

}
