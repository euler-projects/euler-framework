package org.eulerframework.security.oauth2.server.authorization.jackson2;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;

import java.util.List;

public abstract class EulerOAuth2ObjectMapper {
    private static ObjectMapper objectMapper;

    public static ObjectMapper getInstance() {
        if (objectMapper != null) {
            return objectMapper;
        }

        synchronized (EulerOAuth2ObjectMapper.class) {
            if (objectMapper != null) {
                return objectMapper;
            }
            objectMapper = new ObjectMapper();
            List<Module> securityModules = SecurityJackson2Modules.getModules(EulerOAuth2ObjectMapper.class.getClassLoader());
            objectMapper.registerModules(securityModules);
            objectMapper.registerModule(new OAuth2AuthorizationServerJackson2Module());
            objectMapper.registerModule(new EulerOAuth2AuthorizationServerJackson2Module());
            return objectMapper;
        }
    }
}
