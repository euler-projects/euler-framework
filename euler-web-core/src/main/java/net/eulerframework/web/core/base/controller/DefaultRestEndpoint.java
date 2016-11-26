package net.eulerframework.web.core.base.controller;

import net.eulerframework.web.core.base.exception.ResourceNotFoundException;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.eulerframework.web.core.annotation.RestEndpoint;
import net.eulerframework.web.core.base.response.WebServiceResponse;

@RestEndpoint
@Scope("prototype")
@RequestMapping("/")
public class DefaultRestEndpoint extends AbstractRestEndpoint {

    @ResponseBody
    @RequestMapping(value = { "**" })
    public WebServiceResponse<String> defaultRequest() {
        throw new ResourceNotFoundException();
    }
}