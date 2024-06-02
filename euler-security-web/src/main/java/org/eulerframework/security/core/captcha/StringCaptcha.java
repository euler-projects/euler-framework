package org.eulerframework.security.core.captcha;

import org.springframework.util.Assert;

public class StringCaptcha implements Captcha {
    private final String captcha;

    public StringCaptcha(String captcha) {
        Assert.hasText(captcha, "captcha is empty");
        this.captcha = captcha;
    }

    public String getCaptcha() {
        return captcha;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StringCaptcha that = (StringCaptcha) o;
        return captcha.equalsIgnoreCase(that.captcha);
    }

    @Override
    public int hashCode() {
        return captcha.toLowerCase().hashCode();
    }
}
