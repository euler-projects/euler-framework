package org.eulerframework.security.core.captcha.storage;

import org.eulerframework.security.core.captcha.Captcha;

import javax.annotation.Nonnull;

public interface CaptchaStorage<C extends Captcha<T>, T> {
    void saveCaptcha(@Nonnull C captcha);

    C loadByCaptcha(@Nonnull T captcha);
}
