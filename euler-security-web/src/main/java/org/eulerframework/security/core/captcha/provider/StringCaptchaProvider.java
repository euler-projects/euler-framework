package org.eulerframework.security.core.captcha.provider;

import org.eulerframework.common.util.Assert;
import org.eulerframework.security.core.captcha.Captcha;
import org.eulerframework.security.core.captcha.StringCaptcha;
import org.eulerframework.security.core.captcha.storage.CaptchaStorage;
import org.eulerframework.security.exception.InvalidCaptchaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Random;

public class StringCaptchaProvider implements CaptchaProvider {
    private final Logger logger = LoggerFactory.getLogger(StringCaptchaProvider.class);
    private static final char[] DEFAULT_AVAILABLE_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    private final Random random = new Random();
    private final CaptchaStorage captchaStorage;

    private char[] availableChars = DEFAULT_AVAILABLE_CHARS;
    private int captchaLength = 6;

    public StringCaptchaProvider(CaptchaStorage captchaStorage) {
        this.captchaStorage = captchaStorage;
    }

    @Override
    public StringCaptcha generateCaptcha(Object key) {
        Assert.notNull(key, "key is null");

        StringBuilder captchaBuilder = new StringBuilder();
        for (int i = 0; i < captchaLength; i++) {
            captchaBuilder.append(this.availableChars[random.nextInt(this.availableChars.length)]);
        }
        StringCaptcha captcha = new StringCaptcha(captchaBuilder.toString());

        this.captchaStorage.saveCaptcha(key, captcha);
        return captcha;
    }

    @Override
    public void validateCaptcha(@Nonnull Object key, @Nonnull Captcha provideCaptcha) {
        Assert.notNull(key, "key is null");
        Assert.notNull(provideCaptcha, "provideCaptcha is null");

        Captcha savedCaptcha = this.captchaStorage.getCaptcha(key);

        if (savedCaptcha == null) {
            throw new InvalidCaptchaException();
        }

        if (savedCaptcha.getClass() != StringCaptcha.class || provideCaptcha.getClass() != StringCaptcha.class) {
            // Only support StringCaptcha
            this.logger.warn("Unsupported captcha type, saved type: '{}', provide type: '{}'",
                    savedCaptcha.getClass(), provideCaptcha.getClass());
            throw new InvalidCaptchaException();
        }

        if (!savedCaptcha.equals(provideCaptcha)) {
            throw new InvalidCaptchaException();
        }
    }

    public void setCaptchaLength(int captchaLength) {
        this.captchaLength = captchaLength;
    }

    public void setAvailableChars(char[] availableChars) {
        this.availableChars = availableChars;
    }
}
