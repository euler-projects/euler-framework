package net.eulerframework.web.module.authentication.util;

/**
 * 
 * 用于在发送短信前生成新的短信发送实现类实例，之所以每次生成新的实例是为了在出现并发时，每个实例的逻辑互不干扰。
 * 
 * @author cFrost
 *
 */
public interface SmsSenderFactory{
    
    abstract public SmsSender newSmsSender();
    
    public interface SmsSender {
        public void sendSms(String mobile, String msg);
    }
}
