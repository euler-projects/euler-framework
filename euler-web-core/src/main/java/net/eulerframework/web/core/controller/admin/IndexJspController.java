package net.eulerframework.web.core.controller.admin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.annotation.JspController;
import net.eulerframework.web.core.base.controller.JspSupportWebController;
import net.eulerframework.web.core.exception.PageNotFoundException;

@JspController
@RequestMapping("/")
public class IndexJspController extends JspSupportWebController {
    
    @RequestMapping(value={""}, method = RequestMethod.GET)
    public String index() {
        return this.display("index");
    }
    
    /**
     * 捕获所有未定义的请求，只有满足[contextPaht]/AdminRootPath才会被重定向到[contextPaht]/AdminRootPath/，其他请求返回404
     * @return 后台管理首页
     */
    @RequestMapping(value={"**"}, method = RequestMethod.GET)
    public String adminRedirect() {
        if(this.getRequest().getRequestURI().replace(this.getRequest().getContextPath(), "").equals(WebConfig.getAdminRootPath()))
            return this.redirect(WebConfig.getAdminRootPath() + "/");
        throw new PageNotFoundException(this.getRequest());
    }
}
