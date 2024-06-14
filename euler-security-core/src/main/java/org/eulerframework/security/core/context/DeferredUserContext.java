package org.eulerframework.security.core.context;

import org.eulerframework.security.core.userdetails.EulerUserDetails;

import java.util.function.Supplier;

public class DeferredUserContext implements UserContext {
    private UserContext userContext;
    private Supplier<UserContext> userContextSupplier;

    DeferredUserContext(Supplier<UserContext> userContextSupplier) {
        this.userContextSupplier = userContextSupplier;
    }
    @Override
    public EulerUserDetails getUserDetails() {
        return this.getUserContext().getUserDetails();
    }

    @Override
    public String getUserId() {
        return this.getUserContext().getUserId();
    }

    private UserContext getUserContext() {
        if (this.userContext != null) {
            return this.userContext;
        }
        synchronized (this) {
            if (this.userContext == null) {
                this.userContext = this.userContextSupplier.get();
                this.userContextSupplier = null;
            }
        }
        return this.userContext;
    }
}
