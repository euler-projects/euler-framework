package net.eulerframework.common.email;

import javax.mail.MessagingException;

public class ThreadSimpleMailSender {
    
    private final EmailConfig emailConfig;
    
    public ThreadSimpleMailSender(EmailConfig emailConfig) {
        this.emailConfig = emailConfig;
    }
    
    public void sendToDefaultReceiver(String subject, Object content) {
        MailSender1 senderThread = new MailSender1(subject, content);
        senderThread.start();
    }

    public void send(String subject, Object content, String receiver){
        MailSender2 senderThread = new MailSender2(subject, content, receiver);
        senderThread.start();
    }

    public void send(String subject, Object content, String... receiver) {
        MailSender3 senderThread = new MailSender3(subject, content, receiver);
        senderThread.start();
    }

    private class MailSender1 extends Thread {
        private final String subject;
        private final Object content;
        
        public MailSender1(String subject, Object content){
            this.subject = subject;
            this.content = content;
        }

        @Override
        public void run() {
            SimpleMailSender sender = new SimpleMailSender(emailConfig);
            try {
                sender.sendToDefaultReceiver(subject, content);
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
            
        }
    }
    
    private class MailSender2 extends Thread {
        private final String subject;
        private final Object content;
        private final String receiver;
        
        public MailSender2(String subject, Object content, String receiver){
            this.subject = subject;
            this.content = content;
            this.receiver = receiver;
        }

        @Override
        public void run() {
            SimpleMailSender sender = new SimpleMailSender(emailConfig);
            try {
                sender.send(subject, content, receiver);
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
            
        }
    }
    
    private class MailSender3 extends Thread {
        private final String subject;
        private final Object content;
        private final String[] receivers;
        
        public MailSender3(String subject, Object content, String... receiver){
            this.subject = subject;
            this.content = content;
            this.receivers = receiver;
        }

        @Override
        public void run() {
            SimpleMailSender sender = new SimpleMailSender(emailConfig);
            try {
                sender.send(subject, content, receivers);
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
            
        }
    }

}
