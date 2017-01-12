package net.eulerframework.web.module.authentication;

public class Lang {

    public enum PASSWD {
        INCORRECT_PASSWD_FORMAT,INCORRECT_PASSWD_LENGTH,INCORRECT_PASSWD, PASSWD_IS_ULL;
    }

    public enum PASSWD_CHANGE {
        UNKNOWN_CHANGE_PASSWD_ERROR;
    }

    public enum USER_EMAIL {
        INCORRECT_EMAIL_FORMAT,EMAIL_USED, EMAIL_IS_NULL;
    }

    public enum USER_MOBILE {
        INCORRECT_MOBILE_FORMAT,MOBILE_USED,NULL;
    }

    public enum USERNAME {
        INCORRECT_USERNAME_FORMAT,USERNAME_USED,USERNAME_IS_NULL;
    }

    public enum USER_SIGNUP {
        UNKNOWN_USER_SIGNUP_ERROR;
    }

}
