package org.eulerframework.security.core.captcha;

import org.springframework.util.Assert;

public class StringCaptcha implements Captcha<String> {
    private final String[] scopes;
    private final String captcha;

    public StringCaptcha(String captcha, String... scopes) {
        Assert.hasText(captcha, "captcha is empty");
        Assert.notNull(scopes, "scopes is null");
        this.captcha = captcha;
        this.scopes = scopes;
    }

    @Override
    public String[] getScopes() {
        return scopes;
    }

    public String getCaptcha() {
        return captcha;
    }
}
