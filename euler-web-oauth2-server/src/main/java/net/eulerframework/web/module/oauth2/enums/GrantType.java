package net.eulerframework.web.module.oauth2.enums;

public enum GrantType {
    AUTHORIZATION_CODE("authorization_code"),
    PASSWORD("password"),
    CLIENT_CREDENTIALS("client_credentials"),
    IMPLICIT("implicit"),
    REFRESH_TOKEN("refresh_token");
    
    private String value;
    
    GrantType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return this.value;
    }
}
