package net.eulerframework.web.module.authentication.exception;

import net.eulerframework.web.core.exception.web.SystemWebError;
import net.eulerframework.web.core.exception.web.WebException;

public class UserInfoCheckWebException extends WebException {

    public UserInfoCheckWebException(String message) {
        super(
            message, 
            SystemWebError.PARAMETER_NOT_MEET_REQUIREMENT
        );
    }
}
