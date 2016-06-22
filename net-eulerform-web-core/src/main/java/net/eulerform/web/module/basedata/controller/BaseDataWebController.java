package net.eulerform.web.module.basedata.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import net.eulerform.web.core.annotation.WebController;
import net.eulerform.web.core.base.controller.BaseController;
import net.eulerform.web.core.base.entity.QueryRequest;
import net.eulerform.web.core.base.entity.PageResponse;
import net.eulerform.web.module.basedata.entity.CodeTable;
import net.eulerform.web.module.basedata.entity.Module;
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
    
    @RequestMapping(value ="/page",method=RequestMethod.GET)
    public String page(){
        return "/basedata/page";
    }
    
    @ResponseBody
    @RequestMapping(value ="/findModuleByPage")
    public PageResponse<Module> findModuleByPage(HttpServletRequest request, String page, String rows) {
        QueryRequest queryRequest = new QueryRequest(request);
        
        int pageIndex = Integer.parseInt(page);
        int pageSize = Integer.parseInt(rows);
        return this.baseDataService.findModuleByPage(queryRequest, pageIndex, pageSize);
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
    @RequestMapping(value ="/saveCodeTable", method = RequestMethod.POST)
    public void saveCodeTable(CodeTable codeTable) {
        this.baseDataService.saveCodeTable(codeTable);
    }
    
    @ResponseBody
    @RequestMapping(value ="/delCodeTablesByIds", method = RequestMethod.POST)
    public void delCodeTablesByIds(String ids) {
        String[] idArray = ids.trim().replace(" ", "").split(",");
        this.baseDataService.deleteCodeTables(idArray);
    }
    
    @ResponseBody
    @RequestMapping(value ="/delCodeTableById", method = RequestMethod.POST)
    public void delCodeTableById(String id) {
        this.baseDataService.deleteCodeTable(id);
    }
    
}
