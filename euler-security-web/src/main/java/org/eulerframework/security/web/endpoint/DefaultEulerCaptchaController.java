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

import org.eulerframework.security.core.captcha.StringCaptcha;
import org.eulerframework.security.core.captcha.provider.StringCaptchaProvider;
import org.eulerframework.security.core.captcha.storage.CaptchaStorage;
import org.eulerframework.security.web.captcha.storage.SessionStringCaptchaStorage;
import org.eulerframework.security.core.captcha.storage.StringCaptchaStorage;
import org.eulerframework.security.core.captcha.util.ImageStringCaptchaDrawer;
import org.eulerframework.web.core.base.controller.ThymeleafPageController;
import org.eulerframework.web.core.base.response.ErrorResponse;
import org.eulerframework.web.core.exception.web.WebException;
import org.eulerframework.web.util.ServletUtils;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("captcha")
public class DefaultEulerCaptchaController extends ThymeleafPageController {
    private final StringCaptchaProvider stringCaptchaProvider;
    private final ImageStringCaptchaDrawer imageStringCaptchaDrawer;

    /**
     * Init a DefaultEulerSecurityController use
     * default {@link StringCaptchaProvider} with {@link SessionStringCaptchaStorage},
     * default {@link ImageStringCaptchaDrawer}
     */
    public DefaultEulerCaptchaController() {
        this.stringCaptchaProvider = new StringCaptchaProvider(new SessionStringCaptchaStorage());
        this.imageStringCaptchaDrawer = new ImageStringCaptchaDrawer();
    }

    /**
     * Init a DefaultEulerSecurityController use
     * default {@link StringCaptchaProvider} with given {@link CaptchaStorage},
     * default {@link ImageStringCaptchaDrawer}
     */
    public DefaultEulerCaptchaController(StringCaptchaStorage stringCaptchaStorage) {
        Assert.notNull(stringCaptchaStorage, "stringCaptchaStorage must not be null");

        this.stringCaptchaProvider = new StringCaptchaProvider(stringCaptchaStorage);
        this.imageStringCaptchaDrawer = new ImageStringCaptchaDrawer();
    }

    /**
     * Init a DefaultEulerSecurityController use
     * default {@link StringCaptchaProvider} with given {@link CaptchaStorage},
     * given {@link ImageStringCaptchaDrawer}
     */
    public DefaultEulerCaptchaController(StringCaptchaStorage stringCaptchaStorage, ImageStringCaptchaDrawer imageStringCaptchaDrawer) {
        Assert.notNull(stringCaptchaStorage, "stringCaptchaStorage must not be null");
        Assert.notNull(imageStringCaptchaDrawer, "imageStringCaptchaDrawer must not be null");

        this.stringCaptchaProvider = new StringCaptchaProvider(stringCaptchaStorage);
        this.imageStringCaptchaDrawer = imageStringCaptchaDrawer;
    }

    /**
     * Init a DefaultEulerSecurityController use
     * given {@link StringCaptchaProvider},
     * given {@link ImageStringCaptchaDrawer}
     */
    public DefaultEulerCaptchaController(StringCaptchaProvider stringCaptchaProvider, ImageStringCaptchaDrawer imageStringCaptchaDrawer) {
        Assert.notNull(stringCaptchaProvider, "stringCaptchaProvider must not be null");
        Assert.notNull(imageStringCaptchaDrawer, "imageStringCaptchaDrawer must not be null");

        this.stringCaptchaProvider = stringCaptchaProvider;
        this.imageStringCaptchaDrawer = imageStringCaptchaDrawer;
    }

    @GetMapping("img")
    @ResponseBody
    public void captcha(@RequestParam(required = false) String scope) throws IOException {
        String[] scopes = StringUtils.delimitedListToStringArray(scope, " ");
        StringCaptcha stringCaptcha = this.stringCaptchaProvider.generateCaptcha(scopes);
        ServletUtils.writeFileHeader(this.getResponse(), "captcha.jpeg", null);
        this.imageStringCaptchaDrawer.drawCaptchaImage(this.getResponse().getOutputStream(), stringCaptcha);
    }

    @GetMapping("validCaptcha")
    @ResponseBody
    public Object validCaptcha(@RequestParam String captcha, @RequestParam(required = false) String scope) {
        try {
            String[] scopes = StringUtils.delimitedListToStringArray(scope, " ");
            this.stringCaptchaProvider.validateCaptcha(new StringCaptcha(captcha, scopes));
            return null;
        } catch (WebException e) {
            this.getResponse().setStatus(HttpStatus.BAD_REQUEST.value());
            return new ErrorResponse(e);
        }
    }
}
