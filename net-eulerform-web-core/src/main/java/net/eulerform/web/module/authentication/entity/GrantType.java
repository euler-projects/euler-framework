package net.eulerform.web.module.authentication.entity;

public enum GrantType {
    authorization_code,password,client_credentials,implicit,refresh_token;
    
    public static GrantType getGrantType(String str){
        if(authorization_code.toString().equalsIgnoreCase(str)){
            return authorization_code;
        } else if(password.toString().equalsIgnoreCase(str)){
            return password;
        } else if(client_credentials.toString().equalsIgnoreCase(str)){
            return client_credentials;
        } else if(implicit.toString().equalsIgnoreCase(str)){
            return implicit;
        } else if(refresh_token.toString().equalsIgnoreCase(str)){
            return refresh_token;
        }
        return null;
    }
}
