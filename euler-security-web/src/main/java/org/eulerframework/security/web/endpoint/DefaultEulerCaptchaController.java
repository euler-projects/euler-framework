/*
 * Copyright 2013-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eulerframework.security.web.endpoint;

import org.eulerframework.security.core.captcha.CaptchaVisibility;
import org.eulerframework.security.core.captcha.StringCaptchaDetails;
import org.eulerframework.security.core.captcha.provider.StringCaptchaProvider;
import org.eulerframework.security.core.captcha.storage.CaptchaStorage;
import org.eulerframework.security.core.captcha.view.ImageCaptchaView;
import org.eulerframework.security.core.captcha.view.SmsCaptchaView;
import org.eulerframework.security.core.captcha.view.DefaultImageCaptchaView;
import org.eulerframework.security.web.captcha.storage.SessionCaptchaStorage;
import org.eulerframework.web.core.base.controller.ApiSupportWebController;
import org.eulerframework.web.core.base.response.ErrorResponse;
import org.eulerframework.web.core.exception.web.WebException;
import org.eulerframework.web.util.ServletUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("captcha")
public class DefaultEulerCaptchaController extends ApiSupportWebController {
    private final StringCaptchaProvider stringCaptchaProvider;
    private final ImageCaptchaView imageCaptchaView;
    private SmsCaptchaView smsCaptchaView;

    /**
     * Init a DefaultEulerSecurityController use
     * default {@link StringCaptchaProvider} with {@link SessionCaptchaStorage},
     * default {@link DefaultImageCaptchaView}
     */
    public DefaultEulerCaptchaController() {
        this.stringCaptchaProvider = new StringCaptchaProvider(new SessionCaptchaStorage());
        this.imageCaptchaView = new DefaultImageCaptchaView();
    }

    /**
     * Init a DefaultEulerSecurityController use
     * default {@link StringCaptchaProvider} with given {@link CaptchaStorage},
     * default {@link DefaultImageCaptchaView}
     */
    public DefaultEulerCaptchaController(CaptchaStorage captchaStorage) {
        Assert.notNull(captchaStorage, "captchaStorage must not be null");

        this.stringCaptchaProvider = new StringCaptchaProvider(captchaStorage);
        this.imageCaptchaView = new DefaultImageCaptchaView();
    }

    /**
     * Init a DefaultEulerSecurityController use
     * default {@link StringCaptchaProvider} with given {@link CaptchaStorage},
     * given {@link DefaultImageCaptchaView}
     */
    public DefaultEulerCaptchaController(CaptchaStorage captchaStorage, ImageCaptchaView imageCaptchaView) {
        Assert.notNull(captchaStorage, "captchaStorage must not be null");
        Assert.notNull(imageCaptchaView, "imageCaptchaView must not be null");

        this.stringCaptchaProvider = new StringCaptchaProvider(captchaStorage);
        this.imageCaptchaView = imageCaptchaView;
    }

    /**
     * Init a DefaultEulerSecurityController use
     * given {@link StringCaptchaProvider},
     * given {@link DefaultImageCaptchaView}
     */
    public DefaultEulerCaptchaController(StringCaptchaProvider stringCaptchaProvider, ImageCaptchaView imageCaptchaView) {
        Assert.notNull(stringCaptchaProvider, "stringCaptchaProvider must not be null");
        Assert.notNull(imageCaptchaView, "imageCaptchaView must not be null");

        this.stringCaptchaProvider = stringCaptchaProvider;
        this.imageCaptchaView = imageCaptchaView;
    }

    @GetMapping
    public void captcha(
            @RequestParam(required = false) String scope,
            @RequestParam String phone) {
        String[] scopes = StringUtils.delimitedListToStringArray(scope, " ");
        StringCaptchaDetails stringCaptcha = this.stringCaptchaProvider.generateCaptcha(CaptchaVisibility.SESSION, scopes);
        this.smsCaptchaView.sendSms("SIGN_IN", phone, "1234", 10);
    }

    @GetMapping(produces = MediaType.IMAGE_PNG_VALUE)
    public void pngCaptcha(@RequestParam(required = false) String scope) throws IOException {
        String[] scopes = StringUtils.delimitedListToStringArray(scope, " ");
        StringCaptchaDetails stringCaptcha = this.stringCaptchaProvider.generateCaptcha(CaptchaVisibility.SESSION, scopes);
        ServletUtils.writeFileHeader(this.getResponse(), "captcha.png", null);
        this.imageCaptchaView.draw(stringCaptcha, "png", this.getResponse().getOutputStream());
    }

    @GetMapping("validCaptcha")
    public Object validCaptcha(@RequestParam String captcha, @RequestParam(required = false) String scope) {
        try {
            String[] scopes = StringUtils.delimitedListToStringArray(scope, " ");
            this.stringCaptchaProvider.validateCaptcha(captcha, scopes);
            return null;
        } catch (WebException e) {
            this.getResponse().setStatus(HttpStatus.BAD_REQUEST.value());
            return new ErrorResponse(e);
        }
    }

    public void setSmsCaptchaView(SmsCaptchaView smsCaptchaView) {
        this.smsCaptchaView = smsCaptchaView;
    }
}
