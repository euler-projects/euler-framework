package net.eulerform.web.core.base.controller;

import net.eulerform.web.core.annotation.RestEndpoint;
import net.eulerform.web.core.base.entity.WebServiceResponse;
import net.eulerform.web.core.base.entity.WebServiceResponseStatus;

import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@RestEndpoint
@Scope("prototype")
@RequestMapping("/")
public class ServiceNotFoundRest extends BaseRest {
	
    @ResponseBody
    @RequestMapping(value={"**"},method=RequestMethod.GET)
    public WebServiceResponse<String> urlNotFound() {
    	return new WebServiceResponse<String>(WebServiceResponseStatus.SERVICE_NOT_FOUND);
    }
    
}
