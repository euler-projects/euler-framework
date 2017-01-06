package net.eulerframework.web.module.basic.controller;

import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import net.eulerframework.web.core.annotation.WebController;
import net.eulerframework.web.core.base.controller.AbstractWebController;

@WebController
@Scope("prototype")
@RequestMapping("/")
public class ErrorWebController extends AbstractWebController {
    
    @RequestMapping("error-{errorCode}")
    public String error(@PathVariable("errorCode") String errorCode) {
        return this.display(errorCode);
    }
    
}
