package org.eulerframework.security.core.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DelegatingUserContext implements UserContext {
    private final List<UserContext> userContexts = new ArrayList<>();

    public DelegatingUserContext(UserContext... userContext) {
        this.userContexts.addAll(Arrays.asList(userContext));
    }

    @Override
    public String getUsername() {
        String username = null;
        for (UserContext userContext : this.userContexts) {
            if ((username = userContext.getUsername()) != null) {
                return username;
            }
        }
        return username;
    }

    @Override
    public String getTenantId() {
        String tenantId = null;
        for (UserContext userContext : this.userContexts) {
            if ((tenantId = userContext.getTenantId()) != null) {
                return tenantId;
            }
        }
        return tenantId;
    }
}
