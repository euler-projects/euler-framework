package org.eulerframework.security.core.captcha.storage;

import jakarta.servlet.http.HttpSession;
import org.eulerframework.security.core.captcha.StringCaptcha;
import org.eulerframework.web.util.ServletUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class SessionStringCaptchaStorage implements StringCaptchaStorage {
    private final static String SESSION_ATTR_PREFIX = "euler.security.captcha";

    @Override
    @SuppressWarnings("unchecked")
    public void saveCaptcha(@Nonnull StringCaptcha captcha) {
        Assert.notNull(captcha, "captcha must not be null");
        Assert.hasText(captcha.getCaptcha(), "captcha value must not be null");

        HttpSession session = ServletUtils.getSession();
        if (session == null) {
            throw new IllegalStateException("No session available. Is the current thread in a http request?");
        }

        Map<String, StringCaptcha> captchaMap;
        if ((captchaMap = (Map<String, StringCaptcha>) session.getAttribute(SESSION_ATTR_PREFIX)) == null) {
            captchaMap = new HashMap<>();
            session.setAttribute(SESSION_ATTR_PREFIX, captchaMap);
        }
        captchaMap.put(captcha.getCaptcha().toLowerCase(), captcha);
    }

    @Override
    public StringCaptcha loadByCaptcha(@Nonnull String captcha) {
        Assert.notNull(captcha, "captcha must not be null");

        HttpSession session = ServletUtils.getSession();
        if (session == null) {
            throw new IllegalStateException("No session available. Is the current thread in a http request?");
        }

        @SuppressWarnings("unchecked")
        Map<String, StringCaptcha> captchaMap = (Map<String, StringCaptcha>) session.getAttribute(SESSION_ATTR_PREFIX);
        if (CollectionUtils.isEmpty(captchaMap)) {
            return null;
        }

        return captchaMap.get(captcha.toLowerCase());
    }
}
