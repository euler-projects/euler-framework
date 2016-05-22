package net.eulerform.web.core.base.controller.rest;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import net.eulerform.web.core.base.entity.RestResponseEntity;
import net.eulerform.web.core.base.entity.RestResponseStatus;

@Controller
@Scope("prototype")
@RequestMapping("/")
public class ServiceNotFoundRest extends BaseRest {
	
    @ResponseBody
    @RequestMapping(value={"**"},method=RequestMethod.GET)
    public RestResponseEntity<String> urlNotFound() {
    	return new RestResponseEntity<String>(RestResponseStatus.SERVICE_NOT_FOUND);
    }
    
}
