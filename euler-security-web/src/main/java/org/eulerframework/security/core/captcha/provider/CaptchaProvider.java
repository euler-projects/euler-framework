package org.eulerframework.security.core.captcha.provider;

import org.eulerframework.security.core.captcha.Captcha;

import javax.annotation.Nonnull;

public interface CaptchaProvider {
    Captcha generateCaptcha(Object key);

    void validateCaptcha(@Nonnull Object key, @Nonnull Captcha provideCaptcha);
}
