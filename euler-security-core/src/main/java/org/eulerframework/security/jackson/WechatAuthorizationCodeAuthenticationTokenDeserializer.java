package org.eulerframework.security.jackson;

import org.eulerframework.security.authentication.WechatAuthorizationCodeAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.node.MissingNode;

import javax.annotation.Nullable;
import java.util.Set;

public class WechatAuthorizationCodeAuthenticationTokenDeserializer extends ValueDeserializer<WechatAuthorizationCodeAuthenticationToken> {

    private static final TypeReference<Set<GrantedAuthority>> GRANTED_AUTHORITY_SET = new TypeReference<>() {
    };

    @Override
    public WechatAuthorizationCodeAuthenticationToken deserialize(tools.jackson.core.JsonParser jp, DeserializationContext ctxt) throws tools.jackson.core.JacksonException {
        JsonNode jsonNode = ctxt.readTree(jp);
        boolean authenticated = readJsonNode(jsonNode, "authenticated").asBoolean();
        JsonNode principalNode = readJsonNode(jsonNode, "principal");
        Object principal = getPrincipal(ctxt, principalNode);
        JsonNode credentialsNode = readJsonNode(jsonNode, "credentials");
        Object credentials = getCredentials(credentialsNode);
        JsonNode authoritiesNode = readJsonNode(jsonNode, "authorities");
        Set<? extends GrantedAuthority> authorities = ctxt.readTreeAsValue(authoritiesNode,
                ctxt.getTypeFactory().constructType(GRANTED_AUTHORITY_SET));
        WechatAuthorizationCodeAuthenticationToken token = (!authenticated)
                ? WechatAuthorizationCodeAuthenticationToken.unauthenticated(credentials)
                : WechatAuthorizationCodeAuthenticationToken.authenticated(principal, credentials, authorities);
        JsonNode detailsNode = readJsonNode(jsonNode, "details");
        if (detailsNode.isNull() || detailsNode.isMissingNode()) {
            token.setDetails(null);
        }
        else {
            Object details = ctxt.readTreeAsValue(detailsNode, Object.class);
            token.setDetails(details);
        }
        return token;
    }

    private @Nullable Object getCredentials(JsonNode credentialsNode) {
        if (credentialsNode.isNull() || credentialsNode.isMissingNode()) {
            return null;
        }
        return credentialsNode.asString();
    }

    private Object getPrincipal(DeserializationContext ctxt, JsonNode principalNode)
            throws StreamReadException, DatabindException {
        if (principalNode.isObject()) {
            return ctxt.readTreeAsValue(principalNode, Object.class);
        }
        return principalNode.asString();
    }

    private JsonNode readJsonNode(JsonNode jsonNode, String field) {
        return jsonNode.has(field) ? jsonNode.get(field) : MissingNode.getInstance();
    }
}
