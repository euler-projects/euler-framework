package net.eulerframework.web.core.base.response;

import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.exception.web.AjaxException;

public class ErrorAjaxResponse extends AjaxResponse<String> {

    private int errorCode;
    private String errorMsg;
    private String localizedErrorMsg;

    public ErrorAjaxResponse(AjaxException ajaxException) {
        super("ERROR");
        if (WebConfig.isDebugMode()) {
            this.logger.error("Error Code: " + ajaxException.getCode() + "message: " + ajaxException.getMessage(),
                    ajaxException);
        }

        this.errorCode = ajaxException.getCode();
        this.errorMsg = ajaxException.getMessage();
        this.localizedErrorMsg = ajaxException.getLocalizedMessage();
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public String getLocalizedErrorMsg() {
        return localizedErrorMsg;
    }
}
