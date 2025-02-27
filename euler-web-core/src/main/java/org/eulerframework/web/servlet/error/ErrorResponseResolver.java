package org.eulerframework.web.servlet.error;

import jakarta.servlet.http.HttpServletRequest;
import org.eulerframework.web.core.base.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

public interface ErrorResponseResolver {
    /**
     * Resolve an error view for the specified details.
     * @param request the source request
     * @param status the http status of the error
     * @param model the suggested model to be used with the view
     * @return a resolved {@link ModelAndView} or {@code null}
     */
    ErrorResponse resolveErrorResponse(HttpServletRequest request, HttpStatus status, Map<String, Object> model);
}
