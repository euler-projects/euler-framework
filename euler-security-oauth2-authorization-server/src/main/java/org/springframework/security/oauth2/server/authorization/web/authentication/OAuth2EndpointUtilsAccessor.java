package org.springframework.security.oauth2.server.authorization.web.authentication;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.MultiValueMap;

public class OAuth2EndpointUtilsAccessor {
    public static MultiValueMap<String, String> getFormParameters(HttpServletRequest request) {
        return OAuth2EndpointUtils.getFormParameters(request);
    }
}
