package net.eulerform.web.module.root.controller.web;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import net.eulerform.web.core.base.controller.web.BaseController;

@Controller
@Scope("prototype")
@RequestMapping("/")
public class RootController extends BaseController {
    
    @RequestMapping(value = "/about", method = RequestMethod.GET)
    public String about() {
        return "/about/index";
    }

    @RequestMapping(value = { "/login" }, method = RequestMethod.GET)
    public String login()
    {
        return "/root/login";
    }
}
