/*
 * Copyright 2013-2026 the original author or authors.
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

    @Override
    public String getTenantId() {
        return this.getUserContext().getTenantId();
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
