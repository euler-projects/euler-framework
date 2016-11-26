package net.eulerframework.common.email;

import net.eulerframework.common.util.GlobalProperties;
import net.eulerframework.common.util.GlobalPropertyReadException;

/**
 * 
 */
public class MailSenderFactory {
 
    /**
     * 服务邮箱
     */
    private static SimpleMailSender simpleSystemMailSender = null;
    private static ThreadSimpleMailSender threadSimpleSystemMailSender = null;
 
    /**
     * 获取简单系统邮件发送器<br>
     * 需要classpath:config.properties<br>
     * <code>mail.username</code>登录名<br>
     * <code>mail.password</code>密码<br>
     * <code>mail.smtp</code>SMTP服务器地址<br>
     * <code>mail.sender</code>系统发件箱<br>
     * <code>mail.defaultReceiver</code>系统收件箱<br>
     * @return {@link SimpleMailSender}
     * @throws GlobalPropertyReadException
     */
    public static SimpleMailSender getSimpleSystemMailSender() throws GlobalPropertyReadException {
        if (simpleSystemMailSender == null) {
            String username = GlobalProperties.get(EmailConfig.F_CONFIG_KEY_UESRNAME);
            String password = GlobalProperties.get(EmailConfig.F_CONFIG_KEY_PASSWORD);
            String smtp = GlobalProperties.get(EmailConfig.F_CONFIG_KEY_SMTP);
            String sender = GlobalProperties.get(EmailConfig.F_CONFIG_KEY_SYS_SENDER);
            String defaultReceiver = GlobalProperties.get(EmailConfig.F_CONFIG_KEY_DEFAULT_RECEIVER);
            EmailConfig config = new EmailConfig();
            config.setUsername(username);
            config.setPassword(password);
            config.setDefaultReceiver(defaultReceiver);
            config.setSmtp(smtp);
            config.setSender(sender);
            simpleSystemMailSender = new SimpleMailSender(config);
        }
        return simpleSystemMailSender;
    }
    
    public static ThreadSimpleMailSender getThreadSimpleMailSender() throws GlobalPropertyReadException {
        if (threadSimpleSystemMailSender == null) {
            String username = GlobalProperties.get(EmailConfig.F_CONFIG_KEY_UESRNAME);
            String password = GlobalProperties.get(EmailConfig.F_CONFIG_KEY_PASSWORD);
            String smtp = GlobalProperties.get(EmailConfig.F_CONFIG_KEY_SMTP);
            String sender = GlobalProperties.get(EmailConfig.F_CONFIG_KEY_SYS_SENDER);
            String defaultReceiver = GlobalProperties.get(EmailConfig.F_CONFIG_KEY_DEFAULT_RECEIVER);
            EmailConfig config = new EmailConfig();
            config.setUsername(username);
            config.setPassword(password);
            config.setDefaultReceiver(defaultReceiver);
            config.setSmtp(smtp);
            config.setSender(sender);
            threadSimpleSystemMailSender = new ThreadSimpleMailSender(config);
        }
        return threadSimpleSystemMailSender;
    }
    
    public static ThreadSimpleMailSender getThreadSimpleMailSenderUseEmailConfig(EmailConfig config) {
        return new ThreadSimpleMailSender(config);
    }
 
    public static void main(String[] args) throws Exception {
        SimpleMailSender simpleSystemMailSender = MailSenderFactory.getSimpleSystemMailSender();
        simpleSystemMailSender.send("密码重置邮件", "<p>请点击下面的链接重置您的密码</p><p><a href=\"http://www.baidu.com\">重置密码</a></p>", "cfrostsun@163.com");
    }
}
