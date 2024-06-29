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
import org.eulerframework.security.core.captcha.CaptchaDetails;
import org.eulerframework.security.core.captcha.CaptchaVisibility;
import org.eulerframework.security.core.captcha.StringCaptchaDetails;
import org.eulerframework.security.core.captcha.storage.CaptchaStorage;
import org.eulerframework.security.exception.InvalidCaptchaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import java.util.Random;

public class StringCaptchaProvider implements CaptchaProvider<StringCaptchaDetails, String> {
    private final Logger logger = LoggerFactory.getLogger(StringCaptchaProvider.class);
    private static final char[] DEFAULT_AVAILABLE_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    private static final char[] NUMBERS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    private static final int DEFAULT_LENGTH = 6;

    private final Random random = new Random();
    private final CaptchaStorage captchaStorage;

    private char[] defaultAvailableChars = DEFAULT_AVAILABLE_CHARS;
    private int defaultLength = DEFAULT_LENGTH;

    public StringCaptchaProvider(CaptchaStorage captchaStorage) {
        this.captchaStorage = captchaStorage;
    }

    public StringCaptchaDetails generateCaptcha(char[] availableChars, int length, CaptchaVisibility visibility, String... scope) {
        StringBuilder captchaBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            captchaBuilder.append(availableChars[random.nextInt(availableChars.length)]);
        }

        return StringCaptchaDetails.builder()
                .captcha(captchaBuilder.toString())
                .scope(scope)
                .visibility(visibility)
                .caseSensitive(false)
                .build();
    }

    @Override
    public StringCaptchaDetails generateCaptcha(CaptchaVisibility visibility, String... scope) {
        StringCaptchaDetails captcha = this.generateCaptcha(this.defaultAvailableChars, this.defaultLength, visibility, scope);
        this.captchaStorage.saveCaptcha(captcha);
        return captcha;
    }

    @Override
    public void validateCaptcha(@Nonnull String provideCaptcha, String... exceptScope) {
        Assert.hasText(provideCaptcha, "provideCaptcha is empty");
        Assert.notEmpty(exceptScope, "exceptScopes is empty");

        CaptchaDetails savedCaptchaDetails = this.captchaStorage.loadByCaptcha(provideCaptcha);

        if (savedCaptchaDetails instanceof StringCaptchaDetails stringCaptchaDetails) {
            String savedCaptcha = stringCaptchaDetails.getCaptcha();

            if (stringCaptchaDetails.isCaseSensitive()) {
                if (!savedCaptcha.equals(provideCaptcha)) {
                    throw new InvalidCaptchaException("invalid captcha");
                }
            } else {
                if (!savedCaptcha.equalsIgnoreCase(provideCaptcha)) {
                    throw new InvalidCaptchaException("invalid captcha");
                }
            }

            String[] savedScopes = savedCaptchaDetails.getScopes();

            if (exceptScope.length > savedScopes.length) {
                throw new InvalidCaptchaException("invalid scopes");
            }

            for (String each : exceptScope) {
                if (!ArrayUtils.contains(savedScopes, each)) {
                    throw new InvalidCaptchaException("invalid scopes");
                }
            }
        } else {
            throw new InvalidCaptchaException("invalid captcha");
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
