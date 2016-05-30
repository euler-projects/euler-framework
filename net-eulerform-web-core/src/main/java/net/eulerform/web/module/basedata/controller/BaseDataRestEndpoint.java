package net.eulerform.web.module.basedata.controller;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import net.eulerform.web.core.annotation.RestEndpoint;
import net.eulerform.web.core.base.controller.BaseRest;
import net.eulerform.web.core.base.entity.WebServiceResponse;
import net.eulerform.web.module.basedata.entity.CodeTable;
import net.eulerform.web.module.basedata.service.ICodeTableService;

@RestEndpoint
@Scope("prototype")
@RequestMapping("/baseData")
public class BaseDataRestEndpoint extends BaseRest {
    
    @Resource
    private ICodeTableService codeTableService;
    
    @ResponseBody
    @RequestMapping(value ="/saveCodeTable", method = RequestMethod.POST)
    public WebServiceResponse<String> saveCodeTable(CodeTable codeTable) {
        this.codeTableService.saveCodeTable(codeTable);
        return new WebServiceResponse<String>(HttpStatus.OK);
    }
    
}
