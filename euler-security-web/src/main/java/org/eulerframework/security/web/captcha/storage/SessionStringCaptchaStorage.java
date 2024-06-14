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
package org.eulerframework.security.web.captcha.storage;

import jakarta.servlet.http.HttpSession;
import org.eulerframework.security.core.captcha.StringCaptcha;
import org.eulerframework.security.core.captcha.storage.StringCaptchaStorage;
import org.eulerframework.web.util.ServletUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class SessionStringCaptchaStorage implements StringCaptchaStorage {
    private final static String SESSION_ATTR_PREFIX = "euler.security.captcha";

    @Override
    @SuppressWarnings("unchecked")
    public void saveCaptcha(@Nonnull StringCaptcha captcha) {
        Assert.notNull(captcha, "captcha must not be null");
        Assert.hasText(captcha.getCaptcha(), "captcha value must not be null");

        HttpSession session = ServletUtils.getSession();
        if (session == null) {
            throw new IllegalStateException("No session available. Is the current thread in a http request?");
        }

        Map<String, StringCaptcha> captchaMap;
        if ((captchaMap = (Map<String, StringCaptcha>) session.getAttribute(SESSION_ATTR_PREFIX)) == null) {
            captchaMap = new HashMap<>();
            session.setAttribute(SESSION_ATTR_PREFIX, captchaMap);
        }
        captchaMap.put(captcha.getCaptcha().toLowerCase(), captcha);
    }

    @Override
    public StringCaptcha loadByCaptcha(@Nonnull String captcha) {
        Assert.notNull(captcha, "captcha must not be null");

        HttpSession session = ServletUtils.getSession();
        if (session == null) {
            throw new IllegalStateException("No session available. Is the current thread in a http request?");
        }

        @SuppressWarnings("unchecked")
        Map<String, StringCaptcha> captchaMap = (Map<String, StringCaptcha>) session.getAttribute(SESSION_ATTR_PREFIX);
        if (CollectionUtils.isEmpty(captchaMap)) {
            return null;
        }

        return captchaMap.get(captcha.toLowerCase());
    }
}
