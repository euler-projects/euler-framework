package net.eulerframework.web.module.authentication.exception;

import net.eulerframework.web.core.exception.web.SystemWebError;
import net.eulerframework.web.core.exception.web.WebRuntimeException;

public class UserInfoCheckWebException extends WebRuntimeException {

    public UserInfoCheckWebException(String message) {
        super(
            message, 
            SystemWebError.PARAMETER_NOT_MEET_REQUIREMENT
        );
    }
}
