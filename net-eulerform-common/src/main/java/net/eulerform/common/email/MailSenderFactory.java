package net.eulerform.common.email;

import net.eulerform.common.util.GlobalProperties;

/**
 * 发件箱工厂
 * 
 * @author MZULE
 * 
 */
public class MailSenderFactory {
 
    /**
     * 服务邮箱
     */
    private static SimpleMailSender simpleSystemMailSender = null;
    
    private SimpleMailSender simpleMailSender = null;
    
    private String username;
    private String password;
    private String smtpHost;
    private String senderEmailAddr;
    
    public MailSenderFactory(String username, String password, String smtpHost, String senderEmailAddr) {
        this.username = username;
        this.password = password;
        this.smtpHost = smtpHost;
        this.senderEmailAddr = senderEmailAddr;
    }
    
    public SimpleMailSender getSimpleMailSender() throws Exception {
        if (simpleMailSender == null) {
            simpleMailSender = new SimpleMailSender(username, password, smtpHost, senderEmailAddr);
        }
        return simpleMailSender;
    }
 
    /**
     * 获取简单系统邮件发送器
     * @return
     * @throws Exception
     */
    public static SimpleMailSender getSimpleSystemMailSender() throws Exception {
        if (simpleSystemMailSender == null) {
            String username = GlobalProperties.get("mail.username");
            String password = GlobalProperties.get("mail.password");
            String smtp = GlobalProperties.get("mail.smtp.host");
            String adminEmail = GlobalProperties.get("mail.admin.email");
            simpleSystemMailSender = new SimpleMailSender(username, password, smtp, adminEmail);
        }
        return simpleSystemMailSender;
    }
 
    public static void main(String[] args) throws Exception {
        SimpleMailSender simpleSystemMailSender = MailSenderFactory.getSimpleSystemMailSender();
        simpleSystemMailSender.send("密码重置邮件", "<p>请点击下面的链接重置您的密码</p><p><a href=\"http://www.baidu.com\">重置密码</a></p>", "cfrostsun@163.com");
    }
}
