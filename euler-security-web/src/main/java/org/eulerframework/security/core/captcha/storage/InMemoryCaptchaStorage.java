package org.eulerframework.security.core.captcha.storage;

import org.eulerframework.security.core.captcha.Captcha;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCaptchaStorage implements CaptchaStorage {
    private final Map<Object, Captcha> captchaStorage = new ConcurrentHashMap<Object, Captcha>();

    @Override
    public void saveCaptcha(@Nonnull Object key, @Nonnull Captcha captcha) {
        Assert.notNull(key, "key must not be null");
        Assert.notNull(captcha, "captcha must not be null");
        this.captchaStorage.put(key, captcha);
    }

    @Override
    public Captcha getCaptcha(@Nonnull Object key) {
        Assert.notNull(key, "key must not be null");
        return this.captchaStorage.get(key);
    }
}
