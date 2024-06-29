package org.eulerframework.security.core.captcha.storage;

import org.eulerframework.security.core.captcha.CaptchaDetails;

import javax.annotation.Nonnull;

public class DelegatingCaptchaStorage implements CaptchaStorage {

    @Override
    public boolean support(@Nonnull CaptchaDetails captchaDetails) {
        return false;
    }

    @Override
    public void saveCaptcha(@Nonnull CaptchaDetails captchaDetails) {

    }

    @Override
    public CaptchaDetails loadByCaptcha(@Nonnull Object captcha) {
        return null;
    }
}
