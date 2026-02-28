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
import org.eulerframework.common.util.json.Jackson2Utils;
import org.eulerframework.security.core.captcha.CaptchaDetails;
import org.eulerframework.security.core.captcha.CaptchaVisibility;
import org.eulerframework.security.core.captcha.storage.CaptchaStorage;
import org.eulerframework.web.servlet.util.ServletUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class SessionCaptchaStorage implements CaptchaStorage {
    private final static String SESSION_ATTR_PREFIX = "euler.security.captcha";

    private final static Object LOCK = new Object();

    private final Map<Class<?>, Function<Object, Object>> captchaKeygen = new HashMap<>();

    public SessionCaptchaStorage() {
        this.addKeyGen(String.class, String::toLowerCase);
    }

    @SuppressWarnings("unchecked")
    public <T> void addKeyGen(Class<T> captchaClass, Function<T, Object> keygen) {
        Assert.notNull(captchaClass, "captchaClass must not be null");
        Assert.notNull(keygen, "keygen must not be null");
        this.captchaKeygen.put(captchaClass, (Function<Object, Object>) keygen);
    }

    public void addCaptchaKeygen(Class<?> captchaType, Function<Object, Object> keygen) {
        Assert.notNull(captchaType, "captchaType must not be null");
        Assert.notNull(keygen, "keygen must not be null");
        captchaKeygen.put(captchaType, keygen);
    }

    @Override
    public boolean support(@Nonnull CaptchaDetails captchaDetails) {
        Assert.notNull(captchaDetails, "captcha must not be null");
        Assert.notNull(captchaDetails.getVisibility(), "captcha visibility must not be null");
        return captchaDetails.getVisibility().getLevel() <= CaptchaVisibility.SESSION.getLevel();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void saveCaptcha(@Nonnull CaptchaDetails captchaDetails) {
        Assert.isTrue(this.support(captchaDetails), () -> "Unsupported captcha: " + Jackson2Utils.writeValueAsString(captchaDetails));

        HttpSession session = ServletUtils.getSession();
        if (session == null) {
            throw new IllegalStateException("No session available. Is the current thread in a http request?");
        }

        Map<Object, CaptchaDetails> captchaMap;
        if ((captchaMap = (Map<Object, CaptchaDetails>) session.getAttribute(SESSION_ATTR_PREFIX)) == null) {
            synchronized (LOCK) {
                if ((captchaMap = (Map<Object, CaptchaDetails>) session.getAttribute(SESSION_ATTR_PREFIX)) == null) {
                    captchaMap = new HashMap<>();
                    session.setAttribute(SESSION_ATTR_PREFIX, captchaMap);
                }
            }
        }

        captchaMap.put(this.keygen(captchaDetails.getCaptcha()), captchaDetails);
    }

    @Override
    public CaptchaDetails loadByCaptcha(@Nonnull Object captcha) {
        Assert.notNull(captcha, "captcha must not be null");

        HttpSession session = ServletUtils.getSession();
        if (session == null) {
            throw new IllegalStateException("No session available. Is the current thread in a http request?");
        }

        @SuppressWarnings("unchecked")
        Map<Object, CaptchaDetails> captchaMap = (Map<Object, CaptchaDetails>) session.getAttribute(SESSION_ATTR_PREFIX);
        if (CollectionUtils.isEmpty(captchaMap)) {
            return null;
        }

        return captchaMap.get(this.keygen(captcha));
    }

    private Object keygen(Object captcha) {
        return Optional.ofNullable(this.captchaKeygen.get(captcha.getClass()))
                .map(keygen -> keygen.apply(captcha))
                .orElse(captcha);
    }
}
