package org.eulerframework.security.core.captcha.view;

public interface SmsCaptchaView {
    void sendSms(String template, String mobile, String captcha, int expireMinutes);
}
