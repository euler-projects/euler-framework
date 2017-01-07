package net.eulerframework.web.core.base.controller;

import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.eulerframework.web.core.annotation.ApiEndpoint;
import net.eulerframework.web.core.base.response.WebServiceResponse;
import net.eulerframework.web.core.exception.ResourceNotFoundException;

@ApiEndpoint
@Scope("prototype")
@RequestMapping("/")
@Deprecated
public abstract class DefaultRestEndpoint extends AbstractApiEndpoint {

    @ResponseBody
    @RequestMapping(value = { "**" })
    public WebServiceResponse<String> defaultRequest() {
        throw new ResourceNotFoundException();
    }
}
