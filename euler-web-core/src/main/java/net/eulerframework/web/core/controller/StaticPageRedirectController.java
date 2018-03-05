package net.eulerframework.web.core.controller;

import java.util.Locale;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import net.eulerframework.common.util.CommonUtils;
import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.annotation.JspController;
import net.eulerframework.web.core.base.controller.JspSupportWebController;

@JspController
@RequestMapping("/")
public class StaticPageRedirectController extends JspSupportWebController {
    
    @RequestMapping("h/{url}")
    public String error(@PathVariable("url") String url, RedirectAttributes redirectAttributes, Locale locale) {
        
        redirectAttributes.addAllAttributes(this.getRequest().getParameterMap());
        
        return this.redirect(WebConfig.getStaticPagesRootPath() + "/" + CommonUtils.formatLocal(locale, '-') + "/" + url);
    }
    
}
