package net.eulerform.web.module.basedata.entity;

import javax.validation.constraints.NotNull;

public class EmailConfig {

    @NotNull
    private String username;
    @NotNull
    private String password;
    @NotNull
    private String smtp;
    @NotNull
    private String sender;
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
    
}
