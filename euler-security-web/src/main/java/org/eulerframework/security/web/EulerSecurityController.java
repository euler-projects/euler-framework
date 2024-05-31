package org.eulerframework.security.web;

import org.springframework.web.bind.annotation.GetMapping;

public interface EulerSecurityController {
    String loginPage();

    @GetMapping("${euler.security.web.login-page:/login}")
    String logoutPage();
}
