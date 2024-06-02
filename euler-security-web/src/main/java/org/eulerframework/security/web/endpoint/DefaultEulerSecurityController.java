package org.eulerframework.security.web.endpoint;

import org.eulerframework.security.conf.SecurityConfig;
import org.eulerframework.security.core.captcha.StringCaptcha;
import org.eulerframework.security.core.captcha.storage.CaptchaStorage;
import org.eulerframework.security.core.captcha.util.ImageStringCaptchaDrawer;
import org.eulerframework.security.core.captcha.provider.StringCaptchaProvider;
import org.eulerframework.security.core.captcha.storage.SessionCaptchaStorage;
import org.eulerframework.web.core.base.controller.ThymeleafSupportWebController;
import org.eulerframework.web.core.base.response.ErrorResponse;
import org.eulerframework.web.core.exception.web.WebException;
import org.eulerframework.web.util.ServletUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;

@Controller
public class DefaultEulerSecurityController extends ThymeleafSupportWebController implements EulerSecurityController {
    private boolean signupEnabled;
    private String signupProcessingUrl;
    private String loginProcessingUrl;
    private String logoutProcessingUrl;
    //private EulerUserService eulerUserService;
    private final StringCaptchaProvider stringCaptchaProvider;
    private final ImageStringCaptchaDrawer imageStringCaptchaDrawer;

    /**
     * Init a DefaultEulerSecurityController use
     * default {@link StringCaptchaProvider} with {@link SessionCaptchaStorage},
     * default {@link ImageStringCaptchaDrawer}
     */
    public DefaultEulerSecurityController() {
        this.stringCaptchaProvider = new StringCaptchaProvider(new SessionCaptchaStorage());
        this.imageStringCaptchaDrawer = new ImageStringCaptchaDrawer();
    }

    /**
     * Init a DefaultEulerSecurityController use
     * default {@link StringCaptchaProvider} with given {@link CaptchaStorage},
     * default {@link ImageStringCaptchaDrawer}
     */
    public DefaultEulerSecurityController(CaptchaStorage captchaStorage) {
        Assert.notNull(captchaStorage, "captchaStorage must not be null");

        this.stringCaptchaProvider = new StringCaptchaProvider(captchaStorage);
        this.imageStringCaptchaDrawer = new ImageStringCaptchaDrawer();
    }

    /**
     * Init a DefaultEulerSecurityController use
     * default {@link StringCaptchaProvider} with given {@link CaptchaStorage},
     * given {@link ImageStringCaptchaDrawer}
     */
    public DefaultEulerSecurityController(CaptchaStorage captchaStorage, ImageStringCaptchaDrawer imageStringCaptchaDrawer) {
        Assert.notNull(captchaStorage, "captchaStorage must not be null");
        Assert.notNull(imageStringCaptchaDrawer, "imageStringCaptchaDrawer must not be null");

        this.stringCaptchaProvider = new StringCaptchaProvider(captchaStorage);
        this.imageStringCaptchaDrawer = imageStringCaptchaDrawer;
    }

    /**
     * Init a DefaultEulerSecurityController use
     * given {@link StringCaptchaProvider},
     * given {@link ImageStringCaptchaDrawer}
     */
    public DefaultEulerSecurityController(StringCaptchaProvider stringCaptchaProvider, ImageStringCaptchaDrawer imageStringCaptchaDrawer) {
        Assert.notNull(stringCaptchaProvider, "stringCaptchaProvider must not be null");
        Assert.notNull(imageStringCaptchaDrawer, "imageStringCaptchaDrawer must not be null");

        this.stringCaptchaProvider = stringCaptchaProvider;
        this.imageStringCaptchaDrawer = imageStringCaptchaDrawer;
    }

    @GetMapping("captcha/simple")
    @ResponseBody
    public void captcha() throws IOException {
        ServletUtils.writeFileHeader(this.getResponse(), "captcha.jpeg", null);
        StringCaptcha stringCaptcha = this.stringCaptchaProvider.generateCaptcha(this.getRequest());
        this.imageStringCaptchaDrawer.drawCaptchaImage(this.getResponse().getOutputStream(), stringCaptcha);
    }

    @GetMapping("validCaptcha")
    @ResponseBody
    public Object validCaptcha(@RequestParam String captcha) {
        try {
            this.stringCaptchaProvider.validateCaptcha(this.getRequest(), new StringCaptcha(captcha));
            return null;
        } catch (WebException e) {
            this.getResponse().setStatus(HttpStatus.BAD_REQUEST.value());
            return new ErrorResponse(e);
        }
    }

    @Override
    @GetMapping("${" + EulerSecurityEndpoints.SIGNUP_PAGE_PROPERTY_NAME + ":" + EulerSecurityEndpoints.SIGNUP_PAGE + "}")
    public String signupPage() throws NoResourceFoundException {
        if (!this.signupEnabled) {
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

    @PostMapping("${" + EulerSecurityEndpoints.SIGNUP_PROCESSING_URL_PROPERTY_NAME + ":" + EulerSecurityEndpoints.SIGNUP_PROCESSING_URL + "}")
    public String litesignup(
            @RequestParam String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String mobile,
            @RequestParam String password) {
        if (SecurityConfig.isSignUpEnabled()) {
            if (SecurityConfig.isSignUpEnableCaptcha()) {
                //Captcha.validCaptcha(this.getRequest());
            }

            //this.eulerUserService.signUp(username, email, mobile, password);
            return this.success();
        } else {
            return this.notfound();
        }
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

    //    @Autowired
//    public void setEulerUserService(EulerUserService eulerUserService) {
//        this.eulerUserService = eulerUserService;
//    }
}
