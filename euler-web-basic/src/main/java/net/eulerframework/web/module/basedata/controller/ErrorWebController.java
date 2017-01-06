package net.eulerframework.web.module.basedata.controller;

import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import net.eulerframework.web.core.annotation.WebController;
import net.eulerframework.web.core.base.controller.AbstractWebController;

@WebController
@Scope("prototype")
@RequestMapping("/")
public class ErrorWebController extends AbstractWebController {
    
    @RequestMapping(value="error-{errorCode}", method = RequestMethod.GET)
    public String error(@PathVariable("errorCode") String errorCode) {
        return this.display(errorCode);
    }
    
}
