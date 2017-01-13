package net.eulerframework.web.core.base.controller;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ExceptionHandler;

import net.eulerframework.common.util.StringTool;
import net.eulerframework.web.core.Lang;
import net.eulerframework.web.core.exception.WebControllerException;
import net.eulerframework.web.core.exception.view.ViewException;
import net.eulerframework.web.core.i18n.Tag;

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
    
    protected String redirect(String action) {
        Assert.isTrue(!StringTool.isNull(action), "action path is empty");

        if(!action.startsWith("/"))
            return "redirect:" + "/" + this.getWebControllerName() + "/" + action;
        else
            return "redirect:" + action;        
    }
    
    /**
     * 显示跳转页面
     * @param message 显示信息
     * @param target 跳转目标,不需要加contextPath
     * @param waitSeconds 等待时间(秒)
     * @return 跳转view
     */
    protected String jump(String message, String target, int waitSeconds) {
        message = message == null ? Tag.i18n(Lang.CORE.PAGE_WILL_REDIRECT.toString()) : message;
        target = target == null ? "" : target;
        
        String contextPath = this.getServletContext().getContextPath();
        if(contextPath.equals("/"))
            contextPath = "";
        
        if(target.startsWith("/")) {
            if(target.length() == 1) {
                target = "";                
            }
            else {
                target = target.substring(1);
            }
        }

        HttpServletRequest request = this.getRequest();
        request.setAttribute("message", message);
        request.setAttribute("target", contextPath + "/" + target);
        request.setAttribute("waitSeconds", waitSeconds);
        return this.display("/common/jump");
    }
    
    protected String error(String message) {
        message = message == null ? Tag.i18n(Lang.CORE.UNKNOWN_ERROR.toString()) : message;

        HttpServletRequest request = this.getRequest();
        request.setAttribute("message", message);
        return this.display("/common/error");
        
    }
    
    protected String success(String message) {
        message = message == null ? Tag.i18n(Lang.CORE.SUCCESS.toString()) : message;

        HttpServletRequest request = this.getRequest();
        request.setAttribute("message", message);
        return this.display("/common/success");
        
    }
    
    protected String notfound() {
        return this.redirect("/error-404");
    }
    
    /**  
     * 用于在程序发生{@link ViewException}异常时统一返回错误信息 
     * @return  
     */  
    @ExceptionHandler({ViewException.class})   
    public String viewException(ViewException e) {
        this.logger.error(e.getMessage(), e);
        return this.error(e.getLocalizedMessage());
    }

}
