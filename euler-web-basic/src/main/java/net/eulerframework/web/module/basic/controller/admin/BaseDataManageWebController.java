package net.eulerframework.web.module.basic.controller.admin;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import net.eulerframework.web.core.annotation.WebController;
import net.eulerframework.web.core.base.controller.AbstractWebController;
import net.eulerframework.web.core.base.request.QueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.module.basic.entity.CodeTable;
import net.eulerframework.web.module.basic.entity.Module;
import net.eulerframework.web.module.basic.entity.Page;
import net.eulerframework.web.module.basic.service.IBaseDataService;

@WebController
@Scope("prototype")
@RequestMapping("/manage/basedata")
public class BaseDataManageWebController extends AbstractWebController {
    
    @Resource
    private IBaseDataService baseDataService;
    
    @RequestMapping(value ="/codeTable",method=RequestMethod.GET)
    public String codeTable(){
        return "/manage/basedata/codeTable";
    }
    
    @RequestMapping(value ="/module",method=RequestMethod.GET)
    public String module(){
        return "/manage/basedata/module";
    }

    @RequestMapping(value = "/emailConfig", method = RequestMethod.GET)
    public String emailConfig() {
        return "/manage/basedata/emailConfig";
    }
    
    @ResponseBody
    @RequestMapping(value ="/findCodeTableByPage")
    public PageResponse<CodeTable> findCodeTableByPage(HttpServletRequest request, String page, String rows) {
        QueryRequest queryRequest = new QueryRequest(request);
        
        int pageIndex = Integer.parseInt(page);
        int pageSize = Integer.parseInt(rows);
        return this.baseDataService.findCodeTableByPage(queryRequest, pageIndex, pageSize);
    }
    
    @ResponseBody
    @RequestMapping(value ="/findAllModules")
    public List<Module> findAllModules() {
        return this.baseDataService.findAllModuleFromDB();
    }
    
    @ResponseBody
    @RequestMapping(value ="/findModuleProperties/{id}")
    public Module findModuleProperties(@PathVariable("id") String id) {
        return this.baseDataService.findModuleById(id);
    }
    
    @ResponseBody
    @RequestMapping(value ="/findPageProperties/{id}")
    public Page findPageProperties(@PathVariable("id") String id) {
        return this.baseDataService.findPageById(id);
    }
    
    @ResponseBody
    @RequestMapping(value ="/saveCodeTable", method = RequestMethod.POST)
    public void saveCodeTable(@Valid CodeTable codeTable) {
        this.baseDataService.saveCodeTable(codeTable);
    }
    
    @ResponseBody
    @RequestMapping(value ="/saveModule", method = RequestMethod.POST)
    public void saveModule(@Valid Module module) {
        this.baseDataService.saveModule(module);
    }
    
    @ResponseBody
    @RequestMapping(value ="/savePage", method = RequestMethod.POST)
    public void savePage(@Valid Page page) {
        this.baseDataService.savePage(page);
    }
    
    @ResponseBody
    @RequestMapping(value ="/deleteCodeTables", method = RequestMethod.POST)
    public void deleteCodeTables(@RequestParam String ids) {
        String[] idArray = ids.trim().replace(" ", "").split(";");
        this.baseDataService.deleteCodeTables(idArray);
    }
    
    @ResponseBody
    @RequestMapping(value ="/deleteModule", method = RequestMethod.POST)
    public void deleteModule(@RequestParam String id) {
        this.baseDataService.deleteModule(id);
    }
    
    @ResponseBody
    @RequestMapping(value ="/deletePage", method = RequestMethod.POST)
    public void deletePage(@RequestParam String id) {
        this.baseDataService.deletePage(id);
    }
    
}
