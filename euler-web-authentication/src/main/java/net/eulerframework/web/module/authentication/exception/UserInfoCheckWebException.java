package net.eulerframework.web.module.authentication.exception;

import net.eulerframework.web.core.exception.web.WebError;
import net.eulerframework.web.core.exception.web.WebException;

public class UserInfoCheckWebException extends WebException {

    public UserInfoCheckWebException(String message) {
        super(
            message, 
            WebError.PARAMETER_NOT_MEET_REQUIREMENT.getReasonPhrase(), 
            WebError.PARAMETER_NOT_MEET_REQUIREMENT.value()
        );
    }
}
