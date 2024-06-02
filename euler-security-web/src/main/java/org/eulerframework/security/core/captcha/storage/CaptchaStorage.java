package org.eulerframework.security.core.captcha.storage;

import org.eulerframework.security.core.captcha.Captcha;

import javax.annotation.Nonnull;

public interface CaptchaStorage {
    void saveCaptcha(@Nonnull Object key, @Nonnull Captcha captcha);
    Captcha getCaptcha(@Nonnull Object key);
}
