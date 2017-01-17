package net.eulerframework.web.module.basic.controller.admin;

import net.eulerframework.web.core.base.controller.AbstractWebController;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import net.eulerframework.web.core.annotation.AdminWebController;

@AdminWebController
@Scope("prototype")
@RequestMapping("/")
public class IndexWebController extends AbstractWebController {
    
    @RequestMapping(value={"", "/", "index"}, method = RequestMethod.GET)
    public String index() {
        return this.display("index");
    }
}
