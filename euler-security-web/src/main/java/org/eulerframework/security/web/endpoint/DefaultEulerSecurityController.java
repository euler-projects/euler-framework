package org.eulerframework.security.web.endpoint;

import org.eulerframework.web.core.base.controller.ThymeleafSupportWebController;
import org.eulerframework.web.util.ServletUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Controller
public class DefaultEulerSecurityController extends ThymeleafSupportWebController implements EulerSecurityController {
    private boolean signupEnabled;
    private String signupProcessingUrl;
    private String loginProcessingUrl;
    private String logoutProcessingUrl;

    @Override
    @GetMapping("${" + EulerSecurityEndpoints.SIGNUP_PAGE_PROPERTY_NAME + ":" + EulerSecurityEndpoints.SIGNUP_PAGE + "}")
    public String signupPage() throws NoResourceFoundException {
        if(!this.signupEnabled) {
            throw new NoResourceFoundException(HttpMethod.GET, ServletUtils.findRealURI(this.getRequest()));
        }
        return "euler/security/web/signup";
    }

    @Override
    @GetMapping("${" + EulerSecurityEndpoints.LOGIN_PAGE_PROPERTY_NAME + ":" + EulerSecurityEndpoints.LOGIN_PAGE + "}")
    public String loginPage() {
        return "euler/security/web/login";
    }

    @Override
    @GetMapping("${" + EulerSecurityEndpoints.LOGOUT_PAGE_PROPERTY_NAME + ":" + EulerSecurityEndpoints.LOGOUT_PAGE + "}")
    public String logoutPage() {
        return "euler/security/web/logout";
    }

    @Value("${" + EulerSecurityEndpoints.SIGNUP_ENABLED_PROPERTY_NAME + ":" + EulerSecurityEndpoints.SIGNUP_ENABLED + "}")
    public void setSignupEnabled(boolean signupEnabled) {
        this.signupEnabled = signupEnabled;
    }

    @ModelAttribute("signupProcessingUrl")
    public String getSignupProcessingUrl() {
        return signupProcessingUrl;
    }

    @Value("${" + EulerSecurityEndpoints.SIGNUP_PROCESSING_URL_PROPERTY_NAME + ":" + EulerSecurityEndpoints.SIGNUP_PROCESSING_URL + "}")
    public void setSignupProcessingUrl(String signupProcessingUrl) {
        this.signupProcessingUrl = signupProcessingUrl;
    }

    @ModelAttribute("loginProcessingUrl")
    public String getLoginProcessingUrl() {
        return loginProcessingUrl;
    }

    @Value("${" + EulerSecurityEndpoints.LOGIN_PROCESSING_URL_PROPERTY_NAME + ":" + EulerSecurityEndpoints.LOGIN_PROCESSING_URL + "}")
    public void setLoginProcessingUrl(String loginProcessingUrl) {
        this.loginProcessingUrl = loginProcessingUrl;
    }

    @ModelAttribute("logoutProcessingUrl")
    public String getLogoutProcessingUrl() {
        return logoutProcessingUrl;
    }

    @Value("${" + EulerSecurityEndpoints.LOGOUT_PROCESSING_URL_PROPERTY_NAME + ":" + EulerSecurityEndpoints.LOGOUT_PROCESSING_URL + "}")
    public void setLogoutProcessingUrl(String logoutProcessingUrl) {
        this.logoutProcessingUrl = logoutProcessingUrl;
    }
}
