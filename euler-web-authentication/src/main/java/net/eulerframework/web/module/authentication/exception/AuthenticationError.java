package net.eulerframework.web.module.authentication.exception;

import net.eulerframework.web.core.exception.web.WebError;

/**
 * 系统预定义用户认证模块异常代码
 * 
 * <p>所有系统预定义用户认证模块异常代范围为{@code 710000 ~ 719999}</p>
 * 
 * @author cFrost
 *
 */
public enum AuthenticationError implements WebError {
    
    PASSWD_RESET_ERROR(710000, "passwd_reset_error"),
    USER_NOT_FOUND(710404, "user_not_found");
    
    private final int value;

    private final String reasonPhrase;


    private AuthenticationError(int value, String reasonPhrase) {
        this.value = value;
        this.reasonPhrase = reasonPhrase;
    }

    /**
     * Return the integer value of this web error code.
     */
    public int value() {
        return this.value;
    }

    /**
     * Return the reason phrase of this web error code.
     */
    public String getReasonPhrase() {
        return reasonPhrase;
    }
}