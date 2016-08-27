package net.eulerform.web.module.cms.basic.controller;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import net.eulerform.common.BeanTool;
import net.eulerform.common.CalendarTool;
import net.eulerform.common.StringTool;
import net.eulerform.web.core.annotation.WebController;
import net.eulerform.web.core.base.controller.BaseController;
import net.eulerform.web.core.base.request.QueryRequest;
import net.eulerform.web.core.base.response.PageResponse;
import net.eulerform.web.core.exception.MultipartFileSaveException;
import net.eulerform.web.core.util.WebFileTool;
import net.eulerform.web.module.cms.basic.entity.ListResponse;
import net.eulerform.web.module.cms.basic.entity.News;
import net.eulerform.web.module.cms.basic.entity.Partner;
import net.eulerform.web.module.cms.basic.entity.Slideshow;
import net.eulerform.web.module.cms.basic.service.INewsService;
import net.eulerform.web.module.cms.basic.service.IPartnerService;
import net.eulerform.web.module.cms.basic.service.ISlideshowService;

@WebController
@Scope("prototype")
@RequestMapping("/cms/manage")
public class CmsManageWebController extends BaseController {

    @Resource IPartnerService partnerService;
    @Resource ISlideshowService slideshowService;
    @Resource INewsService newsService;
    
    @RequestMapping(value = "/news", method = RequestMethod.GET)
    public String news() {
        return "/cms/manage/news";
    }
    
    @RequestMapping(value = "/partner", method = RequestMethod.GET)
    public String partner() {
        return "/cms/manage/partner";
    }
    
    @RequestMapping(value = "/slideshow", method = RequestMethod.GET)
    public String slideshow() {
        return "/cms/manage/slideshow";
    }
    
    @RequestMapping(value = "/ueditor", method = RequestMethod.GET)
    public String ueditor() {
        return "/cms/manage/ueditor";
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
        BeanTool.clearEmptyProperty(partner);
        if(logo != null && logo.getSize() > 0){
            if(partner.getId() != null) {
                this.partnerService.deleteLogo(partner.getId() );
            }
            File savedFile = WebFileTool.saveMultipartFile(logo);
            partner.setLogoFileName(savedFile.getName());            
        }
        
        if(!StringTool.isNull(partner.getUrl())) {
            String url = partner.getUrl();
            
            if(url.indexOf("://") < 0) {
                url = "http://" + url;
                partner.setUrl(url);
            }
        }
        this.partnerService.savePartner(partner);
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
    @RequestMapping(value = "/loadSlideshow", method = RequestMethod.GET)
    public ListResponse<Slideshow> loadSlideshow() {
        return new ListResponse<>(this.slideshowService.loadSlideshow());
    }
    
    @ResponseBody
    @RequestMapping(value = "/saveSlideshow", method = RequestMethod.POST)
    public void saveSlideshow(
            @RequestParam(value = "img1", required = false) MultipartFile img1,
            @RequestParam(value = "img2", required = false) MultipartFile img2,
            @RequestParam(value = "img3", required = false) MultipartFile img3,
            @RequestParam(value = "img4", required = false) MultipartFile img4,
            @RequestParam(value = "url1", required = false) String url1,
            @RequestParam(value = "url2", required = false) String url2,
            @RequestParam(value = "url3", required = false) String url3,
            @RequestParam(value = "url4", required = false) String url4
        ) {
        List<MultipartFile> img = new ArrayList<>();
        List<String> url = new ArrayList<>();
        img.add(img1);
        img.add(img2);
        img.add(img3);
        img.add(img4);
        url.add(url1);
        url.add(url2);
        url.add(url3);
        url.add(url4);
        this.slideshowService.saveSlideshow(img, url);
    }
}
