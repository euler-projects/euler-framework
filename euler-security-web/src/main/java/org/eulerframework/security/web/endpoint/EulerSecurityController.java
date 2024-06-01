package org.eulerframework.security.web.endpoint;

import org.springframework.web.servlet.resource.NoResourceFoundException;

public interface EulerSecurityController {
    String signupPage() throws NoResourceFoundException;

    String loginPage();

    String logoutPage();
}
