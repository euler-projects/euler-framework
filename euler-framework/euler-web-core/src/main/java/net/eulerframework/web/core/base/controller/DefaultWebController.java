package net.eulerframework.web.core.base.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import net.eulerframework.web.util.UrlTool;

@Deprecated
public abstract class DefaultWebController extends AbstractWebController {

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
}
