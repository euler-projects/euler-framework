package net.eulerform.web.core.util;

import javax.servlet.http.HttpServletRequest;

public class UrlTool {
    public static String findModuleName(HttpServletRequest httpServletRequest, String[] webFolders){
        
        //去掉项目名称后的URI
        String realURI = UrlTool.findRealURI(httpServletRequest);
        
        //模块名称 /xxx/yyy/zzz中的xxx
        
        String moduleName = null;
        
        String URI = realURI;
        //  /xxx/yyy/zzz to xxx/yyy/zzz
        if(URI.startsWith("/"))
            URI = URI.replaceFirst("/", "");
        
        // webFolders/xxx/yyy to xxx/yyy
        for(String webFolder : webFolders){
            
            if(webFolder.endsWith("/*")){
                webFolder = webFolder.substring(0, webFolder.length()-2);
                if(URI.startsWith(webFolder)){
                    URI = "";
                    break;
                }else{
                    continue;
                }
            }else if(webFolder.endsWith("/")){
                webFolder = webFolder.substring(0, webFolder.length()-1);
            }
            
            if(URI.startsWith(webFolder)) {
                URI = URI.replaceFirst(webFolder+"/", "");
                break;
            }
        }
        
        if(URI.indexOf("/") < 0)//like xxx or xxx.aaa return /
            moduleName = "/";
        else // like xxx/ or xxx/yyy or xxx/yyy.aaa or xxx/yyy/zzz ... return xxx
            moduleName = URI.substring(0, URI.indexOf("/"));

//        System.out.println("realURI:"+realURI);
//        System.out.println("moduleName:"+moduleName);
        
        return moduleName;
    }
    
    public static String findRealURI(HttpServletRequest httpServletRequest) {        
        String requestURI = httpServletRequest.getRequestURI();
        String contextPath = httpServletRequest.getContextPath();
        
//      System.out.println("requestURI:"+requestURI);
//      System.out.println("contextPath:"+contextPath);
        
        return requestURI.replaceFirst(contextPath, "").trim();
    }
}
