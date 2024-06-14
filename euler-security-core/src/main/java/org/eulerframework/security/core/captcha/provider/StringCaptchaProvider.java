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
package org.eulerframework.security.core.captcha.provider;

import org.apache.commons.lang3.ArrayUtils;
import org.eulerframework.common.util.Assert;
import org.eulerframework.security.core.captcha.storage.StringCaptchaStorage;
import org.eulerframework.security.core.captcha.StringCaptcha;
import org.eulerframework.security.exception.InvalidCaptchaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Random;

public class StringCaptchaProvider implements CaptchaProvider<StringCaptcha> {
    private final Logger logger = LoggerFactory.getLogger(StringCaptchaProvider.class);
    private static final char[] DEFAULT_AVAILABLE_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    private static final char[] NUMBERS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    private static final int DEFAULT_LENGTH = 6;

    private final Random random = new Random();
    private final StringCaptchaStorage captchaStorage;

    private char[] defaultAvailableChars = DEFAULT_AVAILABLE_CHARS;
    private int defaultLength = DEFAULT_LENGTH;

    public StringCaptchaProvider(StringCaptchaStorage captchaStorage) {
        this.captchaStorage = captchaStorage;
    }

    public StringCaptcha generateCaptcha(char[] availableChars, int length, String... scope) {
        StringBuilder captchaBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            captchaBuilder.append(availableChars[random.nextInt(availableChars.length)]);
        }
        StringCaptcha captcha = new StringCaptcha(captchaBuilder.toString(), scope);

        this.captchaStorage.saveCaptcha(captcha);
        return captcha;
    }

    @Override
    public StringCaptcha generateCaptcha(String... scope) {
        return this.generateCaptcha(this.defaultAvailableChars, this.defaultLength, scope);
    }

    @Override
    public void validateCaptcha(@Nonnull StringCaptcha provideCaptcha) {
        Assert.notNull(provideCaptcha, "provideCaptcha is null");
        Assert.hasText(provideCaptcha.getCaptcha(), "provideCaptcha is empty");

        StringCaptcha savedCaptcha = this.captchaStorage.loadByCaptcha(provideCaptcha.getCaptcha());

        if (savedCaptcha == null) {
            throw new InvalidCaptchaException();
        }

//        if (savedCaptcha.getClass() != StringCaptcha.class || provideCaptcha.getClass() != StringCaptcha.class) {
//            // Only support StringCaptcha
//            this.logger.warn("Unsupported captcha type, saved type: '{}', provide type: '{}'",
//                    savedCaptcha.getClass(), provideCaptcha.getClass());
//            throw new InvalidCaptchaException();
//        }

        if (!savedCaptcha.getCaptcha().equalsIgnoreCase(provideCaptcha.getCaptcha())) {
            throw new InvalidCaptchaException();
        }

        String[] savedScopes = savedCaptcha.getScopes();
        String[] provideScopes = provideCaptcha.getScopes();

        if (provideScopes.length > savedScopes.length) {
            throw new InvalidCaptchaException();
        }

        if (savedScopes.length > 0 && provideScopes.length == 0) {
            throw new InvalidCaptchaException();
        }

        for (String provideScope : provideScopes) {
            if (!ArrayUtils.contains(savedScopes, provideScope)) {
                throw new InvalidCaptchaException();
            }
        }
    }

    public char[] getDefaultAvailableChars() {
        return defaultAvailableChars;
    }

    public void setDefaultAvailableChars(char[] defaultAvailableChars) {
        this.defaultAvailableChars = defaultAvailableChars;
    }

    public int getDefaultLength() {
        return defaultLength;
    }

    public void setDefaultLength(int defaultLength) {
        this.defaultLength = defaultLength;
    }
}
