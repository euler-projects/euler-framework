package net.eulerframework.common.email;

public class EmailConfig {

    public final static String F_CONFIG_KEY_UESRNAME = "mail.username";
    public final static String F_CONFIG_KEY_PASSWORD = "mail.password";
    public final static String F_CONFIG_KEY_SMTP = "mail.smtp";
    public final static String F_CONFIG_KEY_SYS_SENDER = "mail.sender";
    public final static String F_CONFIG_KEY_DEFAULT_RECEIVER = "mail.defaultReceiver";
    
    public final static String DB_CONFIG_KEY_UESRNAME = "static_emailConfig_username";
    public final static String DB_CONFIG_KEY_PASSWORD = "static_emailConfig_password";
    public final static String DB_CONFIG_KEY_SMTP = "static_emailConfig_smtp";
    public final static String DB_CONFIG_KEY_SYS_SENDER = "static_emailConfig_sender";
    public final static String DB_CONFIG_KEY_DEFAULT_RECEIVER = "static_emailConfig_defaultReceiver";

    private String username;
    private String password;
    private String smtp;
    private String sender;
    private String defaultReceiver;
    
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getSmtp() {
        return smtp;
    }
    public void setSmtp(String smtp) {
        this.smtp = smtp;
    }
    public String getSender() {
        return sender;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }
    public String getDefaultReceiver() {
        return defaultReceiver;
    }
    public void setDefaultReceiver(String defaultReceiver) {
        this.defaultReceiver = defaultReceiver;
    }
    
}
