package net.eulerform.web.module.cms.basic.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import net.eulerform.web.core.annotation.RestEndpoint;
import net.eulerform.web.core.base.controller.BaseRest;
import net.eulerform.web.core.base.entity.PageResponse;
import net.eulerform.web.core.base.entity.QueryRequest;
import net.eulerform.web.module.cms.basic.entity.ListResponse;
import net.eulerform.web.module.cms.basic.entity.News;
import net.eulerform.web.module.cms.basic.entity.Partner;
import net.eulerform.web.module.cms.basic.entity.Slideshow;
import net.eulerform.web.module.cms.basic.service.INewsService;
import net.eulerform.web.module.cms.basic.service.IPartnerService;
import net.eulerform.web.module.cms.basic.service.ISlideshowService;

@RestEndpoint
@Scope("prototype")
@RequestMapping("/cms")
public class CmsRestEndpoint extends BaseRest {

    @Resource IPartnerService partnerService;
    @Resource ISlideshowService slideshowService;
    @Resource INewsService newsService;
    
    @ResponseBody
    @RequestMapping(value ="/findPartnerByPage")
    public PageResponse<Partner> findPartnerByPage(HttpServletRequest request, String page, String rows) {
        QueryRequest queryRequest = new QueryRequest(request);
        
        int pageIndex = Integer.parseInt(page);
        int pageSize = Integer.parseInt(rows);
        return this.partnerService.findPartnerByPage(queryRequest, pageIndex, pageSize);
    }
    
    @ResponseBody
    @RequestMapping(value ="/loadPartners")
    public ListResponse<Partner> loadPartners() {
        return new ListResponse<>(this.partnerService.loadPartners());
    }

    @ResponseBody
    @RequestMapping(value ="/findNewsByPage")
    public PageResponse<News> findNewsByPage(HttpServletRequest request, String page, String rows, boolean loadText, boolean enableTop) {
        QueryRequest queryRequest = new QueryRequest(request);
        
        int pageIndex = Integer.parseInt(page);
        int pageSize = Integer.parseInt(rows);
        return this.newsService.findNewsByPage(queryRequest, pageIndex, pageSize, loadText, enableTop);
    }
    
    @ResponseBody
    @RequestMapping(value = "/loadSlideshow", method = RequestMethod.GET)
    public ListResponse<Slideshow> loadSlideshow() {
        return new ListResponse<>(this.slideshowService.loadSlideshow());
    }
}
