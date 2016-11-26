package net.eulerframework.web.module.cms.basic.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import net.eulerframework.web.core.annotation.RestEndpoint;
import net.eulerframework.web.core.base.controller.AbstractRestEndpoint;
import net.eulerframework.web.core.base.response.WebServiceResponse;
import net.eulerframework.web.module.cms.basic.entity.Partner;
import net.eulerframework.web.module.cms.basic.service.INewsService;
import net.eulerframework.web.module.cms.basic.service.IPartnerService;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import net.eulerframework.web.core.base.request.QueryRequest;
import net.eulerframework.web.core.base.response.WebServicePageResponse;
import net.eulerframework.web.module.cms.basic.entity.News;

@RestEndpoint
@Scope("prototype")
@RequestMapping("/cms")
public class CmsRestEndpoint extends AbstractRestEndpoint {

    @Resource
    IPartnerService partnerService;
    @Resource
    INewsService newsService;
    
    @ResponseBody
    @RequestMapping(value ="/findPartnerByPage", method = RequestMethod.GET)
    public WebServicePageResponse<Partner> findPartnerByPage(HttpServletRequest request, int pageIndex, int pageSize) {
        QueryRequest queryRequest = new QueryRequest(request);
        return new WebServicePageResponse<>(this.partnerService.findPartnerByPage(queryRequest, pageIndex, pageSize));
    }
    
    @ResponseBody
    @RequestMapping(value ="/findPartnerAll", method = RequestMethod.GET)
    public WebServiceResponse<Partner> findPartnerAll(boolean onlyShow) {
        return new WebServiceResponse<>(this.partnerService.loadPartners(onlyShow));
    }

    @ResponseBody
    @RequestMapping(value ="/findNewsByPage", method = RequestMethod.GET)
    public WebServicePageResponse<News> findNewsByPage(HttpServletRequest request, int pageIndex, int pageSize, boolean loadText, boolean enableTop) {
        QueryRequest queryRequest = new QueryRequest(request);
        
        return new WebServicePageResponse<>(this.newsService.findNewsByPage(queryRequest, pageIndex, pageSize, loadText, enableTop));
    }

    @ResponseBody
    @RequestMapping(value ="/news/{newId}", method = RequestMethod.GET)
    public WebServiceResponse<News> findNews(@PathVariable("newId") String newsId) {        
        return new WebServiceResponse<>(this.newsService.findNews(newsId));
    }
}
