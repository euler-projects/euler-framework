package net.eulerform.web.core.base.controller;

import net.eulerform.web.core.annotation.RestEndpoint;
import net.eulerform.web.core.base.entity.WebServiceResponse;

import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestEndpoint
@Scope("prototype")
@RequestMapping("/")
public class DefaultRestEndpoint extends BaseRest {

    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @RequestMapping(value = { "**" })
    public WebServiceResponse<String> defaultRequest() {
        return new WebServiceResponse<String>(HttpStatus.NOT_FOUND);
    }
}
