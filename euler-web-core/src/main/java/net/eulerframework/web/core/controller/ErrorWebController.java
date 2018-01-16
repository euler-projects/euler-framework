package net.eulerframework.web.core.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import net.eulerframework.web.core.annotation.JspController;
import net.eulerframework.web.core.base.controller.JspSupportWebController;

@JspController
@RequestMapping("/")
public class ErrorWebController extends JspSupportWebController {
    
    @RequestMapping("error-{errorCode}")
    public String error(@PathVariable("errorCode") String errorCode) {
        return this.display(errorCode);
    }
    
}
