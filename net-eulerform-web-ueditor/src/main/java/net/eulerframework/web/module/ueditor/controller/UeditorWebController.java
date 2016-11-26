package net.eulerframework.web.module.ueditor.controller;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import net.eulerframework.web.core.base.controller.DefaultWebController;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import net.eulerframework.web.core.annotation.WebController;
import net.eulerframework.web.module.ueditor.service.IUeService;

@WebController
@Scope("prototype")
@RequestMapping("/ueditor")
public class UeditorWebController extends DefaultWebController {
    
    @Resource private IUeService ueService;

    @ResponseBody
    @RequestMapping(value ="/controller", method=RequestMethod.GET)
    public Object controllerGet(String action, HttpServletRequest request) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Class<? extends IUeService> clazz = this.ueService.getClass();
        Method m = clazz.getDeclaredMethod(action, HttpServletRequest.class);
        return m.invoke(this.ueService, request);
    }
    
    @ResponseBody
    @RequestMapping(value ="/controller", method=RequestMethod.POST)
    public Object controllerPost(String action, HttpServletRequest request, MultipartFile file) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Class<? extends IUeService> clazz = this.ueService.getClass();
        Method m = clazz.getDeclaredMethod(action, HttpServletRequest.class, MultipartFile.class);
        return m.invoke(this.ueService, request, file);
    }
}
