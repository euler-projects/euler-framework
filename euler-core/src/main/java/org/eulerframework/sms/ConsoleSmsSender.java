package org.eulerframework.sms;

public class ConsoleSmsSender implements SmsSender {

    @Override
    public void sendCaptcha(String template, String mobile, String captcha, int expireMinutes) {
        System.out.printf(
                "Sms sender is disabled, template: %s mobile: %s captcha: %s expireMinutes: %d%n",
                template, mobile, captcha, expireMinutes);
    }

}
