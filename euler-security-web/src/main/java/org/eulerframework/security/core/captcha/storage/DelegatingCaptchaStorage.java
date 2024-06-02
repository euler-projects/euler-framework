package org.eulerframework.security.core.captcha.storage;

import org.eulerframework.security.core.captcha.Captcha;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import java.util.Map;

public class DelegatingCaptchaStorage implements CaptchaStorage {
    private final Map<Class<?> /* keyType */, CaptchaStorage> storages;

    public DelegatingCaptchaStorage(Map<Class<?> , CaptchaStorage> storages) {
        Assert.notEmpty(storages, "storages must not be empty");
        this.storages = storages;
    }

    @Override
    public void saveCaptcha(@Nonnull Object key, @Nonnull Captcha captcha) {
        Assert.notNull(key, "key must not be null");
        Assert.notNull(captcha, "captcha must not be null");
        this.getCaptchaStorage(key).saveCaptcha(key, captcha);
    }

    @Override
    public Captcha getCaptcha(@Nonnull Object key) {
        Assert.notNull(key, "key must not be null");
        return this.getCaptchaStorage(key).getCaptcha(key);
    }

    private CaptchaStorage getCaptchaStorage(Object key) {
        CaptchaStorage captchaStorage = this.storages.get(key.getClass());
        if (captchaStorage == null) {
            throw new IllegalArgumentException("No storage found for key type is: " + key.getClass());
        }
        return captchaStorage;
    }
}
