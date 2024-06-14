package org.eulerframework.security.authentication;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.util.function.Supplier;

public class DeferredAuthenticationManager implements AuthenticationManager {
    private AuthenticationManager authenticationManager;
    private Supplier<AuthenticationManager> authenticationManagerSupplier;

    public DeferredAuthenticationManager(Supplier<AuthenticationManager> authenticationManagerSupplier) {
        this.authenticationManagerSupplier = authenticationManagerSupplier;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (this.authenticationManager != null) {
            return this.authenticationManager.authenticate(authentication);
        }
        synchronized (this) {
            if (this.authenticationManager == null) {
                this.authenticationManager = this.authenticationManagerSupplier.get();
                this.authenticationManagerSupplier = null;
            }
        }
        return this.authenticationManager.authenticate(authentication);
    }
}
