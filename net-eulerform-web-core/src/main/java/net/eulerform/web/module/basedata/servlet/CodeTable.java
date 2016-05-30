package net.eulerform.web.module.basedata.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import net.eulerform.web.module.basedata.service.IBaseDataService;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

@SuppressWarnings("serial")
public class CodeTable extends HttpServlet {
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        
        ServletContext servletContext = getServletContext();
        WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);  
        IBaseDataService baseDataService = (IBaseDataService) applicationContext.getBean ("baseDataService");
        
        System.out.println("!@#$$%%");
        try {
            
            baseDataService.createCodeDict();
        } catch (IOException e) {
            throw new ServletException(e);
        }
    }
}
