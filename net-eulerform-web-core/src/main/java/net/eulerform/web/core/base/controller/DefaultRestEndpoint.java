package net.eulerform.web.core.base.controller;

import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.eulerform.web.core.annotation.RestEndpoint;
import net.eulerform.web.core.base.controller.AbstractRestEndpoint;
import net.eulerform.web.core.base.exception.ResourceNotFoundException;
import net.eulerform.web.core.base.response.WebServiceResponse;

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
