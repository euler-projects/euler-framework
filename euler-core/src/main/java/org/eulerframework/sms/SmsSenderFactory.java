package org.eulerframework.sms;

/**
 * 用于在发送短信前生成新的短信发送实现类实例，工厂实现类可自行决定是否采用单例模式。
 *
 * @author cFrost
 */
public interface SmsSenderFactory {

    SmsSender newSmsCaptchaSender();

}
