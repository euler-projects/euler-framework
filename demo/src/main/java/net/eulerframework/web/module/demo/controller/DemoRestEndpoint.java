package net.eulerframework.web.module.demo.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;

import net.eulerframework.web.core.annotation.RestEndpoint;
import net.eulerframework.web.core.base.controller.AbstractRestEndpoint;
import net.eulerframework.web.core.base.request.PageQueryRequest;
import net.eulerframework.web.core.base.response.WebServiceResponse;

@RestEndpoint
@RequestMapping("/demo")
public class DemoRestEndpoint extends AbstractRestEndpoint {

    @RequestMapping("/demo")
    public WebServiceResponse<Long> demo(HttpServletRequest request) {
        PageQueryRequest pr = new PageQueryRequest(request);
        System.out.println(pr.getOrderMode("a"));
        return new WebServiceResponse<>(100L);
    }
}
