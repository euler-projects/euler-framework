package net.eulerform.web.core.base.rest;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import net.eulerform.web.core.base.entity.RetResult;
import net.eulerform.web.core.base.entity.RetStatus;

@Controller
@Scope("prototype")
@RequestMapping("/")
public class RootRest extends BaseRest {
	
    @ResponseBody
    @RequestMapping(value={"**"},method=RequestMethod.GET)
    public RetResult<String> urlNotFound() {
    	return new RetResult<String>(RetStatus.RESROURCE_NOT_FOUND);
    }
    
}
