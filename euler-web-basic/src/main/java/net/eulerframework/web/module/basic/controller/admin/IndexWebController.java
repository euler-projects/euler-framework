package net.eulerframework.web.module.basic.controller.admin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.NoHandlerFoundException;

import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.annotation.AdminWebController;
import net.eulerframework.web.core.base.controller.JspSupportWebController;
import net.eulerframework.web.core.exception.web.PageNotFoundException;

@AdminWebController
@RequestMapping("/")
public class IndexWebController extends JspSupportWebController {
    
    @RequestMapping(value={"","index"}, method = RequestMethod.GET)
    public String admin() {
        return this.display("index");
    }
    
    /**
     * 捕获所有未定义的请求，只有满足[contextPaht]/AdminRootPath才会被重定向到[contextPaht]/AdminRootPath/，其他请求返回404
     * @return 后台管理首页
     * @throws NoHandlerFoundException
     */
    @RequestMapping(value={"**"}, method = RequestMethod.GET)
    public String index() throws NoHandlerFoundException {
        if(this.getRequest().getRequestURI().replace(this.getRequest().getContextPath(), "").equals(WebConfig.getAdminRootPath()))
            return this.redirect(WebConfig.getAdminRootPath() + "/");
        throw new PageNotFoundException(this.getRequest());
    }
}
