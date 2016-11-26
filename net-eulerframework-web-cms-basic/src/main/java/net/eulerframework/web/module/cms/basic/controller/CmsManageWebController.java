package net.eulerframework.web.module.cms.basic.controller;

import java.text.ParseException;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import net.eulerframework.web.core.base.controller.DefaultWebController;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.core.exception.MultipartFileSaveException;
import net.eulerframework.web.module.cms.basic.entity.Partner;
import net.eulerframework.web.module.cms.basic.service.INewsService;
import net.eulerframework.web.module.cms.basic.service.IPartnerService;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import net.eulerframework.common.util.CalendarTool;
import net.eulerframework.common.util.StringTool;
import net.eulerframework.web.core.annotation.WebController;
import net.eulerframework.web.core.base.request.QueryRequest;
import net.eulerframework.web.module.cms.basic.entity.News;

@WebController
@Scope("prototype")
@RequestMapping("/manage/cms")
public class CmsManageWebController extends DefaultWebController {

    @Resource
    IPartnerService partnerService;
    @Resource
    INewsService newsService;
    
    @RequestMapping(value = "/news", method = RequestMethod.GET)
    public String news() {
        return "/manage/cms/news";
    }
    
    @RequestMapping(value = "/partner", method = RequestMethod.GET)
    public String partner() {
        return "/manage/cms/partner";
    }
    
    @RequestMapping(value = "/ueditor", method = RequestMethod.GET)
    public String ueditor() {
        return "/manage/cms/ueditor";
    }
    
    @ResponseBody
    @RequestMapping(value ="/findPartnerByPage")
    public PageResponse<Partner> findPartnerByPage(HttpServletRequest request, String page, String rows) {
        QueryRequest queryRequest = new QueryRequest(request);
        
        int pageIndex = Integer.parseInt(page);
        int pageSize = Integer.parseInt(rows);
        return this.partnerService.findPartnerByPage(queryRequest, pageIndex, pageSize);
    }

    @ResponseBody
    @RequestMapping(value ="/findNewsByPage")
    public PageResponse<News> findNewsByPage(HttpServletRequest request, String page, String rows) {
        QueryRequest queryRequest = new QueryRequest(request);
        
        int pageIndex = Integer.parseInt(page);
        int pageSize = Integer.parseInt(rows);
        return this.newsService.findNewsByPage(queryRequest, pageIndex, pageSize, true, true);
    }
    
    @ResponseBody
    @RequestMapping(value = "/savePartner", method = RequestMethod.POST)
    public void partner(@RequestParam(value = "logo", required = false) MultipartFile logo, @Valid Partner partner) throws MultipartFileSaveException {
        
        if(!StringTool.isNull(partner.getUrl())) {
            String url = partner.getUrl();
            
            if(url.indexOf("://") < 0) {
                url = "http://" + url;
                partner.setUrl(url);
            }
        }
        
        if(partner.getShow() == null)
            partner.setShow(false);
        
        this.partnerService.savePartner(partner, logo);
    }

    @ResponseBody
    @RequestMapping(value = "/saveNews", method = RequestMethod.POST)
    public void saveNews(
            @RequestParam(value = "img", required = false) MultipartFile img,
            @RequestParam(value = "pubDateStr", required = true) String pubDateStr,
            @Valid News news) throws MultipartFileSaveException, ParseException {
        
        news.setPubDate(CalendarTool.parseDate(pubDateStr, "yyyy-MM-dd HH:mm:ss"));
        this.newsService.saveNews(news, img);
    }
    
    @ResponseBody
    @RequestMapping(value = "/deletePartners", method = RequestMethod.POST)
    public void deletePartners(@RequestParam String ids) {
        String[] idArray = ids.trim().replace(" ", "").split(";");
        this.partnerService.deletePartners(idArray);
    }
    
    @ResponseBody
    @RequestMapping(value = "/deleteNews", method = RequestMethod.POST)
    public void deleteNews(@RequestParam String ids) {
        String[] idArray = ids.trim().replace(" ", "").split(";");
        this.newsService.deleteNews(idArray);
    }
    
    @ResponseBody
    @RequestMapping(value ="/findPartnerAll", method = RequestMethod.GET)
    public List<Partner> findPartnerAll() {
        return this.partnerService.loadPartners(false);
    }
}
