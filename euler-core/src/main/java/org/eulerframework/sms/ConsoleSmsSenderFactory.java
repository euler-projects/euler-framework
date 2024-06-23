package org.eulerframework.sms;

public class ConsoleSmsSenderFactory implements SmsSenderFactory {
    private final static SmsSender SENDER = new ConsoleSmsSender();

    @Override
    public SmsSender newSmsCaptchaSender() {
        return SENDER;
    }
}
