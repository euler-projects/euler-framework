package org.eulerframework.sms;

public interface SmsSender {
    void sendCaptcha(String template, String mobile, String captcha, int expireMinutes);
}
