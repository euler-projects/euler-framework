/*
 * Copyright 2013-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
