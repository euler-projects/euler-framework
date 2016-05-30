package net.eulerform.web.module.basedata.controller;

import java.io.IOException;

import javax.annotation.Resource;

import net.eulerform.web.core.annotation.RestEndpoint;
import net.eulerform.web.core.base.controller.BaseRest;
import net.eulerform.web.core.base.entity.WebServiceResponse;
import net.eulerform.web.module.basedata.service.IBaseDataService;

import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@RestEndpoint
@Scope("prototype")
@RequestMapping("/baseData")
public class BaseDataRestEndpoint extends BaseRest {
    
    @Resource
    private IBaseDataService baseDataService;
    
    @ResponseBody
    @RequestMapping(value ="/codeTable", method = RequestMethod.GET)
    public WebServiceResponse<String> codeTable() throws IOException {
        System.out.println("createCodeDictEndpoint");
        this.baseDataService.createCodeDict();
        return new WebServiceResponse<String>(HttpStatus.OK);
    }
    
}
