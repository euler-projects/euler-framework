package net.eulerform.web.core.base.controller;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.eulerform.web.core.util.UrlTool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

public abstract class BaseController {
    
    protected final Logger log = LogManager.getLogger();
    
    protected ServletContext getServletContext(){
        WebApplicationContext webApplicationContext = ContextLoader.getCurrentWebApplicationContext();  
        return webApplicationContext.getServletContext();
    }
    
    protected void writeString(HttpServletResponse httpServletResponse, String str) throws IOException{
        httpServletResponse.getOutputStream().write(str.getBytes("UTF-8"));
    }

    @RequestMapping(value={"/", "index"},method=RequestMethod.GET)
    public String index(HttpServletRequest request, Model model) {
        String moduleName = this.findModuleName(request);
        String pagePath = moduleName+"/index";
        this.log.info("Redirect to module index page: "+pagePath);
        //request.setAttribute("user", SecurityContextTool.getCurrentUser());
        //model.addAttribute("user", SecurityContextTool.getCurrentUser());
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
}
