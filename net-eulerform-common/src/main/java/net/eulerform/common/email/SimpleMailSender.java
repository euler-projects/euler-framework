package net.eulerform.common.email;

import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

/**
 * 简单邮件发送器，可单发，群发。
 * 
 * @author MZULE
 * 
 */
public class SimpleMailSender {
    
    private String senderEmailAddr;

    /**
     * 发送邮件的props文件
     */
    private final transient Properties props = System.getProperties();
    /**
     * 邮件服务器登录验证
     */
    private transient MailAuthenticator authenticator;

    /**
     * 邮箱session
     */
    private transient Session session;

    /**
     * 初始化邮件发送器
     * 
     * @param smtpHostName
     *            SMTP邮件服务器地址
     * @param username
     *            发送邮件的用户名(地址)
     * @param password
     *            发送邮件的密码
     */
    public SimpleMailSender(final String username, final String password, final String smtpHostName, final String senderEmailAddr) {
        init(username, password, smtpHostName);
        this.senderEmailAddr = senderEmailAddr;
    }

    /**
     * 初始化邮件发送器
     * 
     * @param username
     *            发送邮件的用户名(地址)，并以此解析SMTP服务器地址
     * @param password
     *            发送邮件的密码
     */
    public SimpleMailSender(final String username, final String password) {
        // 通过邮箱地址解析出smtp服务器，对大多数邮箱都管用
        final String smtpHostName = "smtp." + username.split("@")[1];
        init(username, password, smtpHostName);

    }

    /**
     * 初始化
     * 
     * @param username
     *            发送邮件的用户名(地址)
     * @param password
     *            密码
     * @param smtpHostName
     *            SMTP主机地址
     */
    private void init(String username, String password, String smtpHostName) {
        // 初始化props
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", smtpHostName);
        // 验证
        authenticator = new MailAuthenticator(username, password);
        // 创建session
        session = Session.getInstance(props, authenticator);
    }

    /**
     * 发送邮件
     * 
     * @param subject
     *            邮件主题
     * @param content
     *            邮件内容
     * @param receiver
     *            收件人邮箱地址
     * @throws AddressException
     * @throws MessagingException
     */
    public void send(String subject, Object content, String receiver) throws AddressException, MessagingException {
        // 创建mime类型邮件
        final MimeMessage message = new MimeMessage(session);
        // 设置发信人
        message.setFrom(new InternetAddress(this.senderEmailAddr));
        // 设置收件人
        message.setRecipient(RecipientType.TO, new InternetAddress(receiver));
        // 设置主题
        message.setSubject(subject);
        // 设置邮件内容
        message.setContent(content.toString(), "text/html;charset=utf-8");
        // 发送
        Transport.send(message);
    }

    /**
     * 群发邮件
     * 
     * @param subject
     *            主题
     * @param content
     *            内容
     * @param receiver
     *            收件人们
     * @throws AddressException
     * @throws MessagingException
     */
    public void send(String subject, Object content, String... receiver)
            throws AddressException, MessagingException {
        // 创建mime类型邮件
        final MimeMessage message = new MimeMessage(session);
        // 设置发信人
        message.setFrom(new InternetAddress(this.senderEmailAddr));
        Set<InternetAddress> addresses = new HashSet<>();
        for (String each : receiver) {
            addresses.add(new InternetAddress(each));
        }
        message.setRecipients(RecipientType.TO, addresses.toArray(new InternetAddress[0]));
        // 设置主题
        message.setSubject(subject);
        // 设置邮件内容
        message.setContent(content.toString(), "text/html;charset=utf-8");
        // 发送
        Transport.send(message);
    }

    /**
     * 群发邮件
     * 
     * @param subject
     *            主题
     * @param content
     *            内容
     * @param receivers
     *            收件人们
     * @throws AddressException
     * @throws MessagingException
     */
    public void send(String subject, Object content, Collection<String> receivers)
            throws AddressException, MessagingException {
        // 创建mime类型邮件
        final MimeMessage message = new MimeMessage(session);
        // 设置发信人
        message.setFrom(new InternetAddress(this.senderEmailAddr));
        Set<InternetAddress> addresses = new HashSet<>();
        for (String receiver : receivers) {
            addresses.add(new InternetAddress(receiver));
        }
        message.setRecipients(RecipientType.TO, addresses.toArray(new InternetAddress[0]));
        // 设置主题
        message.setSubject(subject);
        // 设置邮件内容
        message.setContent(content.toString(), "text/html;charset=utf-8");
        // 发送
        Transport.send(message);
    }

}
