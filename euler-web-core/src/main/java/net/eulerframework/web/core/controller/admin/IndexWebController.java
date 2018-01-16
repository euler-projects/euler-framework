package net.eulerframework.web.core.controller.admin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import net.eulerframework.web.core.annotation.WebController;
import net.eulerframework.web.core.base.controller.JspSupportWebController;

@WebController
@RequestMapping("/")
public class IndexWebController extends JspSupportWebController {
    
    @RequestMapping(value={""}, method = RequestMethod.GET)
    public String index() {
        return this.display("index");
    }
}
