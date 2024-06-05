package org.eulerframework.security.core.captcha;

public interface Captcha<T> {
    String[] getScopes();
    T getCaptcha();
}
