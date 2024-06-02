package org.eulerframework.security.core.captcha.storage;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.eulerframework.security.core.captcha.Captcha;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;

public class SessionCaptchaStorage implements CaptchaStorage {
    private final static String SESSION_ATTR_PREFIX = "euler.security.captcha";

    @Override
    public void saveCaptcha(@Nonnull Object key, @Nonnull Captcha captcha) {
        Assert.notNull(key, "key must not be null");
        Assert.notNull(captcha, "captcha must not be null");
        Assert.isAssignable(HttpServletRequest.class, key.getClass(), "key must be a HttpServletRequest");

        HttpServletRequest request = (HttpServletRequest) key;
        HttpSession session = request.getSession();
        if (session == null) {
            throw new IllegalStateException("No session available");
        }
        session.setAttribute(SESSION_ATTR_PREFIX, captcha);
    }

    @Override
    public Captcha getCaptcha(@Nonnull Object key) {
        Assert.notNull(key, "key must not be null");
        Assert.isAssignable(HttpServletRequest.class, key.getClass(), "key must be a HttpServletRequest");

        HttpServletRequest request = (HttpServletRequest) key;
        HttpSession session = request.getSession();
        if (session == null) {
            throw new IllegalStateException("No session available");
        }
        return  (Captcha) session.getAttribute(SESSION_ATTR_PREFIX);
    }
}
