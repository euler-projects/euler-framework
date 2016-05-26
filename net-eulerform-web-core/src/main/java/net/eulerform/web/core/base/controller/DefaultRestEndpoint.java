package net.eulerform.web.core.base.controller;

import net.eulerform.web.core.annotation.RestEndpoint;
import net.eulerform.web.core.base.entity.WebServiceResponse;
import net.eulerform.web.core.base.entity.WebServiceResponseStatus;

import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestEndpoint
@Scope("prototype")
@RequestMapping("/")
public class DefaultRestEndpoint extends BaseRest {

    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @RequestMapping(value = { "**" }, method = RequestMethod.GET)
    public WebServiceResponse<String> defaultRequest() {
        return new WebServiceResponse<String>(WebServiceResponseStatus.RESOURCE_NOT_FOUND);
    }
}
