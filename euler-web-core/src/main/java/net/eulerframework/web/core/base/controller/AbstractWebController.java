package net.eulerframework.web.core.base.controller;

import javax.servlet.http.Cookie;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import net.eulerframework.common.util.StringTool;
import net.eulerframework.web.core.base.response.HttpStatusResponse;
import net.eulerframework.web.core.exception.NotFoundException;
import net.eulerframework.web.core.exception.WebControllerException;

public abstract class AbstractWebController extends BaseController {
    
    private String getWebControllerName() {
        String className = this.getClass().getSimpleName();
        
        int indexOfWebController = className.lastIndexOf("WebController");
        
        if(indexOfWebController <= 0)
            throw new WebControllerException("If you want to use this.display(), WebController's class name must end with 'WebController'");  
            
        return StringTool.toLowerCaseFirstChar(className.substring(0, className.lastIndexOf("WebController")));
    }
    
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
    
    protected String display(String viewPath) {
        Assert.isTrue(!StringTool.isNull(viewPath), "view path is empty");
        
        if(!viewPath.startsWith("/"))
            return this.theme() + "/" + this.getWebControllerName() + "/" + viewPath;
        else
            return this.theme() + viewPath;
    }
    
    
    
    /**  
     * 用于在程序发生{@link NotFoundException}异常时统一返回错误信息 
     * @return  
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({NotFoundException.class})   
    public String notFoundException(NotFoundException e) {
        return "/404";
    }
    
    /**  
     * 用于在程序发生{@link AccessDeniedException}异常时统一返回错误信息 
     * @return  
     */
    @ExceptionHandler({AccessDeniedException.class})   
    public HttpStatusResponse accessDeniedException(AccessDeniedException e) {
        e.printStackTrace();
        throw e;
    }
    
}
