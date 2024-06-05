package org.eulerframework.security.core.captcha.provider;

import org.eulerframework.security.core.captcha.Captcha;

import javax.annotation.Nonnull;

public interface CaptchaProvider<C extends Captcha<?>> {
    C generateCaptcha(String... scope);

    void validateCaptcha(@Nonnull C provideCaptcha);
}
