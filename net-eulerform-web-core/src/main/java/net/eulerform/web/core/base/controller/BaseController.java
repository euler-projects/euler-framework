package net.eulerform.web.core.base.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import net.eulerform.web.core.base.entity.HttpStatusResponse;
import net.eulerform.web.core.base.entity.WebResponseStatus;
import net.eulerform.web.core.base.exception.IllegalParamException;
import net.eulerform.web.core.base.exception.ResourceExistException;
import net.eulerform.web.core.util.UrlTool;

public abstract class BaseController {
    
    protected final Logger logger = LogManager.getLogger(this.getClass());
    
    protected ServletContext getServletContext(){
        WebApplicationContext webApplicationContext = ContextLoader.getCurrentWebApplicationContext();  
        return webApplicationContext.getServletContext();
    }
    
    protected void writeString(HttpServletResponse httpServletResponse, String str) throws IOException{
        httpServletResponse.getOutputStream().write(str.getBytes("UTF-8"));
    }

    @RequestMapping(value={"", "/", "index"},method=RequestMethod.GET)
    public String index(HttpServletRequest request, Model model) {
        String moduleName = this.findModuleName(request);
        String pagePath = moduleName+"/index";
        this.logger.info("Redirect to module index page: "+pagePath);
        return pagePath;
    }
    
    private String findModuleName(HttpServletRequest httpServletRequest){
        String requestURI = UrlTool.findRealURI(httpServletRequest);
        while(requestURI.lastIndexOf(".") > requestURI.lastIndexOf("/")){
            requestURI=requestURI.substring(0, requestURI.lastIndexOf("."));
        }
        if(requestURI.endsWith("/")){
            requestURI = requestURI.substring(0, requestURI.length()-1);
        }
        if(requestURI.endsWith("/index")){
            requestURI = requestURI.substring(0, requestURI.length()-"/index".length());
        }
        if("".equals(requestURI)){
            requestURI="/root";
        }
        return requestURI.substring(requestURI.lastIndexOf("/"));
    }
    
    /**  
     * 用于在程序发生{@link BindException}异常时统一返回错误信息 
     * @return  
     */  
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({BindException.class})   
    public HttpStatusResponse bindException(BindException e) {
        e.printStackTrace();
        List<ObjectError> errors = e.getAllErrors();
        List<String> errMsg = new ArrayList<>();
        for(ObjectError err : errors){
            if(FieldError.class.isAssignableFrom(err.getClass()))
                errMsg.add(((FieldError)err).getField()+ ": " + err.getDefaultMessage());
            else
                errMsg.add(err.getDefaultMessage());
        }
        return new HttpStatusResponse(WebResponseStatus.FIELD_VALID_FAILED.value(), errMsg.toString());
    }
    
    /**  
     * 用于在程序发生{@link ResourceExistException}异常时统一返回错误信息 
     * @return  
     */  
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ResourceExistException.class})   
    public HttpStatusResponse exception(ResourceExistException e) {
        e.printStackTrace();
        return new HttpStatusResponse(WebResponseStatus.RESOURCE_EXIST.value(), e.getLocalizedMessage());
    }
    
    /**  
     * 用于在程序发生{@link IllegalParamException}异常时统一返回错误信息 
     * @return  
     */  
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({IllegalParamException.class})   
    public HttpStatusResponse illegalParamException(IllegalParamException e) {
        e.printStackTrace();
        return new HttpStatusResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }
    
    /**  
     * 用于在程序发生{@link Exception}异常时统一返回错误信息 
     * @return  
     */  
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({Exception.class})   
    public HttpStatusResponse exception(Exception e) {
        e.printStackTrace();
        return new HttpStatusResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getLocalizedMessage());
    }
}
