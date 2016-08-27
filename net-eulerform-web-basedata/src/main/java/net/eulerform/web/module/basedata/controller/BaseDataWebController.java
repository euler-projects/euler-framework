package net.eulerform.web.module.basedata.controller;

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

import net.eulerform.web.core.annotation.WebController;
import net.eulerform.web.core.base.controller.BaseController;
import net.eulerform.web.core.base.request.QueryRequest;
import net.eulerform.web.core.base.response.PageResponse;
import net.eulerform.web.module.basedata.entity.CodeTable;
import net.eulerform.web.module.basedata.entity.Module;
import net.eulerform.web.module.basedata.entity.Page;
import net.eulerform.web.module.basedata.service.IBaseDataService;

@WebController
@Scope("prototype")
@RequestMapping("/basedata")
public class BaseDataWebController extends BaseController {
    
    @Resource
    private IBaseDataService baseDataService;
    
    @RequestMapping(value ="/codeTable",method=RequestMethod.GET)
    public String codeTable(){
        return "/basedata/codeTable";
    }
    
    @RequestMapping(value ="/module",method=RequestMethod.GET)
    public String module(){
        return "/basedata/module";
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
