package net.eulerframework.web.module.demo.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;

import net.eulerframework.web.core.annotation.RestEndpoint;
import net.eulerframework.web.core.base.controller.AbstractRestEndpoint;
import net.eulerframework.web.core.base.request.QueryRequest;

@RestEndpoint
@RequestMapping("/demo")
public class DemoRestEndpoint extends AbstractRestEndpoint {

    @RequestMapping("/demo")
    public String demo(HttpServletRequest request) {
        QueryRequest pr = new QueryRequest(request);
        System.out.println(pr.getOrderMode("a"));
        return "ININININ";
    }
}
